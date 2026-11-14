package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NearbyPlayer(
    @SerialName("user_id") val userId: Int,
    val username: String,
    val rating: Int,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("distance_km") val distanceKm: Int,
    val active: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null
)