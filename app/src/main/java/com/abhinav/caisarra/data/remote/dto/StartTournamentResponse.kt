package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartTournamentResponse(
    val message: String = "",
    @SerialName("tournament_id") val tournamentId: String,
    val status: String,
    @SerialName("current_round") val currentRound: Int = 0
)