package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.remote.dto.BotGameDto
import com.abhinav.caisarra.data.remote.dto.CreateBotGameRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BotGameService {

    @POST("api/bot-games")
    suspend fun createBotGame(@Body request: CreateBotGameRequest): BotGameDto

    @GET("api/bot-games/{gameID}")
    suspend fun getBotGame(@Path("gameID") gameId: String): BotGameDto
}