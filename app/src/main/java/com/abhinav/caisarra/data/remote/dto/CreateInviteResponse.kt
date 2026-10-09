package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateInviteResponse(
    @SerialName("invite_code") val inviteCode: String,
    @SerialName("game_id") val gameId: String,
    val link: String,
    @SerialName("time_control_minutes") val timeControlMinutes: Int,
    @SerialName("increment_seconds") val incrementSeconds: Int,
    val color: String,
    val status: String
)