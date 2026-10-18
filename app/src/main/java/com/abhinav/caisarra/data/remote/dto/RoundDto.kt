package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoundDto(
    val id: String,
    @SerialName("round_number") val roundNumber: Int,
    val status: String = "",
    val pairings: List<PairingDto> = emptyList()
)