package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EndTournamentResponse(
    val message: String = "",
    @SerialName("tournament_id") val tournamentId: String,
    val status: String,
    @SerialName("winner_id") val winnerId: Long? = null
)
