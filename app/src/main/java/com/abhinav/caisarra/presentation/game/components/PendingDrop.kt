package com.abhinav.caisarra.presentation.game.components

import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.Square

data class PendingDrop(
    val piece: Piece,
    val from: Square,
    val to: Square,
    val boardAtDrop: Map<Square, Piece>
)
