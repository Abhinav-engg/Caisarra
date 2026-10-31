package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.api.ClubService
import com.abhinav.caisarra.data.remote.api.LeaderboardEntry
import com.abhinav.caisarra.data.remote.api.LeaderboardService
import com.abhinav.caisarra.data.remote.api.PuzzleRushLeaderboardEntry
import com.abhinav.caisarra.data.remote.dto.ClubDto
import com.abhinav.caisarra.data.remote.dto.ClubLeaderboardPlayerDto
import com.abhinav.caisarra.data.repository.ClubRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LeaderboardType {
    GLOBAL,
    CLUB,
    PUZZLE_RUSH
}

data class LeaderboardUiState(
    val selectedType: LeaderboardType = LeaderboardType.GLOBAL,
    val globalEntries: List<LeaderboardEntry> = emptyList(),
    val clubEntries: List<LeaderboardEntry> = emptyList(),
    val puzzleRushEntries: List<PuzzleRushLeaderboardEntry> = emptyList(),
    val globalMode: String = "rapid",
    val clubMode: String = "rapid",
    val puzzleMode: String = "3min",
    val myClubs: List<ClubDto> = emptyList(),
    val selectedClub: ClubDto? = null,
    val clubId: String = "",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMoreGlobal: Boolean = true,
    val globalOffset: Int = 0,
    val error: String? = null
)

