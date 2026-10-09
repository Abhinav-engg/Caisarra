package com.abhinav.caisarra.data.repository

import android.content.Context
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.dto.ChatMessageDto
import com.abhinav.caisarra.data.remote.dto.SendChatRequest

class ChatRepository private constructor(context: Context) {

    private val service = RetrofitInstance.createChatService(TokenManager(context))

    suspend fun getMessages(gameId: String): Result<List<ChatMessageDto>> =
        call { service.getMessages(gameId) }

    suspend fun sendMessage(gameId: String, message: String): Result<ChatMessageDto> =
        call { service.sendMessage(gameId, SendChatRequest(message)) }

    private suspend fun <T> call(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorInterpreter.toUserMessage(e)))
        }
    }

    companion object {
        @Volatile
        private var instance: ChatRepository? = null

        fun get(context: Context): ChatRepository =
            instance ?: synchronized(this) {
                instance ?: ChatRepository(context.applicationContext).also { instance = it }
            }
    }
}