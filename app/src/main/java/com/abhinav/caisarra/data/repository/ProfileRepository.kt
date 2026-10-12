
package com.abhinav.caisarra.data.repository

import com.abhinav.caisarra.data.remote.api.ProfileService
import com.abhinav.caisarra.data.remote.dto.ProfileData
import com.abhinav.caisarra.data.remote.dto.UpdateProfileRequest

class ProfileRepository(
    private val service: ProfileService
) {
    suspend fun getMyProfile(): ProfileData {
        val response = service.getMyProfile()

        if (!response.success) {
            error(response.error ?: response.message ?: "Could not load profile")
        }
        return response.data ?: error("Profile response was empty")
    }
    suspend fun updateProfile(
        request: UpdateProfileRequest
    ): ProfileData {
        val response = service.updateMyProfile(request)

        if (!response.success) {
            error(response.error ?: response.message ?: "Could not update profile")
        }
        return response.data ?: error("Updated profile response was empty")
    }
}
