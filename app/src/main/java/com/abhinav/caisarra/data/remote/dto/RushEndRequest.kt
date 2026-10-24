package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RushEndRequest(
    @SerialName("session_id") val sessionId: String
)
