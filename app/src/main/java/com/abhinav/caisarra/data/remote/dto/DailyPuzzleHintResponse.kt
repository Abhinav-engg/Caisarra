package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyPuzzleHintResponse(
    @SerialName("hint_type") val hintType: String? = null,
    @SerialName("from_square") val fromSquare: String? = null,
    val message: String? = null,
    @SerialName("hint_used") val hintUsed: Boolean? = null
)
