package com.abhinav.caisarra.features.notificationsettings.presentation

data class NotificationSettingsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val updateError: String? = null,
    val pushEnabled: Boolean = true,
    val pushUpdating: Boolean = false,
    val enabledTypes: Set<String> = emptySet(),
    val updatingCategories: Set<NotificationCategory> = emptySet()
) {
    fun isEnabled(category: NotificationCategory) = category.types.any { it in enabledTypes }
}
