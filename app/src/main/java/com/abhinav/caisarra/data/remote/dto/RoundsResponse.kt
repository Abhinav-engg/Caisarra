package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoundsResponse(
    @SerialName("tournament_id") val tournamentId: String,
    val rounds: List<RoundDto> = emptyList()
)