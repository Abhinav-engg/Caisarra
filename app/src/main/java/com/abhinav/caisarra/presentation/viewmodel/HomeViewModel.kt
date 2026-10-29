
package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.local.RatingPreferencesStore
import com.abhinav.caisarra.data.local.TokenExpiryChecker
import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.data.local.entity.RecordStatus
import com.abhinav.caisarra.data.remote.dto.ModeRating
import com.abhinav.caisarra.data.remote.dto.ProfileData
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.GameRepository
import com.abhinav.caisarra.data.repository.ProfileRepository
import com.abhinav.caisarra.data.repository.PuzzleRepository
import com.abhinav.caisarra.data.repository.RatingRepository
import com.abhinav.caisarra.data.repository.RemoteGameRepository
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant

data class ActiveGame(
    val id: String,
    val opponentUsername: String,
    val isMyTurn: Boolean,
    val timeControlMinutes: Int,
    val mode: GameMode = GameMode.DAILY,
    val daysPerMove: Int = ((timeControlMinutes + 1439) / 1440).coerceAtLeast(1),
    val position: String = "",
    val expiresAt: Long? = null,
    val initialTimeMs: Long = 0L,
    val whiteTimeMs: Long = 0L,
    val blackTimeMs: Long = 0L
)

typealias ActiveDailyGame = ActiveGame

data class HomeUiState(
    val username: String = "",
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val streak: Int = 0,
    val selectedMode: GameMode = GameMode.RAPID,
    val rating: Int? = null,
    val ratingChange: Int? = null,
    val provisional: Boolean = true,
    val modeRatings: Map<GameMode, Int?> = emptyMap(),
    val isLoggingOut: Boolean = false,
    val error: String? = null,
    val gameHistory: List<GameEntity> = emptyList(),
    val isLoadingHistory: Boolean = true,
    val activeGames: List<ActiveGame> = emptyList(),
    val activeDailyGames: List<ActiveDailyGame> = emptyList()
)

