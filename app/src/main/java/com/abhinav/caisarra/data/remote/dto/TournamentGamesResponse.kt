package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TournamentGamesResponse(
    @SerialName("tournament_id") val tournamentId: String,
    val games: List<TournamentGameDto> = emptyList()
)