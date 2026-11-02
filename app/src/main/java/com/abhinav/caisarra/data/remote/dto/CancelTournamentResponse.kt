package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CancelTournamentResponse(
    val message: String = "",
    @SerialName("tournament_id") val tournamentId: String = "",
    val status: String = ""
)
