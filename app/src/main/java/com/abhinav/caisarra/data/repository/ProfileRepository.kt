
package com.abhinav.caisarra.data.repository

import com.abhinav.caisarra.data.remote.api.ProfileService
import com.abhinav.caisarra.data.remote.dto.ProfileData
import com.abhinav.caisarra.data.remote.dto.UpdateProfileRequest
import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileInputStream

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

    suspend fun searchPlayers(query: String): List<com.abhinav.caisarra.data.remote.dto.PlayerSearchDto> {
        val response = service.searchPlayers(query.trim())

        if (!response.success) {
            error(response.error ?: response.message ?: "Could not search players")
        }
        return response.data?.players.orEmpty()
    }
    suspend fun uploadAvatar(context: Context, uri: Uri): ProfileData {
        val bytes = when (uri.scheme) {
            "file" -> FileInputStream(File(uri.path!!)).use { it.readBytes() }
            else -> context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Unable to read selected image")
        }
        require(bytes.size <= 5 * 1024 * 1024) { "Photo must be 5 MB or smaller." }
        val mimeType = if (uri.scheme == "file") {
            "image/jpeg"
        } else {
            context.contentResolver.getType(uri) ?: "image/jpeg"
        }
        require(mimeType in setOf("image/jpeg", "image/png", "image/webp")) {
            "Choose a JPEG, PNG, or WebP image."
        }
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val body = bytes.toRequestBody(mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData("photo", "profile.$extension", body)
        val response = service.uploadAvatar(part)
        if (!response.success) error(response.error ?: response.message ?: "Could not upload photo")
        return getMyProfile()
    }

    suspend fun setDefaultAvatar(avatarId: String): ProfileData {
        val response = service.setDefaultAvatar(com.abhinav.caisarra.data.remote.api.DefaultAvatarRequest(avatarId))
        if (!response.success) error(response.error ?: response.message ?: "Could not set avatar")
        return getMyProfile()
    }
    suspend fun getDefaultAvatars():
            List<com.abhinav.caisarra.data.remote.api.DefaultAvatarItem> {

        val response = service.getDefaultAvatars()

        if (!response.success) {
            error(response.error ?: response.message ?: "Unable to load avatars")
        }

        return response.data?.avatars
            ?: error("Default avatar list is empty")
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
    suspend fun getPublicProfile(username: String): ProfileData {
        require(username.isNotBlank()) {
            "Username cannot be empty."
        }

        val response = service.getPublicProfile(username.trim())

        if (!response.success) {
            error(
                response.error
                    ?: response.message
                    ?: "Could not load player profile"
            )
        }

        return response.data
            ?: error("Player profile response was empty")
    }
}

