package com.abhinav.caisarra.presentation.game.viewmodel

data class GameSetupUiState(
    val minutes: Float = 10f,
    val bonusSeconds: Float = 0f,
    val selectedControl: String = "10 min"
)
