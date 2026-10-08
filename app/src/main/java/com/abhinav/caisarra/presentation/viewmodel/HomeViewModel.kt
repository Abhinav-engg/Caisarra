package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(

    val username: String = "",

    val isLoggingOut: Boolean = false,

    val error: String? = null,

    val gameHistory:
    List<GameEntity> = emptyList()
)

class HomeViewModel(
    private val repository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            HomeUiState()
        )

    val state:
            StateFlow<HomeUiState> =
        _state.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {

        viewModelScope.launch {

            val username =
                repository.getUsername()

            val ownerId =
                username
                    ?: repository.getGuestId()
                    ?: "local-user"

            _state.update {
                it.copy(
                    username =
                        username.orEmpty()
                )
            }

            gameRepository
                .observeFinished(ownerId)
                .collect { games ->

                    _state.update {
                        it.copy(
                            gameHistory = games
                        )
                    }
                }
        }
    }

    fun logout(
        onSuccess: () -> Unit
    ) {

        if (_state.value.isLoggingOut) {
            return
        }

        _state.update {
            it.copy(
                isLoggingOut = true,
                error = null
            )
        }

        viewModelScope.launch {

            try {

                repository.logout()

                _state.update {
                    it.copy(
                        isLoggingOut = false
                    )
                }

                onSuccess()

            } catch (e: Exception) {

                _state.update {
                    it.copy(
                        isLoggingOut = false,
                        error =
                            e.message
                                ?: "Unable to log out"
                    )
                }
            }
        }
    }

    fun clearError() {

        _state.update {
            it.copy(
                error = null
            )
        }
    }
}