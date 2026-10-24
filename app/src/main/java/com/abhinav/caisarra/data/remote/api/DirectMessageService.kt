package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.DirectConversationsEnvelope
import com.abhinav.caisarra.data.remote.dto.DirectMessagesEnvelope
import com.abhinav.caisarra.data.remote.dto.DirectMessageDto
import com.abhinav.caisarra.data.remote.dto.SendDirectMessageRequest
import com.abhinav.caisarra.data.remote.dto.PlayerSearchEnvelope
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
interface DirectMessageService {

    @GET("api/messages")
    suspend fun getConversations(): DirectConversationsEnvelope


    @GET("api/messages/{userID}")
    suspend fun getHistory(
        @Path("userID") userId: Long
    ): DirectMessagesEnvelope

    @POST("api/messages/{userID}")
    suspend fun sendMessage(
        @Path("userID") userId: Long,
        @Body request: SendDirectMessageRequest
    ): DirectMessageDto
    @GET("api/players/search")
    suspend fun searchPlayers(
        @Query("q") query: String
    ): PlayerSearchEnvelope
}