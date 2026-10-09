package com.abhinav.caisarra.presentation.game.viewmodel

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

data class GameHistoryUiState(
    val games: List<GameEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class GameHistoryViewModel(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
            GameHistoryUiState()
        )

    val state: StateFlow<GameHistoryUiState> = _state.asStateFlow()

    init {
        observeHistory()
    }

    private fun observeHistory() {

        viewModelScope.launch {

            val ownerId =
                authRepository.getUsername()
                    ?: authRepository.getGuestId()
                    ?: "local-user"

            gameRepository
                .observeFinished(ownerId)
                .collect { games ->

                    _state.update {
                        it.copy(
                            games = games,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
}