package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NearbyPlayer(
    @SerialName("user_id") val userId: Int,
    val username: String,
    val rating: Int,
    @SerialName("distance_km") val distanceKm: Int,
    val active: Boolean
)