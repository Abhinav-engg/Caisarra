package com.abhinav.caisarra.features.notificationsettings.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationPreferenceDto(
    val type: String,
    val enabled: Boolean
)

@Serializable
data class NotificationPreferencesDto(
    val preferences: List<NotificationPreferenceDto>
)
