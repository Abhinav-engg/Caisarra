package com.abhinav.caisarra.presentation.game.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.dto.BotGame
import com.abhinav.caisarra.data.repository.BotGameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BotHistoryUiState(
    val isLoading: Boolean = false,
    val games: List<BotGame> = emptyList(),
    val errorMessage: String? = null
)

class BotHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BotGameRepository.get(application)

    private val _state = MutableStateFlow(BotHistoryUiState())
    val state: StateFlow<BotHistoryUiState> = _state.asStateFlow()

    init {
        loadGames()
    }

    fun loadGames() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.getGames()
                .onSuccess { games ->
                    _state.update { it.copy(isLoading = false, games = games) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }
}
