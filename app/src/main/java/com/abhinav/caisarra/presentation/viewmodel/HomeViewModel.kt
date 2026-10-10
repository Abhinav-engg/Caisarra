
package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.GameRepository
import com.abhinav.caisarra.data.repository.RemoteGameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val username: String = "",
    val isLoggingOut: Boolean = false,
    val error: String? = null,
    val gameHistory: List<GameEntity> = emptyList()
)

class HomeViewModel(
    private val repository: AuthRepository,
    private val gameRepository: GameRepository,
    private val remoteGameRepository: RemoteGameRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())

    val state: StateFlow<HomeUiState> = _state.asStateFlow()
    private var localHistory: List<GameEntity> = emptyList()
    private var remoteHistory: List<GameEntity> = emptyList()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            val username = repository.getUsername()
                ?.takeIf { it.isNotBlank() }
            _state.update {
                it.copy(username = username.orEmpty(),
                    error = null
                )
            }
            val ownerId = username ?: repository.getGuestId() ?: "local-user"

            launch {
                gameRepository
                    .observeFinished(ownerId)
                    .collect { games ->
                        localHistory = games
                        publishHistory()
                    }
            }
            if (username != null) {
                launch {
                    try {
                        remoteHistory = remoteGameRepository.getGameHistory(username)
                        _state.update {
                            it.copy(error = null)
                        }
                        publishHistory()
                    } catch (e: Exception) {
                        _state.update {
                            it.copy(
                                error = e.message ?: "Unable to load server game history."
                            )
                        }
                        publishHistory()
                    }
                }
            } else {
                remoteHistory = emptyList()
                publishHistory()
            }
        }
    }

    private fun publishHistory() {
        val combinedHistory = (localHistory + remoteHistory)
            .distinctBy { it.id }
            .sortedByDescending { it.endedAt ?: it.startedAt }

        _state.update {
            it.copy(gameHistory = combinedHistory)
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
                        error = e.message ?: "Unable to log out."
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
