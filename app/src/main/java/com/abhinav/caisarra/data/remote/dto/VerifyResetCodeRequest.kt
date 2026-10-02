package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyResetCodeRequest(
    @SerialName("email") val email: String,
    @SerialName("code") val code: String
)