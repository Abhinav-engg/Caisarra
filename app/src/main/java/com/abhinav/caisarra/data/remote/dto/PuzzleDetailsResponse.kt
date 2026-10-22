package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PuzzleDetailsResponse(
    @SerialName("puzzle_id") val puzzleId: String,
    val fen: String,
    @SerialName("initial_move") val initialMove: String,
    val rating: Int,
    val themes: List<String> = emptyList(),
    @SerialName("solution_moves") val solutionMoves: List<String> = emptyList()
)
