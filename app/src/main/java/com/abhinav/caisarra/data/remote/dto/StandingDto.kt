package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StandingDto(
    val rank: Int,
    @SerialName("player_id") val playerId: Long,
    val username: String,
    val score: Double = 0.0,
    val buchholz: Double = 0.0,
    val wins: Int = 0,
    val draws: Int = 0,
    val losses: Int = 0,
    @SerialName("games_played") val gamesPlayed: Int = 0
)