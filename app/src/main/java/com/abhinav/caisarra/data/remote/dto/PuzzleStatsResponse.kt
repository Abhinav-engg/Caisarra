package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PuzzleStatsResponse(
    val rating: Int = 0,
    @SerialName("best_rating") val bestRating: Int = 0,
    @SerialName("best_rating_at") val bestRatingAt: String? = null,
    @SerialName("puzzles_solved") val puzzlesSolved: Int = 0,
    @SerialName("puzzles_failed") val puzzlesFailed: Int = 0,
    @SerialName("win_rate") val winRate: Double = 0.0,
    @SerialName("current_streak") val currentStreak: Int = 0,
    @SerialName("max_streak") val maxStreak: Int = 0
)
