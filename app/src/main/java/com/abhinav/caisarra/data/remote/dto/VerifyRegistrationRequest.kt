package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VerifyRegistrationRequest(
    val email: String,
    val code: String
)