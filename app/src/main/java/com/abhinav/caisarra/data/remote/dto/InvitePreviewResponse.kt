package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InvitePreviewResponse(
    val code: String? = null,
    @SerialName("game_id") val gameId: String,
    val inviter: InviterDto? = null,
    @SerialName("creator_id") val creatorId: Long? = null,
    @SerialName("creator_username") val creatorUsername: String? = null,
    @SerialName("time_control_minutes") val timeControlMinutes: Int = 10,
    @SerialName("increment_seconds") val incrementSeconds: Int = 0,
    val color: String = "random",
    val rated: Boolean = true,
    val status: String = "waiting"
) {
    val effectiveInviterId: Long?
        get() = inviter?.id ?: creatorId

    val effectiveInviterUsername: String
        get() = inviter?.username ?: creatorUsername ?: "Unknown"
}
