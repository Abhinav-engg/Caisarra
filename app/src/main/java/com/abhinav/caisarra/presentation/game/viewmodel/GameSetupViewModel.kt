package com.abhinav.caisarra.presentation.game.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GameSetupUiState(
    val whitePlayerName: String = "Player 1",
    val blackPlayerName: String = "Player 2",
    val timeMinutes: Int = 10,
    val error: String? = null
)

class GameSetupViewModel : ViewModel() {

    private val _state =
        MutableStateFlow(
            GameSetupUiState()
        )

    val state =
        _state.asStateFlow()

    fun setWhitePlayerName(name: String) {
        _state.update {
            it.copy(
                whitePlayerName = name,
                error = null
            )
        }
    }

    fun setBlackPlayerName(name: String) {
        _state.update {
            it.copy(
                blackPlayerName = name,
                error = null
            )
        }
    }

    fun setTime(minutes: Int) {
        _state.update {
            it.copy(
                timeMinutes = minutes,
                error = null
            )
        }
    }

    fun validate(): Boolean {

        val current =
            _state.value

        if (
            current.whitePlayerName
                .isBlank() ||
            current.blackPlayerName
                .isBlank()
        ) {
            _state.update {
                it.copy(
                    error = "Player names cannot be empty."
                )
            }

            return false
        }

        return true
    }
}