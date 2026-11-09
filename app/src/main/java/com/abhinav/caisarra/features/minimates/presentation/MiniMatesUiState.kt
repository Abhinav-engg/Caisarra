package com.abhinav.caisarra.features.minimates.presentation

import com.abhinav.caisarra.features.minimates.data.MiniMatesLevelDto
import com.abhinav.caisarra.features.minimates.data.MiniMatesOverviewDto
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.Square

data class MiniMatesUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val overview: MiniMatesOverviewDto? = null,
    val level: MiniMatesLevelDto? = null,
    val board: Map<Square, Piece> = emptyMap(),
    val selected: Square? = null,
    val targets: List<Square> = emptyList(),
    val lastFrom: Square? = null,
    val lastTo: Square? = null,
    val whiteInCheck: Boolean = false,
    val blackInCheck: Boolean = false,
    val moveIndex: Int = 0,
    val stars: Int = 0,
    val nextLevel: Int? = null,
    val note: String? = null,
    val status: MiniMatesStatus = MiniMatesStatus.PLAYING
)
