package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateInviteResponse(
    val code: String,
    @SerialName("creator_id") val creatorId: Long,
    @SerialName("time_control_minutes") val timeControlMinutes: Int,
    val color: String
)