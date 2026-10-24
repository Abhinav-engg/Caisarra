package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NextBot(
    val id: String,
    val name: String,
    val level: String,
    val order: Int,
    val rating: Int
)
