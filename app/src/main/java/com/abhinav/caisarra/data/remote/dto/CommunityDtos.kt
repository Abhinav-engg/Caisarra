
package com.abhinav.caisarra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityAuthorDto(
    val username: String,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)

@Serializable
data class CommunityPostDto(
    val id: Long,
    val author: CommunityAuthorDto,
    val body: String,
    val reactions: Map<String, Int> = emptyMap(),
    @SerialName("my_reaction")
    val myReaction: String? = null,
    @SerialName("comment_count")
    val commentCount: Int = 0,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class CommunityCommentDto(
    val id: Long,
    val author: CommunityAuthorDto,
    val body: String,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class CommunityFeedData(
    val posts: List<CommunityPostDto> = emptyList(),
    @SerialName("next_before_id")
    val nextBeforeId: Long? = null
)

@Serializable
data class CommunityCommentsData(
    val comments: List<CommunityCommentDto> = emptyList(),
    @SerialName("next_after_id")
    val nextAfterId: Long? = null
)

@Serializable
data class CommunityReactionData(
    val reactions: Map<String, Int> = emptyMap(),
    @SerialName("my_reaction")
    val myReaction: String? = null
)

@Serializable
data class CreateCommunityPostRequest(
    val body: String
)

@Serializable
data class CommunityReactionRequest(
    val reaction: String
)

@Serializable
data class CreateCommunityCommentRequest(
    val body: String
)
