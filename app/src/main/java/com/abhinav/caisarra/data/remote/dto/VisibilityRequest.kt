package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VisibilityRequest(
    val enabled: Boolean
)