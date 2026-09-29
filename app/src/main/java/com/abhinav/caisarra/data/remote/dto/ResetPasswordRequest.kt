package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(
    @SerialName("reset_token") val resetToken: String,
    @SerialName("new_password") val newPassword: String,
    @SerialName("confirm_password") val confirmPassword: String
)