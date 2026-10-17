package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.remote.dto.CreateTournamentRequest
import com.abhinav.caisarra.data.remote.dto.JoinTournamentRequest
import com.abhinav.caisarra.data.remote.dto.JoinTournamentResponse
import com.abhinav.caisarra.data.remote.dto.LeaveTournamentResponse
import com.abhinav.caisarra.data.remote.dto.RoundsResponse
import com.abhinav.caisarra.data.remote.dto.StandingsResponse
import com.abhinav.caisarra.data.remote.dto.StartTournamentResponse
import com.abhinav.caisarra.data.remote.dto.TournamentDto
import com.abhinav.caisarra.data.remote.dto.TournamentGamesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TournamentService {

    @POST("api/tournaments")
    suspend fun createTournament(@Body request: CreateTournamentRequest): TournamentDto

    @GET("api/tournaments")
    suspend fun listTournaments(): List<TournamentDto>

    @GET("api/tournaments/{id}")
    suspend fun getTournament(@Path("id") id: String): TournamentDto

    @GET("api/tournaments/invite/{code}")
    suspend fun getTournamentByInvite(@Path("code") code: String): TournamentDto

    @POST("api/tournaments/{id}/join")
    suspend fun joinTournament(
        @Path("id") id: String,
        @Body request: JoinTournamentRequest
    ): JoinTournamentResponse

    @POST("api/tournaments/invite/{code}/join")
    suspend fun joinByInvite(@Path("code") code: String): JoinTournamentResponse

    @POST("api/tournaments/{id}/leave")
    suspend fun leaveTournament(@Path("id") id: String): LeaveTournamentResponse

    @POST("api/tournaments/{id}/start")
    suspend fun startTournament(@Path("id") id: String): StartTournamentResponse

    @GET("api/tournaments/{id}/standings")
    suspend fun getStandings(@Path("id") id: String): StandingsResponse

    @GET("api/tournaments/{id}/rounds")
    suspend fun getRounds(@Path("id") id: String): RoundsResponse

    @GET("api/tournaments/{id}/rounds/{round}")
    suspend fun getRound(
        @Path("id") id: String,
        @Path("round") round: Int
    ): RoundsResponse

    @GET("api/tournaments/{id}/games")
    suspend fun getGames(@Path("id") id: String): TournamentGamesResponse

    @GET("api/tournaments/{id}/my-games")
    suspend fun getMyGames(@Path("id") id: String): TournamentGamesResponse
}