class LeaderboardViewModel(
    private val service: LeaderboardService,
    private val clubService: ClubService,
    private val clubRepository: ClubRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardUiState())
    val state = _state.asStateFlow()

    private val pageSize = 50

    init {
        loadGlobalLeaderboard()
        loadMyClubs()
    }

    fun selectType(type: LeaderboardType) {
        if (_state.value.selectedType == type) return

        _state.update {
            it.copy(
                selectedType = type,
                error = null
            )
        }

        when (type) {
            LeaderboardType.GLOBAL -> {
                if (_state.value.globalEntries.isEmpty()) {
                    loadGlobalLeaderboard()
                }
            }

            LeaderboardType.CLUB -> {
                if (_state.value.myClubs.isEmpty()) {
                    loadMyClubs()
                }
                if (_state.value.clubEntries.isEmpty() && _state.value.clubId.isNotBlank()) {
                    loadClubLeaderboard()
                }
            }

            LeaderboardType.PUZZLE_RUSH -> {
                if (_state.value.puzzleRushEntries.isEmpty()) {
                    loadPuzzleRushLeaderboard()
                }
            }
        }
    }

    fun loadMyClubs() {
        viewModelScope.launch {
            try {
                val clubs = clubRepository.getMyClubs()
                _state.update { current ->
                    val chosenClub = current.selectedClub ?: clubs.firstOrNull()
                    val targetClubId = current.clubId.ifBlank { chosenClub?.effectiveId.orEmpty() }
                    current.copy(
                        myClubs = clubs,
                        selectedClub = chosenClub,
                        clubId = targetClubId
                    )
                }
                if (_state.value.selectedType == LeaderboardType.CLUB && _state.value.clubEntries.isEmpty() && _state.value.clubId.isNotBlank()) {
                    loadClubLeaderboard()
                }
            } catch (_: Exception) {
            }
        }
    }

    fun selectClub(club: ClubDto) {
        _state.update {
            it.copy(
                selectedClub = club,
                clubId = club.effectiveId,
                error = null
            )
        }
        loadClubLeaderboard(clubId = club.effectiveId, mode = _state.value.clubMode)
    }

    fun setClubId(value: String) {
        _state.update {
            it.copy(
                clubId = value,
                error = null
            )
        }
    }

    fun selectGlobalMode(mode: String) {
        val normalized = mode.lowercase()
        if (normalized == _state.value.globalMode) return
        if (normalized !in setOf("bullet", "blitz", "rapid", "daily")) return

        _state.update {
            it.copy(
                globalMode = normalized,
                globalOffset = 0,
                globalEntries = emptyList(),
                hasMoreGlobal = true,
                error = null
            )
        }
        loadGlobalLeaderboard(refresh = true)
    }

    fun selectClubMode(mode: String) {
        val normalized = mode.lowercase()
        if (normalized == _state.value.clubMode) return
        if (normalized !in setOf("bullet", "blitz", "rapid", "daily")) return

        _state.update {
            it.copy(
                clubMode = normalized,
                error = null
            )
        }
        if (_state.value.clubId.isNotBlank()) {
            loadClubLeaderboard(clubId = _state.value.clubId, mode = normalized)
        }
    }

    fun selectPuzzleMode(mode: String) {
        val normalized = mode.lowercase()
        if (normalized == _state.value.puzzleMode) return
        if (normalized !in setOf("3min", "5min", "survival")) return

        loadPuzzleRushLeaderboard(mode = normalized)
    }

    fun loadGlobalLeaderboard(refresh: Boolean = false) {
        val current = _state.value
        if ((current.isLoading && !refresh) || current.isLoadingMore) return

        val offset = if (refresh) 0 else current.globalOffset
        val mode = current.globalMode

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = offset == 0 && !refresh,
                    isRefreshing = refresh,
                    isLoadingMore = offset > 0,
                    error = null
                )
            }

            try {
                val response = service.getGlobalLeaderboard(
                    mode = mode,
                    limit = pageSize,
                    offset = offset
                )

                check(response.success) {
                    response.error
                        ?: response.message?.takeIf { it.isNotBlank() }
                        ?: "Unable to load the global leaderboard."
                }

                val mappedEntries = response.data.mapIndexed { index, entry ->
                    if (entry.rank > 0) entry
                    else entry.copy(rank = offset + index + 1)
                }

                _state.update { prev ->
                    prev.copy(
                        globalEntries = if (offset == 0) {
                            mappedEntries
                        } else {
                            prev.globalEntries + mappedEntries
                        },
                        globalOffset = offset + mappedEntries.size,
                        hasMoreGlobal = mappedEntries.size == pageSize && mappedEntries.isNotEmpty(),
                        isLoading = false,
                        isLoadingMore = false,
                        isRefreshing = false,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val userError = ApiErrorInterpreter.toUserMessage(e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        isRefreshing = false,
                        error = userError
                    )
                }
            }
        }
    }

    fun loadMoreGlobalLeaderboard() {
        val current = _state.value
        if (current.isLoading || current.isLoadingMore || !current.hasMoreGlobal) return
        loadGlobalLeaderboard()
    }

    fun loadClubLeaderboard(
        clubId: String = _state.value.clubId,
        mode: String = _state.value.clubMode
    ) {
        var input = clubId.trim()
        val normalizedMode = mode.lowercase()

        if (input.isBlank()) {
            input = _state.value.selectedClub?.effectiveId
                ?: _state.value.myClubs.firstOrNull()?.effectiveId
                .orEmpty()
        }

        if (input.isBlank()) {
            _state.update {
                it.copy(
                    error = "Select a club or enter a club ID / share code."
                )
            }
            return
        }

        if (normalizedMode !in setOf("bullet", "blitz", "rapid", "daily")) {
            _state.update {
                it.copy(error = "Select a valid rating mode.")
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    clubId = input,
                    clubMode = normalizedMode,
                    isLoading = true,
                    error = null,
                    clubEntries = emptyList()
                )
            }

            try {
                val myClubs = if (_state.value.myClubs.isNotEmpty()) {
                    _state.value.myClubs
                } else {
                    try {
                        val fetched = clubRepository.getMyClubs()
                        _state.update { it.copy(myClubs = fetched) }
                        fetched
                    } catch (_: Exception) {
                        emptyList()
                    }
                }

                var resolvedClub = myClubs.firstOrNull { club ->
                    club.shareCode.equals(input, ignoreCase = true) ||
                            club.effectiveId.equals(input, ignoreCase = true) ||
                            club.id.equals(input, ignoreCase = true) ||
                            club.clubId.equals(input, ignoreCase = true) ||
                            club.name.equals(input, ignoreCase = true)
                }

                if (resolvedClub == null) {
                    try {
                        val searchResults = clubRepository.getClubs(
                            search = input,
                            offset = 0
                        )

                        resolvedClub = searchResults.firstOrNull { club ->
                            club.shareCode.equals(input, ignoreCase = true) ||
                                    club.effectiveId.equals(input, ignoreCase = true) ||
                                    club.id.equals(input, ignoreCase = true) ||
                                    club.clubId.equals(input, ignoreCase = true) ||
                                    club.name.equals(input, ignoreCase = true)
                        } ?: searchResults.firstOrNull()
                    } catch (_: Exception) {
                    }
                }

                val resolvedId = resolvedClub?.effectiveId ?: input

                val clubPlayers = clubRepository.getLeaderboard(
                    id = resolvedId,
                    mode = normalizedMode
                )

                val sortedPlayers = clubPlayers.sortedWith(
                    compareBy<ClubLeaderboardPlayerDto> { if (it.rank > 0) it.rank else Int.MAX_VALUE }
                        .thenByDescending { it.rating }
                )

                val entries = sortedPlayers.mapIndexed { index, player ->
                    LeaderboardEntry(
                        rank = if (player.rank > 0) player.rank else index + 1,
                        userId = player.userId,
                        username = player.username,
                        rating = player.rating
                    )
                }

                _state.update {
                    it.copy(
                        selectedClub = resolvedClub ?: it.selectedClub,
                        clubEntries = entries,
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val userError = ApiErrorInterpreter.toUserMessage(e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = userError
                    )
                }
            }
        }
    }

    fun loadPuzzleRushLeaderboard(
        mode: String = _state.value.puzzleMode
    ) {
        val normalizedMode = mode.lowercase()

        if (normalizedMode !in setOf("3min", "5min", "survival")) {
            _state.update {
                it.copy(
                    isRefreshing = false,
                    error = "Select a valid Puzzle Rush mode."
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    puzzleMode = normalizedMode,
                    isLoading = true,
                    error = null
                )
            }

            try {
                val response = service.getPuzzleRushLeaderboard(
                    mode = normalizedMode,
                    limit = 50
                )

                _state.update {
                    it.copy(
                        puzzleRushEntries = response.entries,
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val userError = ApiErrorInterpreter.toUserMessage(e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = userError
                    )
                }
            }
        }
    }

    fun refresh() {
        _state.update { it.copy(isRefreshing = true, error = null) }
        when (_state.value.selectedType) {
            LeaderboardType.GLOBAL ->
                loadGlobalLeaderboard(refresh = true)

            LeaderboardType.CLUB ->
                loadClubLeaderboard()

            LeaderboardType.PUZZLE_RUSH ->
                loadPuzzleRushLeaderboard()
        }
    }

    fun retry() {
        when (_state.value.selectedType) {
            LeaderboardType.GLOBAL ->
                loadGlobalLeaderboard(refresh = true)

            LeaderboardType.CLUB ->
                loadClubLeaderboard()

            LeaderboardType.PUZZLE_RUSH ->
                loadPuzzleRushLeaderboard()
        }
    }
}
