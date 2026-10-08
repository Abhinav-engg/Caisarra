package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InvitePreviewResponse(
    val code: String,
    val creator: InviteCreatorDto,
    @SerialName("time_control_minutes") val timeControlMinutes: Int,
    val color: String
)