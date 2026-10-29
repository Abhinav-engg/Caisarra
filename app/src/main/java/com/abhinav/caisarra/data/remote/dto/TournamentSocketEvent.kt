package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class TournamentSocketEvent {

    @Serializable
    @SerialName("tournament_round_started")
    data class RoundStarted(val round: Int) : TournamentSocketEvent()

    @Serializable
    @SerialName("tournament_standings_updated")
    data object StandingsUpdated : TournamentSocketEvent()

    @Serializable
    @SerialName("tournament_completed")
    data class Completed(@SerialName("winner_id") val winnerId: Long? = null) : TournamentSocketEvent()

    @Serializable
    @SerialName("tournament_cancelled")
    data class Cancelled(@SerialName("winner_id") val winnerId: Long? = null) : TournamentSocketEvent()
}