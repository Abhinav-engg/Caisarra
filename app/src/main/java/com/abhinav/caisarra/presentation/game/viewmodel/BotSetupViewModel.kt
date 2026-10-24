package com.abhinav.caisarra.presentation.game.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.repository.ActiveGamesLimitException
import com.abhinav.caisarra.data.repository.BotGameRepository
import com.abhinav.caisarra.data.remote.dto.Bot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BotSetupUiState(
    val isLoadingBots: Boolean = false,
    val bots: List<Bot> = emptyList(),
    val selectedBotId: String? = "easy_1",
    val isCustomRating: Boolean = false,
    val customRating: Int = 1500,
    val selectedColor: String = "white",
    val isStartingGame: Boolean = false,
    val errorMessage: String? = null,
    val hasActiveLimitReached: Boolean = false,
    val createdGameId: String? = null
)

class BotSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BotGameRepository.get(application)

    private val _state = MutableStateFlow(BotSetupUiState())
    val state: StateFlow<BotSetupUiState> = _state.asStateFlow()

    init {
        loadBots()
    }

    fun loadBots() {
        _state.update { it.copy(isLoadingBots = true, errorMessage = null) }
        viewModelScope.launch {
            repository.getBots()
                .onSuccess { botList ->
                    _state.update { current ->
                        val firstUnlocked = botList.firstOrNull { it.unlocked }?.id ?: current.selectedBotId
                        current.copy(
                            isLoadingBots = false,
                            bots = botList,
                            selectedBotId = if (current.isCustomRating) null else (current.selectedBotId ?: firstUnlocked)
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoadingBots = false, errorMessage = error.message) }
                }
        }
    }

    fun onBotSelected(bot: Bot) {
        if (!bot.unlocked) return
        _state.update {
            it.copy(
                selectedBotId = bot.id,
                isCustomRating = false,
                errorMessage = null,
                hasActiveLimitReached = false
            )
        }
    }

    fun onCustomRatingSelected() {
        _state.update {
            it.copy(
                isCustomRating = true,
                selectedBotId = null,
                errorMessage = null,
                hasActiveLimitReached = false
            )
        }
    }

    fun onCustomRatingChanged(rating: Int) {
        _state.update {
            it.copy(
                customRating = rating.coerceIn(400, 3000),
                errorMessage = null
            )
        }
    }

    fun onColorSelected(color: String) {
        _state.update { it.copy(selectedColor = color, errorMessage = null) }
    }

    fun startGame() {
        val current = _state.value
        if (current.isStartingGame) return
        _state.update { it.copy(isStartingGame = true, errorMessage = null, hasActiveLimitReached = false) }

        viewModelScope.launch {
            val botId = if (current.isCustomRating) null else (current.selectedBotId ?: "easy_1")
            val rating = if (current.isCustomRating) current.customRating else null
            val color = current.selectedColor

            repository.createGame(botId = botId, rating = rating, color = color)
                .onSuccess { game ->
                    _state.update {
                        it.copy(
                            isStartingGame = false,
                            createdGameId = game.id,
                            hasActiveLimitReached = false
                        )
                    }
                }
                .onFailure { error ->
                    val is409 = error is ActiveGamesLimitException || error.message?.contains("5 bot games") == true
                    _state.update {
                        it.copy(
                            isStartingGame = false,
                            errorMessage = error.message ?: "Failed to start game",
                            hasActiveLimitReached = is409
                        )
                    }
                }
        }
    }

    fun onGameOpened() {
        _state.update { it.copy(createdGameId = null) }
    }

    fun dismissError() {
        _state.update { it.copy(errorMessage = null, hasActiveLimitReached = false) }
    }
}
