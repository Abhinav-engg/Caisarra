package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LeaveTournamentResponse(
    val message: String = "",
    @SerialName("tournament_id") val tournamentId: String,
    @SerialName("user_id") val userId: Long
)