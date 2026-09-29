package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyResetCodeResponse(
    val message: String,
    @SerialName("reset_token") val resetToken: String
)