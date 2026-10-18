package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JoinTournamentRequest(
    @SerialName("invite_code") val inviteCode: String? = null
)