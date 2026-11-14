package com.abhinav.caisarra.features.follow.data

import com.abhinav.caisarra.data.remote.dto.ApiResponse
import com.abhinav.caisarra.features.follow.dto.FollowCountsDto
import com.abhinav.caisarra.features.follow.dto.FollowStatusDto
import com.abhinav.caisarra.features.follow.dto.FollowUsersDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FollowService {
    @POST("api/users/{id}/follow")
    suspend fun follow(@Path("id") userId: Long): ApiResponse<FollowStatusDto>

    @DELETE("api/users/{id}/follow")
    suspend fun unfollow(@Path("id") userId: Long): ApiResponse<FollowStatusDto>

    @GET("api/users/{id}/follow-status")
    suspend fun status(@Path("id") userId: Long): ApiResponse<FollowStatusDto>

    @GET("api/users/{id}/follow-counts")
    suspend fun counts(@Path("id") userId: Long): ApiResponse<FollowCountsDto>

    @GET("api/users/{id}/{list}")
    suspend fun users(
        @Path("id") userId: Long,
        @Path("list") list: String,
        @Query("before_id") beforeId: Long? = null,
        @Query("limit") limit: Int = 20
    ): ApiResponse<FollowUsersDto>
}
