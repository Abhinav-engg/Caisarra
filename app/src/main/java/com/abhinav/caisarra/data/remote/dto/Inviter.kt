package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class InviterDto(
    val id: Long,
    val username: String
)