package com.abhinav.caisarra.features.minimates.presentation

import com.abhinav.caisarra.features.minimates.domain.MiniMatesLevel
import com.abhinav.caisarra.features.minimates.domain.MiniMatesMove
import com.abhinav.caisarra.features.minimates.domain.MiniMatesPosition
import com.abhinav.caisarra.presentation.game.model.Square

data class MiniMatesUiState(
    val level: MiniMatesLevel? = null,
    val position: MiniMatesPosition? = null,
    val selected: Square? = null,
    val targets: List<Square> = emptyList(),
    val lastMove: MiniMatesMove? = null,
    val movesLeft: Int = 0,
    val status: MiniMatesStatus = MiniMatesStatus.PLAYING
)
