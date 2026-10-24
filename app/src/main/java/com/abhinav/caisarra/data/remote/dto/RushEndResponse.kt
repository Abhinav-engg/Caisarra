package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RushEndResponse(
    @SerialName("session_id") val sessionId: String,
    val mode: String,
    @SerialName("final_score") val finalScore: Int = 0,
    @SerialName("total_strikes") val totalStrikes: Int = 0,
    @SerialName("is_new_high_score") val isNewHighScore: Boolean = false,
    @SerialName("best_score") val bestScore: Int = 0,
    @SerialName("duration_sec") val durationSec: Int = 0
)
