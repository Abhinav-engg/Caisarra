package com.abhinav.caisarra.features.notificationsettings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.core.network.userMessage
import com.abhinav.caisarra.features.notificationsettings.data.DevicePushRepository
import com.abhinav.caisarra.features.notificationsettings.data.NotificationPreferencesRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationSettingsViewModel(
    private val repository: NotificationPreferencesRepository,
    private val devicePush: DevicePushRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val pushEnabled = devicePush.isEnabled()
            repository.preferences()
                .onSuccess { data ->
                    val enabledTypes = data.preferences.filter { it.enabled }.map { it.type }.toSet()
                    _uiState.update { it.copy(loading = false, pushEnabled = pushEnabled, enabledTypes = enabledTypes) }
                }
                .onFailure { e -> _uiState.update { it.copy(loading = false, error = e.userMessage()) } }
        }
    }

    fun setPushEnabled(enabled: Boolean) {
        if (_uiState.value.pushUpdating) return
        _uiState.update { it.copy(pushEnabled = enabled, pushUpdating = true, updateError = null) }
        viewModelScope.launch {
            withContext(NonCancellable) {
                devicePush.setEnabled(enabled)
                    .onSuccess { _uiState.update { it.copy(pushUpdating = false) } }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                pushEnabled = !enabled,
                                pushUpdating = false,
                                updateError = e.userMessage("Couldn't update push notifications. Please try again.")
                            )
                        }
                    }
            }
        }
    }

    fun setCategoryEnabled(category: NotificationCategory, enabled: Boolean) {
        val current = _uiState.value
        if (category in current.updatingCategories) return
        val types = category.types.filter { (it in current.enabledTypes) != enabled }
        _uiState.update {
            it.copy(
                enabledTypes = it.enabledTypes.withTypes(types, enabled),
                updatingCategories = it.updatingCategories + category,
                updateError = null
            )
        }
        viewModelScope.launch {
            withContext(NonCancellable) { save(category, types, enabled) }
        }
    }

    fun clearUpdateError() = _uiState.update { it.copy(updateError = null) }

    private suspend fun save(category: NotificationCategory, types: List<String>, enabled: Boolean) {
        types.forEachIndexed { index, type ->
            repository.update(type, enabled).onFailure { e ->
                _uiState.update {
                    it.copy(
                        enabledTypes = it.enabledTypes.withTypes(types.drop(index), !enabled),
                        updatingCategories = it.updatingCategories - category,
                        updateError = e.userMessage("Couldn't save ${category.title}. Please try again.")
                    )
                }
                return
            }
        }
        _uiState.update { it.copy(updatingCategories = it.updatingCategories - category) }
    }

    private fun Set<String>.withTypes(types: List<String>, enabled: Boolean) =
        if (enabled) this + types else this - types.toSet()
}
