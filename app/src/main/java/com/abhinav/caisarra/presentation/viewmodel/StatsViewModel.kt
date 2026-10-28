package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.dto.ModeStatsData
import com.abhinav.caisarra.data.remote.dto.ProfileData
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.ProfileRepository
import com.abhinav.caisarra.data.repository.RatingRepository
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StatsRange(val key: String, val label: String) {
    SEVEN_DAYS("7d", "7D"),
    THIRTY_DAYS("30d", "30D"),
    NINETY_DAYS("90d", "90D"),
    ONE_YEAR("1y", "1Y"),
    ALL("all", "ALL");

    companion object {
        fun fromKey(key: String?): StatsRange {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: THIRTY_DAYS
        }
    }
}

data class StatsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val username: String = "",
    val selectedMode: GameMode = GameMode.BLITZ,
    val selectedRange: StatsRange = StatsRange.THIRTY_DAYS,
    val stats: ModeStatsData? = null,
    val error: String? = null,
    val isGuest: Boolean = false
)

class StatsViewModel(
    private val authRepository: AuthRepository,
    private val ratingRepository: RatingRepository,
    private val profileRepository: ProfileRepository,
    private val initialUsername: String? = null,
    private val initialMode: String? = null
) : ViewModel() {

    private val _state = MutableStateFlow(
        StatsUiState(
            username = initialUsername?.takeUnless { it.contains("@") }?.trim().orEmpty(),
            selectedMode = parseGameMode(initialMode)
        )
    )
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    private var fetchJob: Job? = null
    private var cachedProfile: ProfileData? = null

    init {
        initializeAndLoad()
    }

    private fun initializeAndLoad() {
        viewModelScope.launch {
            val isGuest = authRepository.isGuest() || !authRepository.isLoggedIn()
            if (isGuest) {
                _state.value = _state.value.copy(
                    isGuest = true,
                    isLoading = false
                )
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true, error = null)

            val candidateUsername = _state.value.username.takeIf { it.isNotBlank() && !it.contains("@") }
            if (candidateUsername != null) {
                if (initialMode == null) {
                    try {
                        val profile = cachedProfile ?: runCatching {
                            profileRepository.getPublicProfile(candidateUsername)
                        }.getOrNull() ?: runCatching {
                            profileRepository.getMyProfile()
                        }.getOrNull()
                        cachedProfile = profile
                        val bestMode = pickBestInitialMode(profile)
                        _state.value = _state.value.copy(selectedMode = bestMode)
                    } catch (_: Exception) {}
                }
                loadStats(
                    username = candidateUsername,
                    mode = _state.value.selectedMode,
                    range = _state.value.selectedRange,
                    fallbackProfile = cachedProfile
                )
                return@launch
            }

            var resolvedUsername: String? = null
            try {
                val profile = profileRepository.getMyProfile()
                cachedProfile = profile
                val realUsername = profile.username.trim()
                if (realUsername.isNotBlank()) {
                    resolvedUsername = realUsername
                    authRepository.saveUsername(realUsername)
                }
            } catch (_: Exception) {
                val stored = authRepository.getUsername()?.trim()
                if (!stored.isNullOrBlank() && !stored.contains("@")) {
                    resolvedUsername = stored
                }
            }

            if (!resolvedUsername.isNullOrBlank()) {
                val bestMode = if (initialMode != null) {
                    _state.value.selectedMode
                } else {
                    pickBestInitialMode(cachedProfile)
                }
                _state.value = _state.value.copy(
                    username = resolvedUsername,
                    selectedMode = bestMode
                )
                loadStats(
                    username = resolvedUsername,
                    mode = bestMode,
                    range = _state.value.selectedRange,
                    fallbackProfile = cachedProfile
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Unable to identify user profile. Please log in again."
                )
            }
        }
    }

    fun selectMode(mode: GameMode) {
        if (_state.value.selectedMode == mode && _state.value.stats != null) return
        _state.value = _state.value.copy(selectedMode = mode)
        val username = _state.value.username
        if (username.isNotBlank() && !username.contains("@")) {
            loadStats(username, mode, _state.value.selectedRange, fallbackProfile = cachedProfile)
        } else {
            initializeAndLoad()
        }
    }

    fun selectRange(range: StatsRange) {
        if (_state.value.selectedRange == range && _state.value.stats != null) return
        _state.value = _state.value.copy(selectedRange = range)
        val username = _state.value.username
        if (username.isNotBlank() && !username.contains("@")) {
            loadStats(username, _state.value.selectedMode, range, fallbackProfile = cachedProfile)
        } else {
            initializeAndLoad()
        }
    }

    fun refresh() {
        val username = _state.value.username
        if (username.isNotBlank() && !username.contains("@") && _state.value.stats != null) {
            loadStats(
                username = username,
                mode = _state.value.selectedMode,
                range = _state.value.selectedRange,
                isRefresh = true,
                fallbackProfile = cachedProfile
            )
        } else {
            initializeAndLoad()
        }
    }

    fun retry() {
        initializeAndLoad()
    }

    private fun loadStats(
        username: String,
        mode: GameMode,
        range: StatsRange,
        isRefresh: Boolean = false,
        fallbackProfile: ProfileData? = null
    ) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                error = null
            )

            try {
                val targetUsername = if (username.contains("@")) {
                    val profile = profileRepository.getMyProfile()
                    cachedProfile = profile
                    val realUname = profile.username.trim()
                    authRepository.saveUsername(realUname)
                    _state.value = _state.value.copy(username = realUname)
                    realUname
                } else {
                    username
                }

                val stats = ratingRepository.getModeStats(
                    username = targetUsername,
                    mode = mode.ratingKey,
                    range = range.key
                )
                _state.value = _state.value.copy(
                    stats = stats,
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
            } catch (e: Exception) {
                val is404 = e.message?.contains("404") == true || e.message?.contains("not found", ignoreCase = true) == true
                if (is404 && initialUsername.isNullOrBlank()) {
                    try {
                        val profile = profileRepository.getMyProfile()
                        cachedProfile = profile
                        val realUname = profile.username.trim()
                        if (realUname.isNotBlank() && !realUname.equals(username, ignoreCase = true)) {
                            authRepository.saveUsername(realUname)
                            _state.value = _state.value.copy(username = realUname)
                            val retryStats = ratingRepository.getModeStats(
                                username = realUname,
                                mode = mode.ratingKey,
                                range = range.key
                            )
                            _state.value = _state.value.copy(
                                stats = retryStats,
                                isLoading = false,
                                isRefreshing = false,
                                error = null
                            )
                            return@launch
                        }
                    } catch (_: Exception) {}
                }

                val profile = fallbackProfile ?: cachedProfile ?: runCatching { profileRepository.getMyProfile() }.getOrNull()
                val modeRating = profile?.ratings?.entries?.firstOrNull {
                    it.key.equals(mode.ratingKey, ignoreCase = true)
                }?.value

                if (modeRating != null && modeRating.rating != null) {
                    val totalGames = modeRating.wins + modeRating.losses + modeRating.draws
                    val winPct = if (totalGames > 0) (modeRating.wins * 100 / totalGames) else 0
                    val fallbackStats = ModeStatsData(
                        mode = mode.ratingKey,
                        range = range.key,
                        rating = modeRating.rating,
                        provisional = modeRating.provisional,
                        startRating = modeRating.rating,
                        gamesPlayed = totalGames,
                        wins = modeRating.wins,
                        losses = modeRating.losses,
                        draws = modeRating.draws,
                        winPercent = winPct
                    )
                    _state.value = _state.value.copy(
                        stats = fallbackStats,
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                    return@launch
                }

                val msg = formatErrorMessage(e.message)
                _state.value = _state.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = msg
                )
            }
        }
    }

    private fun formatErrorMessage(rawMessage: String?): String {
        val msg = rawMessage.orEmpty()
        return when {
            msg.contains("429", ignoreCase = true) || msg.contains("too many requests", ignoreCase = true) ->
                "Rate limit exceeded. Please wait a moment and try again."
            msg.contains("404", ignoreCase = true) || msg.contains("not found", ignoreCase = true) ->
                "Player profile or statistics not found."
            msg.contains("500", ignoreCase = true) || msg.contains("internal server error", ignoreCase = true) ->
                "Server error encountered while retrieving statistics. Please try again later."
            msg.contains("Unable to resolve host", ignoreCase = true) || msg.contains("timeout", ignoreCase = true) ->
                "Network connection issue. Please check your internet connection."
            msg.isNotBlank() -> msg
            else -> "Failed to load player statistics"
        }
    }

    private fun pickBestInitialMode(profile: ProfileData?): GameMode {
        val ratings = profile?.ratings ?: return GameMode.RAPID
        val mostPlayed = ratings.entries.maxByOrNull {
            it.value.wins + it.value.losses + it.value.draws
        }
        return mostPlayed?.key?.let { parseGameMode(it) } ?: GameMode.RAPID
    }

    companion object {
        fun parseGameMode(modeKey: String?): GameMode {
            return GameMode.entries.firstOrNull {
                it.ratingKey.equals(modeKey, ignoreCase = true) ||
                it.name.equals(modeKey, ignoreCase = true)
            } ?: GameMode.BLITZ
        }
    }
}
