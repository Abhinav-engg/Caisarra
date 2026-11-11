package com.abhinav.caisarra.features.notificationsettings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.abhinav.caisarra.data.local.PUSH_NOTIFICATIONS_KEY
import com.abhinav.caisarra.data.repository.NotificationRepository
import kotlinx.coroutines.flow.first

class DevicePushRepository(
    private val settings: DataStore<Preferences>,
    private val notifications: NotificationRepository
) {
    suspend fun isEnabled() = settings.data.first()[PUSH_NOTIFICATIONS_KEY] != false

    suspend fun setEnabled(enabled: Boolean): Result<Unit> {
        save(enabled)
        val result = runCatching { currentFcmToken() }.fold(
            onSuccess = { if (enabled) notifications.registerDevice(it) else notifications.unregisterDevice(it) },
            onFailure = { Result.failure(it) }
        )
        if (result.isFailure) save(!enabled)
        return result
    }

    private suspend fun save(enabled: Boolean) {
        settings.edit { it[PUSH_NOTIFICATIONS_KEY] = enabled }
    }
}
