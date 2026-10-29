package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.ActiveGameDto
import com.abhinav.caisarra.data.remote.dto.GameHistoryDto
import com.abhinav.caisarra.data.remote.dto.GameMoveDto
import com.abhinav.caisarra.data.remote.dto.WatchInviteResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GamesService {

    @GET("api/games/history")
    suspend fun getGameHistory(): List<GameHistoryDto>

    @GET("api/games/{gameID}")
    suspend fun getGame(
        @Path("gameID") gameId: String
    ): GameHistoryDto

    @GET("api/games/{gameID}/moves")
    suspend fun getGameMoves(
        @Path("gameID") gameId: String
    ): List<GameMoveDto>

    @POST("api/games/{gameID}/watch-invite")
    suspend fun createWatchInvite(
        @Path("gameID") gameId: String
    ): WatchInviteResponse

    @GET("api/games/active")
    suspend fun getActiveGames(): List<ActiveGameDto>

    @GET("players/{username}/games")
    suspend fun getPlayerGames(
        @Path("username") username: String,
        @retrofit2.http.Query("mode") mode: String? = null,
        @retrofit2.http.Query("limit") limit: Int? = null
    ): okhttp3.ResponseBody
}