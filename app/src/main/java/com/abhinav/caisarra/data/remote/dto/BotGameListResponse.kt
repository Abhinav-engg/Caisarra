package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BotGameListResponse(
    val games: List<BotGame> = emptyList()
)
