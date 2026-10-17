package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.AnalysisDto
import com.abhinav.caisarra.data.remote.dto.AnalysisSummaryResponse
import com.abhinav.caisarra.data.remote.dto.MoveAnalysisDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AnalysisService {

    @POST("api/games/{gameId}/analysis")
    suspend fun requestAnalysis(@Path("gameId") gameId: String): AnalysisDto

    @GET("api/games/{gameId}/analysis")
    suspend fun getSummary(@Path("gameId") gameId: String): AnalysisSummaryResponse

    @GET("api/games/{gameId}/analysis/moves")
    suspend fun getMoves(@Path("gameId") gameId: String): List<MoveAnalysisDto>

    @GET("api/games/{gameId}/analysis/moves/{moveNumber}")
    suspend fun getMove(
        @Path("gameId") gameId: String,
        @Path("moveNumber") moveNumber: Int
    ): MoveAnalysisDto

    @POST("api/games/{gameId}/analysis/retry")
    suspend fun retry(@Path("gameId") gameId: String): AnalysisDto
}