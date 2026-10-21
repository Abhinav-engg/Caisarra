package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBotGameRequest(
    @SerialName("bot_id") val botId: String? = null,
    val rating: Int? = null,
    val color: String? = null
)
