package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTournamentRequest(
    val name: String,
    val description: String,
    val format: String,
    @SerialName("time_control") val timeControl: String,
    @SerialName("min_players") val minPlayers: Int,
    @SerialName("max_players") val maxPlayers: Int,
    val visibility: String,
    @SerialName("total_rounds") val totalRounds: Int,
    @SerialName("start_at") val startAt: String?
)