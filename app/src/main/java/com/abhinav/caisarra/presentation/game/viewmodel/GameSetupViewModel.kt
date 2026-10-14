
package com.abhinav.caisarra.presentation.game.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameSetupViewModel : ViewModel() {

    private val _state = MutableStateFlow(GameSetupUiState())
    val state = _state.asStateFlow()

    fun onEvent(event: GameSetupEvent) {
        _state.update { current ->
            when (event) {
                is GameSetupEvent.TimeChanged -> current.copy(
                    minutes = event.minutes,
                    selectedControl = timeLabel(
                        event.minutes,
                        current.bonusSeconds
                    ),
                    mode = modeFor(event.minutes)
                )

                is GameSetupEvent.BonusChanged -> current.copy(
                    bonusSeconds = event.seconds,
                    selectedControl = timeLabel(
                        current.minutes,
                        event.seconds
                    )
                )

                is GameSetupEvent.ControlSelected -> current.copy(
                    selectedControl = event.label,
                    minutes = event.minutes,
                    bonusSeconds = event.incrementSeconds,
                    mode = event.mode
                )
            }
        }
    }

    private fun modeFor(minutes: Float): GameMode = when {
        minutes >= 1440f -> GameMode.DAILY
        minutes < 3f -> GameMode.BULLET
        minutes < 10f -> GameMode.BLITZ
        else -> GameMode.RAPID
    }

    private fun timeLabel(
        minutes: Float,
        incrementSeconds: Float
    ): String {
        val totalMinutes = minutes.toInt()
        val increment = incrementSeconds.toInt()

        val duration = when (totalMinutes) {
            1440 -> "1 day"
            2880 -> "2 days"
            4320 -> "3 days"
            7200 -> "5 days"
            10080 -> "7 days"
            20160 -> "14 days"
            else -> "$totalMinutes min"
        }

        return if (increment == 0) {
            duration
        } else {
            "$duration + $increment"
        }
    }
}
