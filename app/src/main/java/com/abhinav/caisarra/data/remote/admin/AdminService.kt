package com.abhinav.caisarra.data.remote.admin

import com.abhinav.caisarra.data.remote.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.*

interface AdminService {

    @GET("api/admin/stats")
    suspend fun getStats(): Response<ApiResponse<AdminStats>>

    @GET("api/admin/users")
    suspend fun getUsers(
        @Query("q") query: String = "",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<List<AdminUser>>>

    @GET("api/admin/users/{id}")
    suspend fun getUserDetails(
        @Path("id") id: Long
    ): Response<ApiResponse<AdminUserDetails>>

    @POST("api/admin/users/{id}/ban")
    suspend fun banUser(
        @Path("id") id: Long,
        @Body request: AdminBanRequest
    ): Response<ApiResponse<Unit>>

    @POST("api/admin/users/{id}/unban")
    suspend fun unbanUser(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>

    @POST("api/admin/users/{id}/logout")
    suspend fun forceLogout(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>

    @GET("api/admin/reports")
    suspend fun getReports(
        @Query("status") status: String = "pending",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<List<AdminReport>>>

    @POST("api/admin/reports/{id}/close")
    suspend fun closeReport(
        @Path("id") id: Long,
        @Body request: CloseReportRequest
    ): Response<ApiResponse<Unit>>

    @DELETE("api/admin/posts/{id}")
    suspend fun deletePost(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>

    @DELETE("api/admin/comments/{id}")
    suspend fun deleteComment(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>

    @DELETE("api/admin/clubs/{id}")
    suspend fun deleteClub(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>

    @POST("api/admin/tournaments/{id}/cancel")
    suspend fun cancelTournament(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>

    @GET("api/admin/actions")
    suspend fun getActions(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<List<AdminAction>>>
}