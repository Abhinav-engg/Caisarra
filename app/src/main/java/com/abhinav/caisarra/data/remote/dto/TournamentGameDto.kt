package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TournamentGameDto(
    @SerialName("game_id") val gameId: String,
    @SerialName("pairing_id") val pairingId: String = "",
    @SerialName("round_number") val roundNumber: Int = 0,
    @SerialName("white_player_id") val whitePlayerId: Long? = null,
    @SerialName("white_username") val whiteUsername: String? = null,
    @SerialName("black_player_id") val blackPlayerId: Long? = null,
    @SerialName("black_username") val blackUsername: String? = null,
    val result: String? = null,
    val status: String = ""
)