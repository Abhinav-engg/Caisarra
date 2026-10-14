package com.abhinav.caisarra.presentation.game.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameSetupViewModel : ViewModel() {
    private val _state = MutableStateFlow(GameSetupUiState())
    val state = _state.asStateFlow()

    fun onEvent(event: GameSetupEvent) {
        _state.update {
            when (event) {
                is GameSetupEvent.TimeChanged -> it.copy(
                    minutes = event.minutes,
                    selectedControl = timeLabel(event.minutes, it.bonusSeconds),
                    mode = modeFor(event.minutes)
                )
                is GameSetupEvent.BonusChanged -> it.copy(
                    bonusSeconds = event.seconds,
                    selectedControl = timeLabel(it.minutes, event.seconds)
                )
                is GameSetupEvent.ControlSelected -> it.copy(
                    selectedControl = event.label,
                    minutes = event.minutes,
                    bonusSeconds = event.incrementSeconds,
                    mode = event.mode
                )
            }
        }
    }

    private fun modeFor(minutes: Float): GameMode = when {
        minutes < 3f -> GameMode.BULLET
        minutes < 10f -> GameMode.BLITZ
        else -> GameMode.RAPID
    }

    private fun timeLabel(minutes: Float, incrementSeconds: Float): String {
        val time = minutes.toInt()
        val increment = incrementSeconds.toInt()
        return if (increment == 0) "$time min" else "$time + $increment"
    }
}
