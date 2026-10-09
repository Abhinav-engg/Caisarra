package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InvitePreviewResponse(
    val inviter: InviterDto,
    @SerialName("game_id") val gameId: String,
    @SerialName("time_control_minutes") val timeControlMinutes: Int,
    @SerialName("increment_seconds") val incrementSeconds: Int,
    val color: String,
    val status: String
)