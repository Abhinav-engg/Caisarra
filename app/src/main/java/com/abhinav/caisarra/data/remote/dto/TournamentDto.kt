package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TournamentDto(
    val id: String,
    val name: String,
    val description: String = "",
    val format: String,
    @SerialName("time_control") val timeControl: String,
    val players: Int? = null,
    @SerialName("min_players") val minPlayers: Int,
    @SerialName("max_players") val maxPlayers: Int,
    val visibility: String,
    @SerialName("invite_code") val inviteCode: String = "",
    @SerialName("invite_link") val inviteLink: String = "",
    val status: String,
    @SerialName("current_round") val currentRound: Int = 0,
    @SerialName("total_rounds") val totalRounds: Int = 0,
    @SerialName("created_by") val createdBy: Long,
    @SerialName("start_at") val startAt: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)