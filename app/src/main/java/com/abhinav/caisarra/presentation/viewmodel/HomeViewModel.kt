
package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.local.RatingPreferencesStore
import com.abhinav.caisarra.data.local.TokenExpiryChecker
import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.data.local.entity.RecordStatus
import com.abhinav.caisarra.data.remote.dto.ProfileData
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.GameRepository
import com.abhinav.caisarra.data.repository.ProfileRepository
import com.abhinav.caisarra.data.repository.RemoteGameRepository
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActiveDailyGame(
    val id: String,
    val opponentUsername: String,
    val isMyTurn: Boolean,
    val timeControlMinutes: Int,
    val daysPerMove: Int = (timeControlMinutes + 1439) / 1440,
    val position: String = ""
)

data class HomeUiState(
    val username: String = "",
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val streak: Int = 7,
    val selectedMode: GameMode = GameMode.RAPID,
    val rating: Int? = null,
    val ratingChange: Int? = null,
    val provisional: Boolean = true,
    val modeRatings: Map<GameMode, Int?> = emptyMap(),
    val isLoggingOut: Boolean = false,
    val error: String? = null,
    val gameHistory: List<GameEntity> = emptyList(),
    val isLoadingHistory: Boolean = true,
    val activeDailyGames: List<ActiveDailyGame> = emptyList()
)

class HomeViewModel(
    private val repository: AuthRepository,
    private val gameRepository: GameRepository,
    private val remoteGameRepository: RemoteGameRepository,
    private val profileRepository: ProfileRepository,
    private val ratingPreferenceStore: RatingPreferencesStore? = null
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var localHistory: List<GameEntity> = emptyList()
    private var remoteHistory: List<GameEntity> = emptyList()
    private var signedInUser = false
    private var cachedProfile: ProfileData? = null

    init {
        loadHomeData()
    }

    fun refresh() {
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
            signedInUser = savedUsername != null
            val profile = if (signedInUser) {
                try {
                    profileRepository.getMyProfile().also { cachedProfile = it }
                } catch (e: Exception) {
                    null
                }
            } else null
            val displayUsername = profile?.username?.takeIf { it.isNotBlank() }
                ?: savedUsername?.takeUnless { it.contains("@") }
                ?: "Guest"

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
                    streak = 7,
                    selectedMode = selectedMode,
                    rating = currentRating,
                    provisional = currentProvisional,
                    ratingChange = currentChange,
                    modeRatings = modeRatings
                )
            }

            if (signedInUser) {
                localHistory = emptyList()

                try {
                    val myId = repository.getValidAccessToken()
                        ?.let { TokenExpiryChecker.getUserId(it) }

                    val rawGames = remoteGameRepository.getRawGameHistory()
                    with(remoteGameRepository) {
                        remoteHistory = rawGames
                            .map { it.toGameEntity(savedUsername!!) }
                            .filter(::isCompletedGame)
                    }

                    val activeDaily = rawGames.filter { dto ->
                        val isUnfinished = !dto.status.equals("finished", ignoreCase = true) && dto.endedAt == null
                        val isDaily = dto.timeControlMinutes >= 1440 || dto.timeControlMode.equals("daily", ignoreCase = true)
                        isUnfinished && isDaily
                    }.map { dto ->
                        val isWhite = if (myId != null) {
                            dto.whitePlayerId == myId
                        } else {
                            dto.whiteUsername.equals(savedUsername, ignoreCase = true)
                        }

                        val opponent = if (isWhite) {
                            dto.blackUsername?.takeIf { it.isNotBlank() } ?: "Player #${dto.blackPlayerId}"
                        } else {
                            dto.whiteUsername?.takeIf { it.isNotBlank() } ?: "Player #${dto.whitePlayerId}"
                        }

                        val isMyTurn = if (isWhite) {
                            dto.currentTurn.equals("white", ignoreCase = true)
                        } else {
                            dto.currentTurn.equals("black", ignoreCase = true)
                        }

                        ActiveDailyGame(
                            id = dto.id,
                            opponentUsername = opponent,
                            isMyTurn = isMyTurn,
                            timeControlMinutes = dto.timeControlMinutes,
                            daysPerMove = ((dto.timeControlMinutes + 1439) / 1440).coerceAtLeast(1),
                            position = dto.position
                        )
                    }

                    _state.update {
                        it.copy(activeDailyGames = activeDaily)
                    }

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
                            activeDailyGames = emptyList(),
                            isLoadingHistory = false,
                            error = e.message ?: "Unable to load server game history."
                        )
                    }

                    publishHistory()
                }
            } else {
                remoteHistory = emptyList()
                _state.update { it.copy(activeDailyGames = emptyList()) }

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
}
