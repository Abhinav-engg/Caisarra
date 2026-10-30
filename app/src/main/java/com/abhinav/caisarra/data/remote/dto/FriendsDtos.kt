package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FriendsListData(
    val friends: List<FriendDto> = emptyList()
)

@Serializable
data class FriendDto(
    @SerialName("user_id")
    val userId: Long,

    @SerialName("username")
    val username: String,

    @SerialName("rating")
    val rating: Int? = null,

    @SerialName("friends_since")
    val friendsSince: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    @SerialName("display_name")
    val displayName: String? = null,

    @SerialName("is_online")
    val isOnline: Boolean = false
)

@Serializable
data class FriendRequestsData(
    val requests: List<FriendRequestDto> = emptyList()
)

@Serializable
data class FriendRequestDto(
    val id: Long,
    @SerialName("sender_id")
    val senderId: Long? = null,
    @SerialName("sender_username")
    val senderUsername: String? = null,
    @SerialName("sender_rating")
    val senderRating: Int? = null,
    @SerialName("sender_avatar_url")
    val senderAvatarUrl: String? = null,
    @SerialName("sender_display_name")
    val senderDisplayName: String? = null,
    @SerialName("receiver_id")
    val receiverId: Long? = null,
    @SerialName("receiver_username")
    val receiverUsername: String? = null,
    @SerialName("receiver_rating")
    val receiverRating: Int? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class PlayerSearchEnvelope(
    val success: Boolean = true,
    val message: String? = null,
    val data: PlayerSearchData = PlayerSearchData()
)

@Serializable
data class PlayerSearchData(
    val players: List<PlayerSearchDto> = emptyList()
)

@Serializable
data class PlayerSearchDto(
    @SerialName("user_id")
    val userId: Long,

    val username: String,

    @SerialName("display_name")
    val displayName: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    val rating: Int? = null
)
@Serializable
data class FriendshipStatusData(
    @SerialName("status")
    val status: String,

    @SerialName("request_id")
    val requestId: Long? = null,

    @SerialName("is_online")
    val isOnline: Boolean? = null
)

@Serializable
data class SendFriendRequest(
    val username: String
)

@Serializable
data class SendFriendRequestData(
    @SerialName("request_id")
    val requestId: Long
)
