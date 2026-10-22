package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PuzzleHistoryItemDto(
    val id: Long = 0L,
    @SerialName("puzzle_id") val puzzleId: String,
    val solved: Boolean = false,
    @SerialName("attempted_moves") val attemptedMoves: List<String> = emptyList(),
    @SerialName("solution_moves") val solutionMoves: List<String> = emptyList(),
    @SerialName("user_rating_before") val userRatingBefore: Int? = null,
    @SerialName("user_rating_after") val userRatingAfter: Int? = null,
    @SerialName("rating_change") val ratingChange: Int? = null,
    @SerialName("time_taken_ms") val timeTakenMs: Long? = null,
    @SerialName("created_at") val createdAt: String? = null
)
