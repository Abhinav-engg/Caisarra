package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    val message: String,
    val username: String
)