package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyPuzzleSolutionRequest(
    @SerialName("puzzle_id") val puzzleId: String
)
