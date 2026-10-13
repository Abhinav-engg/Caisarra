package com.abhinav.caisarra.presentation.game.viewmodel

enum class GameMode(val label: String, val ratingKey: String) {
    BULLET("Bullet", "bullet"),
    BLITZ("Blitz", "blitz"),
    RAPID("Rapid", "rapid")
}

data class GameSetupUiState(
    val minutes: Float = 10f,
    val bonusSeconds: Float = 0f,
    val selectedControl: String = "10 min",
    val mode: GameMode = GameMode.RAPID
)
