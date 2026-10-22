package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyPuzzleAttemptResponse(
    val correct: Boolean? = null,
    val solved: Boolean? = null,
    @SerialName("attempts_left") val attemptsLeft: Int? = null,
    @SerialName("rating_change") val ratingChange: Int? = null,
    @SerialName("user_rating_before") val userRatingBefore: Int? = null,
    @SerialName("user_rating_after") val userRatingAfter: Int? = null,
    @SerialName("current_streak") val currentStreak: Int? = null,
    @SerialName("next_move") val nextMove: String? = null,
    val message: String? = null
)
