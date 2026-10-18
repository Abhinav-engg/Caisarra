package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PairingDto(
    val id: String,
    @SerialName("round_id") val roundId: String = "",
    @SerialName("round_number") val roundNumber: Int = 0,
    @SerialName("white_player_id") val whitePlayerId: Long? = null,
    @SerialName("white_username") val whiteUsername: String? = null,
    @SerialName("black_player_id") val blackPlayerId: Long? = null,
    @SerialName("black_username") val blackUsername: String? = null,
    @SerialName("game_id") val gameId: String? = null,
    val result: String? = null,
    val status: String = "",
    @SerialName("is_bye") val isBye: Boolean = false
)