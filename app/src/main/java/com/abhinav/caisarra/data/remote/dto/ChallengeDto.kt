package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChallengeDto(
    val code: String,
    @SerialName("game_id") val gameId: String,
    val link: String? = null,
    @SerialName("challenger_id") val challengerId: Long,
    @SerialName("challenger_username") val challengerUsername: String,
    @SerialName("opponent_id") val opponentId: Long,
    @SerialName("opponent_username") val opponentUsername: String,
    @SerialName("challenger_color") val challengerColor: String = "random",
    @SerialName("time_control_minutes") val timeControlMinutes: Int = 10,
    @SerialName("increment_seconds") val incrementSeconds: Int = 0,
    val rated: Boolean = true,
    val status: String = "pending"
)
