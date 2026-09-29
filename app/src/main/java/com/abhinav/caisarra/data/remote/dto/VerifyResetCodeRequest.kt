package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VerifyResetCodeRequest(
    val email: String,
    val code: String
)