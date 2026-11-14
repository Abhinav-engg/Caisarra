package com.abhinav.caisarra.features.notificationsettings.data

import com.abhinav.caisarra.data.remote.dto.ApiResponse
import com.abhinav.caisarra.data.repository.apiCall
import com.abhinav.caisarra.features.notificationsettings.dto.NotificationPreferenceDto

class NotificationPreferencesRepository(private val service: NotificationPreferencesService) {
    suspend fun preferences() = apiCall { service.preferences() }

    suspend fun update(type: String, enabled: Boolean) = apiCall {
        val response = service.update(NotificationPreferenceDto(type, enabled))
        ApiResponse(
            success = response.success,
            message = response.message,
            data = Unit.takeIf { response.success },
            error = response.error
        )
    }
}
