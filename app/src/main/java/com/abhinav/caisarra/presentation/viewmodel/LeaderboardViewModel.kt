
package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.api.ClubService
import com.abhinav.caisarra.data.remote.api.LeaderboardEntry
import com.abhinav.caisarra.data.remote.api.LeaderboardService
import com.abhinav.caisarra.data.remote.api.PuzzleRushLeaderboardEntry
import com.abhinav.caisarra.data.repository.ClubRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val clubId: String = "",
    val clubMode: String = "blitz",
    val puzzleMode: String = "3min",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
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
    }

    fun selectType(type: LeaderboardType) {
        if (_state.value.selectedType == type) return

        _state.value = _state.value.copy(
            selectedType = type,
            error = null
        )

        when (type) {
            LeaderboardType.GLOBAL -> {
                if (_state.value.globalEntries.isEmpty()) {
                    loadGlobalLeaderboard()
                }
            }

            LeaderboardType.CLUB -> Unit

            LeaderboardType.PUZZLE_RUSH -> {
                loadPuzzleRushLeaderboard()
            }
        }
    }

    fun setClubId(value: String) {
        _state.value = _state.value.copy(
            clubId = value,
            error = null
        )
    }

    fun loadGlobalLeaderboard(refresh: Boolean = false) {
        val current = _state.value

        if (current.isLoading || current.isLoadingMore) return

        val offset = if (refresh) 0 else current.globalOffset

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = offset == 0,
                isLoadingMore = offset > 0,
                error = null
            )

            try {
                val response = service.getGlobalLeaderboard(
                    limit = pageSize,
                    offset = offset
                )

                check(response.success) {
                    response.message.ifBlank {
                        "Unable to load the global leaderboard."
                    }
                }

                _state.value = _state.value.copy(
                    globalEntries = if (offset == 0) {
                        response.data
                    } else {
                        _state.value.globalEntries + response.data
                    },
                    globalOffset = offset + response.data.size,
                    hasMoreGlobal = response.data.size == pageSize,
                    isLoading = false,
                    isLoadingMore = false,
                    error = null
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = e.message
                        ?: "Unable to load the global leaderboard."
                )
            }
        }
    }

    fun loadMoreGlobalLeaderboard() {
        val current = _state.value

        if (
            current.isLoading ||
            current.isLoadingMore ||
            !current.hasMoreGlobal
        ) return

        loadGlobalLeaderboard()
    }


    fun loadClubLeaderboard(
        clubId: String = _state.value.clubId,
        mode: String = _state.value.clubMode
    ) {
        val input = clubId.trim()
        val normalizedMode = mode.lowercase()

        if (input.isBlank()) {
            _state.value = _state.value.copy(
                error = "Enter a club ID or share code."
            )
            return
        }

        if (
            normalizedMode !in
            setOf("bullet", "blitz", "rapid", "daily")
        ) {
            _state.value = _state.value.copy(
                error = "Select a valid rating mode."
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                clubId = input,
                clubMode = normalizedMode,
                isLoading = true,
                error = null,
                clubEntries = emptyList()
            )

            try {
                val myClubs = clubRepository.getMyClubs()

                var resolvedClub = myClubs.firstOrNull { club ->
                    club.shareCode.equals(
                        input,
                        ignoreCase = true
                    ) ||
                            club.effectiveId.equals(
                                input,
                                ignoreCase = true
                            ) ||
                            club.id.equals(
                                input,
                                ignoreCase = true
                            ) ||
                            club.clubId.equals(
                                input,
                                ignoreCase = true
                            )
                }

                if (resolvedClub == null) {
                    val searchResults = clubRepository.getClubs(
                        search = input,
                        offset = 0
                    )

                    resolvedClub = searchResults.firstOrNull { club ->
                        club.shareCode.equals(
                            input,
                            ignoreCase = true
                        ) ||
                                club.effectiveId.equals(
                                    input,
                                    ignoreCase = true
                                ) ||
                                club.id.equals(
                                    input,
                                    ignoreCase = true
                                ) ||
                                club.clubId.equals(
                                    input,
                                    ignoreCase = true
                                )
                    }
                }

                val resolvedId = resolvedClub?.effectiveId
                    ?: run {
                        val enteredIsShareCode = myClubs.any { club ->
                            club.shareCode.equals(
                                input,
                                ignoreCase = true
                            )
                        }

                        if (enteredIsShareCode) {
                            error(
                                "Unable to resolve the club's internal ID. " +
                                        "Refresh My Clubs and try again."
                            )
                        }

                        input
                    }
                val clubPlayers = clubRepository.getLeaderboard(
                    id = resolvedId,
                    mode = normalizedMode
                )

                val entries = clubPlayers.mapIndexed { index, player ->
                    LeaderboardEntry(
                        rank = if (player.rank > 0) {
                            player.rank
                        } else {
                            index + 1
                        },
                        user_id = player.userId,
                        username = player.username,
                        rating = player.rating
                    )
                }

                _state.value = _state.value.copy(
                    clubEntries = entries,
                    isLoading = false,
                    error = null
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("404") == true ->
                        "The club or its leaderboard was not found. " +
                                "Check the club and try again."

                    e.message?.contains("401") == true ->
                        "Your session has expired. Please sign in again."

                    e.message?.contains("403") == true ->
                        "You don't have permission to access this leaderboard."

                    else -> e.message
                        ?: "Unable to load club leaderboard."
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    error = message
                )
            }
        }
    }

    fun loadPuzzleRushLeaderboard(
        mode: String = _state.value.puzzleMode
    ) {
        val normalizedMode = mode.lowercase()

        if (
            normalizedMode !in
            setOf("3min", "5min", "survival")
        ) {
            _state.value = _state.value.copy(
                error = "Select a valid Puzzle Rush mode."
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                puzzleMode = normalizedMode,
                isLoading = true,
                error = null
            )

            try {
                val response = service.getPuzzleRushLeaderboard(
                    mode = normalizedMode,
                    limit = 50
                )

                _state.value = _state.value.copy(
                    puzzleRushEntries = response.entries,
                    isLoading = false,
                    error = null
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                        ?: "Unable to load Puzzle Rush leaderboard."
                )
            }
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
