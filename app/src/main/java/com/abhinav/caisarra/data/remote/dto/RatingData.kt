package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RatingData(
    val level: String,
    val rating: Int
)