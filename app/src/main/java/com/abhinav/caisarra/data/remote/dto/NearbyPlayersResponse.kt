package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NearbyPlayersResponse(
    @SerialName("radius_km") val radiusKm: Int,
    val players: List<NearbyPlayer> = emptyList()
)