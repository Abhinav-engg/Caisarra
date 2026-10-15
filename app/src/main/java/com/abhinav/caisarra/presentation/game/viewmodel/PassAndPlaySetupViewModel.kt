package com.abhinav.caisarra.presentation.game.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PassAndPlaySetupUiState(
    val clockEnabled: Boolean = false,
    val minutes: Float = 10f
)

class PassAndPlaySetupViewModel : ViewModel() {
    private val _state = MutableStateFlow(PassAndPlaySetupUiState())
    val state = _state.asStateFlow()

    fun onEvent(event: PassAndPlaySetupEvent) {
        _state.update { current ->
            when (event) {
                is PassAndPlaySetupEvent.ClockChanged -> current.copy(
                    clockEnabled = event.enabled
                )

                is PassAndPlaySetupEvent.TimeChanged -> current.copy(
                    minutes = event.minutes.coerceIn(1f, 180f)
                )

                is PassAndPlaySetupEvent.PresetSelected -> current.copy(
                    minutes = event.minutes.toFloat()
                )
            }
        }
    }
}
