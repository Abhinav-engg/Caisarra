package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JoinGameResponse(
    @SerialName("game_id") val gameId: String
)