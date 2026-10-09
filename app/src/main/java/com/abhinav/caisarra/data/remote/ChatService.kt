package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.remote.dto.ChatMessageDto
import com.abhinav.caisarra.data.remote.dto.SendChatRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatService {

    @GET("api/games/{gameId}/messages")
    suspend fun getMessages(@Path("gameId") gameId: String): List<ChatMessageDto>

    @POST("api/games/{gameId}/messages")
    suspend fun sendMessage(
        @Path("gameId") gameId: String,
        @Body request: SendChatRequest
    ): ChatMessageDto
}