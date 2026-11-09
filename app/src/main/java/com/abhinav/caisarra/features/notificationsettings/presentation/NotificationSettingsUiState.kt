package com.abhinav.caisarra.features.notificationsettings.presentation

data class NotificationSettingsUiState(
    val pushEnabled: Boolean = true,
    val mutedCategories: Set<NotificationCategory> = emptySet()
)
