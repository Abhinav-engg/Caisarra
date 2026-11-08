package com.abhinav.caisarra.features.minimates.data

import com.abhinav.caisarra.data.remote.dto.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MiniMatesService {
    @GET("mini-mates/overview")
    suspend fun overview(): ApiResponse<MiniMatesOverviewDto>

    @GET("mini-mates/levels/{level}")
    suspend fun level(@Path("level") level: Int): ApiResponse<MiniMatesLevelDto>

    @POST("mini-mates/levels/{level}/hint")
    suspend fun hint(@Path("level") level: Int, @Body body: MiniMatesHintRequest): ApiResponse<MiniMatesHintDto>

    @POST("mini-mates/levels/{level}/move")
    suspend fun move(@Path("level") level: Int, @Body body: MiniMatesMoveRequest): ApiResponse<MiniMatesMoveDto>
}
