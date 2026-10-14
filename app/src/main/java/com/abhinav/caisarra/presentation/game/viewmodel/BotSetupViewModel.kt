package com.abhinav.caisarra.presentation.game.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.repository.BotGameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BotSetupUiState(
    val level: String = "easy",
    val customRating: Int = 1800,
    val color: String = "white",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val createdGameId: String? = null
)

class BotSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BotGameRepository.get(application)

    private val _state = MutableStateFlow(BotSetupUiState())
    val state: StateFlow<BotSetupUiState> = _state.asStateFlow()

    fun onLevelSelected(level: String) {
        _state.update { it.copy(level = level, errorMessage = null) }
    }

    fun onCustomRatingChanged(rating: Int) {
        _state.update { it.copy(customRating = rating.coerceIn(1000, 3000), errorMessage = null) }
    }

    fun onColorSelected(color: String) {
        _state.update { it.copy(color = color, errorMessage = null) }
    }

    fun startGame() {
        val current = _state.value
        if (current.isLoading) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val rating = if (current.level == "custom") current.customRating else null
            repository.createGame(current.level, rating, current.color)
                .onSuccess { game ->
                    _state.update { it.copy(isLoading = false, createdGameId = game.id) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun onGameOpened() {
        _state.update { it.copy(createdGameId = null) }
    }
}