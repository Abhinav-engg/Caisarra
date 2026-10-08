package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class InviteCreatorDto(
    val id: Long,
    val username: String,
    val rating: Int
)