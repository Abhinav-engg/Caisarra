package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StandingsResponse(
    @SerialName("tournament_id") val tournamentId: String,
    val standings: List<StandingDto> = emptyList()
)