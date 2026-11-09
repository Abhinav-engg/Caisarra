package com.abhinav.caisarra.features.notificationsettings.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificationSettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun setPushEnabled(enabled: Boolean) = _uiState.update { it.copy(pushEnabled = enabled) }

    fun setCategoryEnabled(category: NotificationCategory, enabled: Boolean) = _uiState.update {
        it.copy(mutedCategories = if (enabled) it.mutedCategories - category else it.mutedCategories + category)
    }
}
