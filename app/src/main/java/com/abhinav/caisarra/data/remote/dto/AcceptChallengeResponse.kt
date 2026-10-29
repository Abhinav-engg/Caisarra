package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcceptChallengeResponse(
    @SerialName("game_id") val gameId: String,
    val color: String? = null,
    val status: String? = null
)
