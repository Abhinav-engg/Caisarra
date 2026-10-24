package com.abhinav.caisarra.data.repository

import android.content.Context

import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.api.DirectMessageService
import com.abhinav.caisarra.data.remote.dto.DirectConversationDto
import com.abhinav.caisarra.data.remote.dto.DirectMessageDto
import com.abhinav.caisarra.data.remote.dto.DirectPlayerDto
import com.abhinav.caisarra.data.remote.dto.SendDirectMessageRequest

class DirectMessageRepository(context: Context) {

    private val service: DirectMessageService =
        RetrofitInstance.createDirectMessageService(
            TokenManager.get(context)
        )

    suspend fun getConversations(): List<DirectConversationDto> {
        return service.getConversations()
            .data
            .conversations
    }

    suspend fun searchPlayers(
        query: String
    ): List<DirectPlayerDto> {
        val normalizedQuery = query.trim()

        require(
            normalizedQuery.isNotBlank() &&
                    (
                            normalizedQuery.length >= 2 ||
                                    normalizedQuery.all { it.isDigit() }
                            )
        ) {
            "Enter at least two username characters or a numeric user ID."
        }

        return service.searchPlayers(normalizedQuery)
            .data
            .players
            .map { player ->
                DirectPlayerDto(
                    userId = player.userId,
                    username = player.username,
                    displayName = player.displayName,
                    avatarUrl = player.avatarUrl,
                    rating = player.rating
                )
            }
    }
    suspend fun getMessages(
        userId: Long
    ): List<DirectMessageDto> {
        return service.getHistory(userId)
            .data
            .messages
    }

    suspend fun sendMessage(
        userId: Long,
        message: String
    ): DirectMessageDto {
        require(message.isNotBlank()) {
            "Message cannot be empty."
        }

        require(message.length <= 500) {
            "Message cannot exceed 500 characters."
        }

        return service.sendMessage(
            userId = userId,
            request = SendDirectMessageRequest(
                message = message.trim()
            )
        )
    }
}