package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyPuzzleAttemptRequest(
    @SerialName("puzzle_id") val puzzleId: String,
    val move: String,
    @SerialName("time_taken_ms") val timeTakenMs: Long
)
