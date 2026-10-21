package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BotListResponse(
    val bots: List<Bot> = emptyList()
)
