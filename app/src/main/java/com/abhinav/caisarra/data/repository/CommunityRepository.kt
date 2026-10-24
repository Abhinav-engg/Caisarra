
package com.abhinav.caisarra.data.repository

import com.abhinav.caisarra.data.remote.api.CommunityService
import com.abhinav.caisarra.data.remote.dto.CommunityCommentDto
import com.abhinav.caisarra.data.remote.dto.CommunityPostDto
import com.abhinav.caisarra.data.remote.dto.CommunityReactionData
import com.abhinav.caisarra.data.remote.dto.CreateCommunityCommentRequest
import com.abhinav.caisarra.data.remote.dto.CreateCommunityPostRequest
import com.abhinav.caisarra.data.remote.dto.CommunityReactionRequest

class CommunityRepository(
    private val service: CommunityService
) {
    suspend fun loadFeed(beforeId: Long? = null) =
        service.getFeed(beforeId = beforeId, limit = 20)
            .also {
                check(it.success) {
                    it.error ?: it.message ?: "Unable to load community feed"
                }
            }.data ?: error("Community feed response was empty")


    suspend fun createPost(body: String): CommunityPostDto {
        val text = body.trim()

        require(text.isNotEmpty()) {
            "Post cannot be empty."
        }
        require(text.length <= 2000) {
            "Post must not exceed 2000 characters."
        }

        val result = service.createPost(
            CreateCommunityPostRequest(
                body = text,
                kind = "text"
            )
        )
        check(result.success) {
            result.error ?: result.message ?: "Unable to create post"
        }
        return result.data?: error("Created post was missing from response")
    }

    suspend fun shareGame(
        gameId: String,
        caption: String = ""
    ): CommunityPostDto {
        require(gameId.isNotBlank()) {
            "Please select a game to share."
        }

        val text = caption.trim()

        require(text.length <= 2000) {
            "Caption must not exceed 2000 characters."
        }

        val result = service.createPost(
            CreateCommunityPostRequest(
                kind = "game",
                gameId = gameId,
                body = text.ifBlank { null }
            )
        )

        check(result.success) {
            result.error ?: result.message ?: "Unable to share game"
        }

        return result.data ?: error("Shared game was missing from response")
    }



    suspend fun deletePost(postId: Long) {
        val result = service.deletePost(postId)
        check(result.success) {
            result.error ?: result.message ?: "Unable to delete post"
        }
    }

    suspend fun setReaction(
        postId: Long,
        reaction: String
    ): CommunityReactionData {
        require(reaction in setOf("like", "love", "fire", "wow")) {
            "Invalid reaction."
        }

        val result = service.setReaction(
            postId,
            CommunityReactionRequest(reaction)
        )

        check(result.success) {
            result.error ?: result.message ?: "Unable to react"
        }

        return result.data ?: error("Reaction response was empty")
    }

    suspend fun removeReaction(
        postId: Long
    ): CommunityReactionData {
        val result = service.removeReaction(postId)

        check(result.success) {
            result.error ?: result.message ?: "Unable to remove reaction"
        }

        return result.data ?: error("Reaction response was empty")
    }

    suspend fun loadComments(
        postId: Long,
        afterId: Long? = null
    ) = service.getComments(
        postId = postId,
        afterId = afterId,
        limit = 20
    ).let {
        check(it.success) {
            it.error ?: it.message ?: "Unable to load comments"
        }
        it.data ?: error("Comments response was empty")
    }

    suspend fun addComment(
        postId: Long,
        body: String
    ): CommunityCommentDto {
        val text = body.trim()
        require(text.isNotEmpty()) { "Comment cannot be empty." }
        require(text.length <= 500) {
            "Comment must not exceed 500 characters."
        }

        val result = service.addComment(
            postId,
            CreateCommunityCommentRequest(text)
        )

        check(result.success) {
            result.error ?: result.message ?: "Unable to add comment"
        }

        return result.data ?: error("Created comment was missing")
    }

    suspend fun deleteComment(commentId: Long) {
        val result = service.deleteComment(commentId)
        check(result.success) {
            result.error ?: result.message ?: "Unable to delete comment"
        }
    }
}
