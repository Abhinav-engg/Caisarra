package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DirectMessagesEnvelope(
    val success: Boolean = true,
    val message: String? = null,
    val data: DirectMessagesData = DirectMessagesData()
)

@Serializable
data class DirectMessagesData(
    val messages: List<DirectMessageDto> = emptyList()
)

@Serializable
data class DirectConversationsEnvelope(
    val success: Boolean = true,
    val message: String? = null,
    val data: DirectConversationsData = DirectConversationsData()
)

@Serializable
data class DirectConversationsData(
    val conversations: List<DirectConversationDto> = emptyList()
)

@Serializable
data class DirectConversationDto(
    @SerialName("user_id")
    val userId: Long,

    val username: String,

    @SerialName("display_name")
    val displayName: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    @SerialName("last_message")
    val lastMessage: String? = null,

    @SerialName("last_message_at")
    val lastMessageAt: String? = null,

    @SerialName("unread_count")
    val unreadCount: Int = 0
)

@Serializable
data class DirectMessageDto(
    val id: Long,

    @SerialName("sender_id")
    val senderId: Long,

    @SerialName("receiver_id")
    val receiverId: Long,

    val message: String,

    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class SendDirectMessageRequest(
    val message: String
)

@Serializable
data class DirectPlayerDto(
    @SerialName("user_id")
    val userId: Long,

    val username: String,

    @SerialName("display_name")
    val displayName: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    val rating: Int? = null
)