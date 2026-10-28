package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.ApiResponse
import com.abhinav.caisarra.data.remote.dto.PrivacySettingsDto
import com.abhinav.caisarra.data.remote.dto.UpdatePrivacySettingsRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface PrivacyService {

    @GET("api/privacy")
    suspend fun getPrivacySettings(): ApiResponse<PrivacySettingsDto>

    @PUT("api/privacy")
    suspend fun updatePrivacySettings(
        @Body request: UpdatePrivacySettingsRequest
    ): ApiResponse<PrivacySettingsDto>
}
