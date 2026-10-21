package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.BotGame
import com.abhinav.caisarra.data.remote.dto.BotGameListResponse
import com.abhinav.caisarra.data.remote.dto.BotListResponse
import com.abhinav.caisarra.data.remote.dto.CreateBotGameRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BotGameApiService {

    @GET("api/bot-games/bots")
    suspend fun getBots(): Response<BotListResponse>

    @POST("api/bot-games")
    suspend fun createGame(@Body body: CreateBotGameRequest): Response<BotGame>

    @GET("api/bot-games")
    suspend fun getGames(): Response<BotGameListResponse>

    @GET("api/bot-games/{gameID}")
    suspend fun getGame(@Path("gameID") gameId: String): Response<BotGame>
}
