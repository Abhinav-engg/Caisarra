package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateBotGameRequest(
    val level: String,
    val rating: Int? = null,
    val color: String
)