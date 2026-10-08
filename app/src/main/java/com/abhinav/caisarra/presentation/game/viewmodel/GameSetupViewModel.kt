package com.abhinav.caisarra.presentation.game.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

const val MIN_CUSTOM_MINUTES = 1
const val MAX_CUSTOM_MINUTES = 180

val PRESET_TIMES = listOf(3, 5, 10, 15)

data class GameSetupUiState(
    val whitePlayerName: String = "Player 1",
    val blackPlayerName: String = "Player 2",
    val timeMinutes: Int = 10,
    val isCustomTime: Boolean = false,
    val customTimeText: String = "",
    val flipBoard: Boolean = false,
    val undoEnabled: Boolean = true,
    val error: String? = null
)

class GameSetupViewModel : ViewModel() {

    private val _state = MutableStateFlow(GameSetupUiState())
    val state = _state.asStateFlow()

    fun setWhitePlayerName(name: String) {
        _state.update { it.copy(whitePlayerName = name, error = null) }
    }

    fun setBlackPlayerName(name: String) {
        _state.update { it.copy(blackPlayerName = name, error = null) }
    }

    fun setTime(minutes: Int) {
        _state.update { it.copy(timeMinutes = minutes, isCustomTime = false, error = null) }
    }

    fun selectCustomTime() {
        _state.update {
            it.copy(
                isCustomTime = true,
                customTimeText = it.customTimeText.ifBlank { it.timeMinutes.toString() },
                error = null
            )
        }
    }

    fun setCustomTimeText(raw: String) {
        val digits = raw.filter { c -> c.isDigit() }.take(3)
        val minutes = digits.toIntOrNull()

        _state.update {
            it.copy(
                isCustomTime = true,
                customTimeText = digits,
                timeMinutes =
                    if (minutes != null && minutes in MIN_CUSTOM_MINUTES..MAX_CUSTOM_MINUTES) minutes
                    else it.timeMinutes,
                error = null
            )
        }
    }

    fun setFlipBoard(enabled: Boolean) {
        _state.update { it.copy(flipBoard = enabled) }
    }

    fun setUndoEnabled(enabled: Boolean) {
        _state.update { it.copy(undoEnabled = enabled) }
    }

    fun validate(): Boolean {
        val current = _state.value

        if (current.whitePlayerName.isBlank() || current.blackPlayerName.isBlank()) {
            _state.update { it.copy(error = "Player names cannot be empty.") }
            return false
        }

        if (current.isCustomTime) {
            val minutes = current.customTimeText.toIntOrNull()
            if (minutes == null || minutes !in MIN_CUSTOM_MINUTES..MAX_CUSTOM_MINUTES) {
                _state.update {
                    it.copy(error = "Enter a time between $MIN_CUSTOM_MINUTES and $MAX_CUSTOM_MINUTES minutes.")
                }
                return false
            }
        }

        return true
    }
}