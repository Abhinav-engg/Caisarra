package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BotGame(
    val id: String,
    @SerialName("player_id") val playerId: Long,
    @SerialName("player_color") val playerColor: String,
    @SerialName("bot_id") val botId: String? = null,
    @SerialName("bot_level") val botLevel: String,
    @SerialName("bot_rating") val botRating: Int,
    val position: String,
    val moves: List<String> = emptyList(),
    val status: String,
    val result: String? = null,
    @SerialName("end_reason") val endReason: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("ended_at") val endedAt: String? = null,
    @SerialName("next_bot") val nextBot: NextBot? = null
)
