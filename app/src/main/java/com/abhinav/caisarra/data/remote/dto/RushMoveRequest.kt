package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RushMoveRequest(
    @SerialName("session_id") val sessionId: String,
    @SerialName("puzzle_id") val puzzleId: String,
    val move: String
)
