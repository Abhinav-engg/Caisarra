package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginData(
    val username: String,
    @SerialName("needs_rating") val needsRating: Boolean
)