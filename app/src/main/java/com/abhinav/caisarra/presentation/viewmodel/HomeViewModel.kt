
package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.data.local.entity.RecordStatus
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.GameRepository
import com.abhinav.caisarra.data.repository.RemoteGameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.abhinav.caisarra.data.repository.ProfileRepository

data class HomeUiState(
    val username: String = "",
    val isLoggingOut: Boolean = false,
    val error: String? = null,
    val gameHistory: List<GameEntity> = emptyList(),
    val isLoadingHistory: Boolean = true
)

class HomeViewModel(
    private val repository: AuthRepository,
    private val gameRepository: GameRepository,
    private val remoteGameRepository: RemoteGameRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var localHistory: List<GameEntity> = emptyList()
    private var remoteHistory: List<GameEntity> = emptyList()
    private var signedInUser = false

    init {
        loadHomeData()
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
            val displayUsername = if (signedInUser) {
                try {
                    profileRepository.getMyProfile().username.takeIf { it.isNotBlank() } ?: "Player"
                } catch (e: Exception) {
                    savedUsername
                        ?.takeUnless { it.contains("@") }
                        ?: "Player"
                }
            } else {
                "Guest"
            }

            _state.update {
                it.copy(username = displayUsername)
            }

            if (signedInUser) {
                localHistory = emptyList()

                try {
                    remoteHistory = remoteGameRepository
                        .getGameHistory(savedUsername!!)
                        .filter(::isCompletedGame)
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
                            isLoadingHistory = false,
                            error = e.message ?: "Unable to load server game history."
                        )
                    }

                    publishHistory()
                }
            } else {
                remoteHistory = emptyList()

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
