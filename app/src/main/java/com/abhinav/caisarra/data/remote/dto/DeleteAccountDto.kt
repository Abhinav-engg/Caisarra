package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteAccountRequest(
    @SerialName("password")
    val password: String
)

@Serializable
data class DeleteAccountResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String? = null,

    @SerialName("error")
    val error: String? = null
)
