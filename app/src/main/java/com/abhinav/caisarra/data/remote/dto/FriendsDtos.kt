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
    val username: String,
    val rating: Int? = null,
    @SerialName("friends_since")
    val friendsSince: String? = null
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
data class PlayerSearchData(
    val players: List<PlayerSearchDto> = emptyList()
)

@Serializable
data class PlayerSearchDto(
    val username: String,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    val rating: Int? = null
)

@Serializable
data class FriendshipStatusData(
    val status: String,
    @SerialName("request_id")
    val requestId: Long? = null
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
