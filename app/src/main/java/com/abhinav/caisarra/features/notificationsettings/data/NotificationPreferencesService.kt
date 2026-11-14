package com.abhinav.caisarra.features.notificationsettings.data

import com.abhinav.caisarra.data.remote.dto.ApiResponse
import com.abhinav.caisarra.data.remote.dto.SimpleApiResponse
import com.abhinav.caisarra.features.notificationsettings.dto.NotificationPreferenceDto
import com.abhinav.caisarra.features.notificationsettings.dto.NotificationPreferencesDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface NotificationPreferencesService {
    @GET("api/notifications/preferences")
    suspend fun preferences(): ApiResponse<NotificationPreferencesDto>

    @PUT("api/notifications/preferences")
    suspend fun update(@Body body: NotificationPreferenceDto): SimpleApiResponse
}
