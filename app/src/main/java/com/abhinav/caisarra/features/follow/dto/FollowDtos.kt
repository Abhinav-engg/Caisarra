package com.abhinav.caisarra.features.follow.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FollowStatusDto(
    @SerialName("is_following")
    val isFollowing: Boolean = false
)

@Serializable
data class FollowCountsDto(
    val followers: Int = 0,
    val following: Int = 0
)

@Serializable
data class FollowUserDto(
    val id: Long,
    val username: String,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("is_following")
    val isFollowing: Boolean = false
)

@Serializable
data class FollowUsersDto(
    val users: List<FollowUserDto> = emptyList(),
    @SerialName("next_before_id")
    val nextBeforeId: Long? = null
)
