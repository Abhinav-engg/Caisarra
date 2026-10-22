package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyPuzzleResponse(
    @SerialName("puzzle_id") val puzzleId: String,
    val fen: String,
    @SerialName("initial_move") val initialMove: String,
    val rating: Int,
    val themes: List<String> = emptyList(),
    @SerialName("attempts_left") val attemptsLeft: Int,
    @SerialName("hint_available") val hintAvailable: Boolean = false,
    @SerialName("solution_viewed") val solutionViewed: Boolean = false,
    val solved: Boolean = false,
    @SerialName("current_streak") val currentStreak: Int = 0,
    @SerialName("player_rating") val playerRating: Int? = null,
    @SerialName("attempted_moves") val attemptedMoves: List<String> = emptyList(),
    @SerialName("total_moves") val totalMoves: Int = 0,
    @SerialName("player_moves_count") val playerMovesCount: Int = 0
)
