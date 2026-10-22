package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RushStartRequest(
    val mode: String
)
