package com.abhinav.caisarra.presentation.game.model

data class PieceMoveAnimationEvent(
    val id: Long,
    val piece: Piece,
    val from: Square,
    val to: Square
)