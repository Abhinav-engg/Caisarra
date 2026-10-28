package com.abhinav.caisarra.data.repository

import com.abhinav.caisarra.data.remote.api.PrivacyService
import com.abhinav.caisarra.data.remote.dto.PrivacySettingsDto
import com.abhinav.caisarra.data.remote.dto.UpdatePrivacySettingsRequest

class PrivacyRepository(
    private val service: PrivacyService
) {
    suspend fun getPrivacySettings(): PrivacySettingsDto {
        val response = service.getPrivacySettings()
        if (!response.success) {
            error(response.error ?: response.message ?: "Failed to fetch privacy settings")
        }
        return response.data ?: error("Privacy settings response was empty")
    }

    suspend fun updatePrivacySettings(
        directMessage: String? = null,
        club: String? = null,
        tournament: String? = null,
        inviteFriend: String? = null
    ): PrivacySettingsDto {
        val request = UpdatePrivacySettingsRequest(
            directMessage = directMessage,
            club = club,
            tournament = tournament,
            inviteFriend = inviteFriend
        )
        val response = service.updatePrivacySettings(request)
        if (!response.success) {
            error(response.error ?: response.message ?: "Failed to update privacy settings")
        }
        return response.data ?: error("Privacy settings update failed")
    }
}
