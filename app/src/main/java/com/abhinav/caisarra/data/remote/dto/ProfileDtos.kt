
package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileData(
    val username: String,
    val email: String = "",
    @SerialName("display_name")
    val displayName: String? = null,
    val country: String? = null,
    val bio: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    val rating: Int? = null,
    val provisional: Boolean = true,
    val ratings: Map<String, ModeRating>? = null,
    @SerialName("skill_level")
    val skillLevel: String? = null,
    @SerialName("needs_rating")
    val needsRating: Boolean = false,
    @SerialName("joined_at")
    val joinedAt: String? = null
)

@Serializable
data class ModeRating(
    val rating: Int? = null,
    val provisional: Boolean = true,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0
)

@Serializable
data class UpdateProfileRequest(
    @SerialName("display_name")
    val displayName: String? = null,
    val country: String? = null,
    val bio: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)
