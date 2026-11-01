package com.abhinav.caisarra.data.repository

import com.abhinav.caisarra.data.remote.api.PrivacyService
import com.abhinav.caisarra.data.remote.dto.PrivacySettingsDto
import com.abhinav.caisarra.data.remote.dto.UpdatePrivacySettingsRequest
import retrofit2.HttpException

class PrivacyRepository(
    private val service: PrivacyService
) {
    suspend fun getPrivacySettings(): PrivacySettingsDto {
        val response = try {
            service.getPrivacySettings()
        } catch (e: HttpException) {
            if (e.code() == 404 || e.code() == 405) {
                service.getPrivacySettingsLegacy()
            } else {
                throw e
            }
        }
        if (!response.success) {
            error(response.error ?: response.message ?: "Failed to fetch privacy settings")
        }
        return response.data ?: PrivacySettingsDto()
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

        val response = try {
            service.updatePrivacySettings(request)
        } catch (e: HttpException) {
            when (e.code()) {
                404, 405 -> {
                    try {
                        service.patchPrivacySettings(request)
                    } catch (e2: HttpException) {
                        if (e2.code() == 404 || e2.code() == 405) {
                            try {
                                service.updatePrivacySettingsLegacy(request)
                            } catch (e3: HttpException) {
                                if (e3.code() == 404 || e3.code() == 405) {
                                    service.patchPrivacySettingsLegacy(request)
                                } else throw e3
                            }
                        } else throw e2
                    }
                }
                else -> throw e
            }
        }

        if (!response.success) {
            error(response.error ?: response.message ?: "Failed to update privacy settings")
        }
        return response.data ?: PrivacySettingsDto(
            directMessage = directMessage ?: "everyone",
            club = club ?: "everyone",
            tournament = tournament ?: "everyone",
            inviteFriend = inviteFriend ?: "everyone"
        )
    }
}
