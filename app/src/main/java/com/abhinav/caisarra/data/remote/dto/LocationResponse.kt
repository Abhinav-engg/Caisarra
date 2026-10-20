package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationResponse(
    val message: String,
    @SerialName("nearby_enabled") val nearbyEnabled: Boolean
)