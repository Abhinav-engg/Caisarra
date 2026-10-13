package com.abhinav.caisarra.presentation.game.viewmodel

sealed interface GameSetupEvent {
    data class TimeChanged(val minutes: Float) : GameSetupEvent
    data class BonusChanged(val seconds: Float) : GameSetupEvent
    data class ControlSelected(
        val label: String,
        val minutes: Float,
        val incrementSeconds: Float,
        val mode: GameMode
    ) : GameSetupEvent
}
