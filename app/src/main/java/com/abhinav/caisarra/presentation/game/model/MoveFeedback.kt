package com.abhinav.caisarra.presentation.game.model

data class MoveFeedback(
    val id: Long,
    val square: Square,
    val isCorrect: Boolean
)
