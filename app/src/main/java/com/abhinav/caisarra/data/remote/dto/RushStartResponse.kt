package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RushStartResponse(
    @SerialName("session_id") val sessionId: String,
    val mode: String,
    @SerialName("time_limit_sec") val timeLimitSec: Int = 180,
    val score: Int = 0,
    val strikes: Int = 0,
    @SerialName("strikes_left") val strikesLeft: Int = 3,
    val puzzle: RushPuzzleData
)