class HomeViewModel(
    private val repository: AuthRepository,
    private val gameRepository: GameRepository,
    private val remoteGameRepository: RemoteGameRepository,
    private val profileRepository: ProfileRepository,
    private val ratingPreferenceStore: RatingPreferencesStore? = null,
    private val puzzleRepository: PuzzleRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var localHistory: List<GameEntity> = emptyList()
    private var remoteHistory: List<GameEntity> = emptyList()
    private var signedInUser = false
    private var cachedProfile: ProfileData? = null
    private var hasStartedInitialLoad = false

    init {
        if (!hasStartedInitialLoad) {
            hasStartedInitialLoad = true
            loadHomeData()
        }
    }

    fun refresh() {
        if (hasStartedInitialLoad) return

        hasStartedInitialLoad = true
        loadHomeData()
    }

    fun selectMode(mode: GameMode) {
        ratingPreferenceStore?.setSelectedMode(mode)
        val rating = extractRating(cachedProfile, mode)
        val provisional = extractProvisional(cachedProfile, mode)
        val change = ratingPreferenceStore?.getRatingChange(mode)
        _state.update {
            it.copy(
                selectedMode = mode,
                rating = rating,
                provisional = provisional,
                ratingChange = change
            )
        }
    }

    private fun extractRating(profile: ProfileData?, mode: GameMode): Int? {
        if (profile == null) return null
        return profile.ratings?.get(mode.ratingKey)?.rating
            ?: if (mode == GameMode.RAPID) profile.rating else null
    }

    private fun extractProvisional(profile: ProfileData?, mode: GameMode): Boolean {
        if (profile == null) return true
        return profile.ratings?.get(mode.ratingKey)?.provisional
            ?: if (mode == GameMode.RAPID) profile.provisional else true
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingHistory = true,
                    error = null,
                    gameHistory = emptyList()
                )
            }

            val savedUsername = repository.getUsername()
                ?.takeIf { it.isNotBlank() }
            signedInUser = repository.isLoggedIn()
            val profile = if (signedInUser) {
                try {
                    profileRepository.getMyProfile().also {
                        cachedProfile = it
                        if (it.username.isNotBlank()) {
                            repository.saveUsername(it.username)
                        }
                    }
                } catch (e: Exception) {
                    null
                }
            } else null
            val displayUsername = profile?.displayName?.takeIf { it.isNotBlank() }
                ?: profile?.username?.takeIf { it.isNotBlank() }
                ?: savedUsername?.takeUnless { it.contains("@") }
                ?: if (signedInUser) "Player" else "Guest"
            val currentUsername = profile?.username?.takeIf { it.isNotBlank() } ?: savedUsername

            val selectedMode = ratingPreferenceStore?.getSelectedMode() ?: _state.value.selectedMode
            val modeRatings = GameMode.entries.associateWith { mode ->
                extractRating(profile, mode)
            }
            val currentRating = modeRatings[selectedMode]
            val currentProvisional = extractProvisional(profile, selectedMode)
            val currentChange = ratingPreferenceStore?.getRatingChange(selectedMode)

            _state.update {
                it.copy(
                    username = displayUsername,
                    displayName = profile?.displayName,
                    avatarUrl = profile?.avatarUrl,
                    streak = it.streak,
                    selectedMode = selectedMode,
                    rating = currentRating,
                    provisional = currentProvisional,
                    ratingChange = currentChange,
                    modeRatings = modeRatings
                )
            }

            if (puzzleRepository != null) {
                launch {
                    val streak = try {
                        val daily = puzzleRepository.getDailyPuzzle().getOrNull()
                        daily?.currentStreak
                            ?: if (signedInUser) puzzleRepository.getStats().getOrNull()?.currentStreak ?: 0 else 0
                    } catch (e: Exception) {
                        0
                    }
                    _state.update { it.copy(streak = streak) }
                }
            }

            if (signedInUser) {
                localHistory = emptyList()

                try {
                    val myId = repository.getValidAccessToken()
                        ?.let { TokenExpiryChecker.getUserId(it) }

                    val rawGames = remoteGameRepository.getRawGameHistory()
                    with(remoteGameRepository) {
                        remoteHistory = rawGames
                            .map { it.toGameEntity(currentUsername ?: savedUsername!!) }
                            .filter(::isCompletedGame)
                    }

                    val now = System.currentTimeMillis()
                    val activeList = rawGames.mapNotNull { dto ->
                        val isFinished = dto.status.equals("finished", ignoreCase = true)
                            || dto.status.equals("completed", ignoreCase = true)
                            || dto.status.equals("aborted", ignoreCase = true)
                            || dto.status.equals("expired", ignoreCase = true)
                            || dto.status.equals("resigned", ignoreCase = true)
                            || dto.status.equals("timeout", ignoreCase = true)
                            || dto.endedAt != null

                        if (isFinished) return@mapNotNull null

                        val mode = resolveMode(dto.timeControlMinutes, dto.timeControlMode)

                        val isWhite = (myId != null && dto.whitePlayerId == myId)
                            || (currentUsername != null && dto.whiteUsername?.equals(currentUsername, ignoreCase = true) == true)

                        val isBlack = (myId != null && dto.blackPlayerId == myId)
                            || (currentUsername != null && dto.blackUsername?.equals(currentUsername, ignoreCase = true) == true)

                        val userIsWhite = if (isWhite) true else !isBlack && (dto.whiteUsername?.equals(savedUsername, ignoreCase = true) == true)

                        val opponent = if (userIsWhite) {
                            dto.blackUsername?.takeIf { it.isNotBlank() } ?: "Player #${dto.blackPlayerId}"
                        } else {
                            dto.whiteUsername?.takeIf { it.isNotBlank() } ?: "Player #${dto.whitePlayerId}"
                        }

                        val isWhiteTurn = dto.currentTurn.equals("white", ignoreCase = true) || dto.currentTurn.equals("w", ignoreCase = true)
                        val isMyTurn = if (userIsWhite) {
                            isWhiteTurn
                        } else {
                            dto.currentTurn.equals("black", ignoreCase = true) || dto.currentTurn.equals("b", ignoreCase = true)
                        }

                        val initialMs = if (dto.initialTimeMs > 0L) {
                            dto.initialTimeMs
                        } else {
                            dto.timeControlMinutes.toLong() * 60L * 1000L
                        }

                        val daysPerMove = ((dto.timeControlMinutes + 1439) / 1440).coerceAtLeast(1)

                        val expiresAt: Long?
                        if (mode == GameMode.DAILY) {
                            val whiteTimeMs = if (dto.whiteTimeMs > 0L) dto.whiteTimeMs else daysPerMove * 86_400_000L
                            val blackTimeMs = if (dto.blackTimeMs > 0L) dto.blackTimeMs else daysPerMove * 86_400_000L
                            val turnTime = if (isWhiteTurn) whiteTimeMs else blackTimeMs
                            expiresAt = if (turnTime > 0L) now + turnTime else null
                        } else {
                            val whiteTimeMs = dto.whiteTimeMs
                            val blackTimeMs = dto.blackTimeMs
                            val currentTurnRemainingMs = if (isWhiteTurn) whiteTimeMs else blackTimeMs
                            val hasStarted = dto.startedAt != null
                            val hasMoves = (whiteTimeMs != initialMs && whiteTimeMs > 0L) || (blackTimeMs != initialMs && blackTimeMs > 0L)

                            if ((hasStarted || hasMoves) && currentTurnRemainingMs <= 0L) {
                                return@mapNotNull null
                            }

                            val gameTimestamp = runCatching {
                                Instant.parse(dto.startedAt ?: dto.createdAt).toEpochMilli()
                            }.getOrElse { 0L }

                            if (gameTimestamp > 0L) {
                                val elapsed = now - gameTimestamp
                                if (elapsed > (initialMs * 2 + 10 * 60 * 1000L)) {
                                    return@mapNotNull null
                                }
                            }

                            val effectiveRemaining = if (currentTurnRemainingMs > 0L) currentTurnRemainingMs else initialMs
                            expiresAt = now + effectiveRemaining
                            if (expiresAt <= now) {
                                return@mapNotNull null
                            }
                        }

                        ActiveGame(
                            id = dto.id,
                            opponentUsername = opponent,
                            isMyTurn = isMyTurn,
                            timeControlMinutes = dto.timeControlMinutes,
                            mode = mode,
                            daysPerMove = daysPerMove,
                            position = dto.position,
                            expiresAt = expiresAt,
                            initialTimeMs = initialMs,
                            whiteTimeMs = dto.whiteTimeMs,
                            blackTimeMs = dto.blackTimeMs
                        )
                    }

                    _state.update {
                        it.copy(
                            activeGames = activeList,
                            activeDailyGames = activeList.filter { g -> g.mode == GameMode.DAILY }
                        )
                    }

                    startExpiryTicker()

                    publishHistory()
                    _state.update {
                        it.copy(
                            isLoadingHistory = false,
                            error = null
                        )
                    }
                } catch (e: Exception) {
                    remoteHistory = emptyList()

                    _state.update {
                        it.copy(
                            activeGames = emptyList(),
                            activeDailyGames = emptyList(),
                            isLoadingHistory = false,
                            error = e.message ?: "Unable to load server game history."
                        )
                    }

                    publishHistory()
                }
            } else {
                remoteHistory = emptyList()
                _state.update {
                    it.copy(
                        activeGames = emptyList(),
                        activeDailyGames = emptyList()
                    )
                }

                val guestId = repository.getGuestId()
                    ?: "local-user"

                try {
                    gameRepository.observeFinished(guestId)
                        .collect { games ->
                            localHistory = games.filter(
                                ::isCompletedGame
                            )

                            publishHistory()

                            _state.update {
                                it.copy(
                                    isLoadingHistory = false,
                                    error = null
                                )
                            }
                        }
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            isLoadingHistory = false,
                            error = e.message ?: "Unable to load local game history."
                        )
                    }
                }
            }
        }
    }

    private fun isCompletedGame(game: GameEntity): Boolean {
        return game.status != RecordStatus.InProgress &&
                game.endedAt != null
    }

    private fun publishHistory() {
        val sourceHistory =
            if (signedInUser) remoteHistory else localHistory

        val history = sourceHistory
            .filter(::isCompletedGame)
            .distinctBy { it.id }
            .sortedByDescending {
                it.endedAt ?: it.startedAt
            }

        _state.update {
            it.copy(gameHistory = history)
        }
    }

    fun logout(onSuccess: () -> Unit) {
        if (_state.value.isLoggingOut) return

        _state.update {
            it.copy(
                isLoggingOut = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val result = repository.logout()

                _state.update {
                    it.copy(isLoggingOut = false)
                }

                when (result) {
                    is com.abhinav.caisarra.data.repository.AuthResult.Success -> {
                        onSuccess()
                    }

                    is com.abhinav.caisarra.data.repository.AuthResult.Error -> {
                        _state.update {
                            it.copy(error = result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoggingOut = false,
                        error = e.message
                            ?: "Unable to log out."
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update {
            it.copy(error = null)
        }
    }

    fun removeExpiredGame(gameId: String) {
        _state.update { state ->
            val updated = state.activeGames.filter { it.id != gameId }
            state.copy(
                activeGames = updated,
                activeDailyGames = updated.filter { it.mode == GameMode.DAILY }
            )
        }
    }

    private var expiryTickerJob: Job? = null

    private fun startExpiryTicker() {
        expiryTickerJob?.cancel()
        expiryTickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val now = System.currentTimeMillis()
                val current = _state.value.activeGames
                val stillActive = current.filter { it.expiresAt == null || it.expiresAt > now }
                if (stillActive.size != current.size) {
                    _state.update {
                        it.copy(
                            activeGames = stillActive,
                            activeDailyGames = stillActive.filter { g -> g.mode == GameMode.DAILY }
                        )
                    }
                }
                if (stillActive.none { it.expiresAt != null }) {
                    break
                }
            }
        }
    }

    private fun resolveMode(timeControlMinutes: Int, explicitMode: String? = null): GameMode {
        val normalized = explicitMode?.trim()?.lowercase()
        return when {
            normalized == "daily" || timeControlMinutes >= 1440 -> GameMode.DAILY
            normalized == "bullet" || (normalized == null && timeControlMinutes < 3) -> GameMode.BULLET
            normalized == "blitz" || (normalized == null && timeControlMinutes < 10) -> GameMode.BLITZ
            else -> GameMode.RAPID
        }
    }

    override fun onCleared() {
        super.onCleared()
        expiryTickerJob?.cancel()
    }
}
