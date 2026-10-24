package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyPuzzleSolutionResponse(
    val moves: List<String> = emptyList(),
    @SerialName("rating_change") val ratingChange: Int? = 0,
    @SerialName("solution_viewed") val solutionViewed: Boolean? = false,
    val message: String? = null
)
