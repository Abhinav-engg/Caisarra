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
                is GameSetupEvent.TimeChanged -> it.copy(minutes = event.minutes)
                is GameSetupEvent.BonusChanged -> it.copy(bonusSeconds = event.seconds)
                is GameSetupEvent.ControlSelected -> it.copy(
                    selectedControl = event.label,
                    minutes = event.minutes
                )
            }
        }
    }
}
