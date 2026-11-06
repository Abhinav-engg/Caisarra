package com.abhinav.caisarra.features.minimates.domain

import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.Square

data class MiniMatesPosition(
    val size: Int,
    val pieces: Map<Square, Piece>
)
