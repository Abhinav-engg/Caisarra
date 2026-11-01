package com.abhinav.caisarra.data.repository

import android.content.Context

import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.api.DirectMessageService
import com.abhinav.caisarra.data.remote.dto.DirectConversationDto
import com.abhinav.caisarra.data.remote.dto.DirectMessageDto
import com.abhinav.caisarra.data.remote.dto.DirectPlayerDto
import com.abhinav.caisarra.data.remote.dto.SendDirectMessageRequest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

class DirectMessageBlockedException(message: String) : Exception(message)

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
        try {
            return service.getHistory(userId)
                .data
                .messages
        } catch (e: HttpException) {
            throw toReadableError(e, "Couldn't load chat history.")
        }
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

        try {
            return service.sendMessage(
                userId = userId,
                request = SendDirectMessageRequest(
                    message = message.trim()
                )
            )
        } catch (e: HttpException) {
            throw toReadableError(e, "Message could not be sent.")
        }
    }

    private fun toReadableError(e: HttpException, fallback: String): Exception {
        val body = try {
            e.response()?.errorBody()?.string()
        } catch (ex: Exception) {
            null
        }

        var serverMessage: String? = null
        if (!body.isNullOrBlank()) {
            try {
                val obj = RetrofitInstance.json.parseToJsonElement(body).jsonObject
                serverMessage = (obj["error"] ?: obj["message"])?.jsonPrimitive?.content
            } catch (ex: Exception) {
                serverMessage = null
            }
        }

        if (e.code() == 403) {
            return DirectMessageBlockedException(
                if (serverMessage.isNullOrBlank()) {
                    "This player only accepts messages from friends."
                } else {
                    serverMessage
                }
            )
        }

        if (!serverMessage.isNullOrBlank()) {
            return Exception(serverMessage)
        }

        return Exception(fallback)
    }
}