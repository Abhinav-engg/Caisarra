package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RushPuzzleData(
    @SerialName("puzzle_id") val puzzleId: String,
    val fen: String,
    @SerialName("initial_move") val initialMove: String,
    val rating: Int,
    val themes: List<String> = emptyList(),
    @SerialName("total_moves") val totalMoves: Int? = null,
    @SerialName("player_moves_count") val playerMovesCount: Int? = null
)
