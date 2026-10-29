package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.ApiResponse
import com.abhinav.caisarra.data.remote.dto.PrivacySettingsDto
import com.abhinav.caisarra.data.remote.dto.UpdatePrivacySettingsRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT

interface PrivacyService {

    @GET("api/players/privacy")
    suspend fun getPrivacySettings(): ApiResponse<PrivacySettingsDto>

    @GET("api/privacy")
    suspend fun getPrivacySettingsLegacy(): ApiResponse<PrivacySettingsDto>

    @PUT("api/players/privacy")
    suspend fun updatePrivacySettings(
        @Body request: UpdatePrivacySettingsRequest
    ): ApiResponse<PrivacySettingsDto>

    @PATCH("api/players/privacy")
    suspend fun patchPrivacySettings(
        @Body request: UpdatePrivacySettingsRequest
    ): ApiResponse<PrivacySettingsDto>

    @PUT("api/privacy")
    suspend fun updatePrivacySettingsLegacy(
        @Body request: UpdatePrivacySettingsRequest
    ): ApiResponse<PrivacySettingsDto>

    @PATCH("api/privacy")
    suspend fun patchPrivacySettingsLegacy(
        @Body request: UpdatePrivacySettingsRequest
    ): ApiResponse<PrivacySettingsDto>
}
