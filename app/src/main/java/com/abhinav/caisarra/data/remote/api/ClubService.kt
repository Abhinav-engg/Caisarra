package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.*
import kotlinx.serialization.json.JsonElement
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ClubService {

    @GET("api/clubs")
    suspend fun getClubs(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("search") search: String? = null
    ): List<ClubDto>

    @POST("api/clubs")
    suspend fun createClub(
        @Body request: CreateClubRequest
    ): JsonElement

    @GET("api/clubs/mine")
    suspend fun getMyClubs(): List<ClubDto>

    @GET("api/clubs/{clubID}")
    suspend fun getClub(
        @Path("clubID") clubId: String
    ): ClubDto
    @DELETE("api/clubs/{clubID}")
    suspend fun deleteClub(
        @Path("clubID") clubId: String
    ): Response<ResponseBody>

    @POST("api/clubs/code/{code}/join")
    suspend fun joinClubByCode(
        @Path("code") code: String
    ): ApiResponse<ClubDto>

    @POST("api/clubs/{clubID}/join")
    suspend fun joinClub(
        @Path("clubID") clubId: String
    ): ApiResponse<ClubDto>

    @POST("api/clubs/{clubID}/leave")
    suspend fun leaveClub(
        @Path("clubID") clubId: String
    ): Response<ResponseBody>

    @GET("api/clubs/{clubID}/members")
    suspend fun getMembers(
        @Path("clubID") clubId: String
    ): JsonElement

    @GET("api/clubs/{clubID}/leaderboard")
    suspend fun getLeaderboard(
        @Path("clubID") clubId: String,
        @Query("mode") mode: String? = null
    ): JsonElement

    @GET("api/clubs/{clubID}/messages")
    suspend fun getMessages(
        @Path("clubID") clubId: String
    ): ApiResponse<ClubMessagesData>

    @POST("api/clubs/{clubID}/messages")
    suspend fun sendMessage(
        @Path("clubID") clubId: String,
        @Body request: SendClubMessageRequest
    ): ApiResponse<ClubMessageDto>

    @DELETE("api/clubs/{clubID}/members/{userID}")
    suspend fun kickMember(
        @Path("clubID") clubId: String,
        @Path("userID") userId: Long
    ): ApiResponse<Unit>

    @POST("api/clubs/{clubID}/admins/{userID}")
    suspend fun promoteAdmin(
        @Path("clubID") clubId: String,
        @Path("userID") userId: Long
    ): ApiResponse<Unit>

    @DELETE("api/clubs/{clubID}/admins/{userID}")
    suspend fun removeAdmin(
        @Path("clubID") clubId: String,
        @Path("userID") userId: Long
    ): ApiResponse<Unit>

    @POST("api/clubs/{clubID}/transfer/{userID}")
    suspend fun transferLeadership(
        @Path("clubID") clubId: String,
        @Path("userID") userId: Long
    ): ApiResponse<Unit>

    @GET("api/clubs/{clubID}/bans")
    suspend fun getBans(
        @Path("clubID") clubId: String
    ): ApiResponse<ClubMembersData>

    @POST("api/clubs/{clubID}/bans/{userID}")
    suspend fun banMember(
        @Path("clubID") clubId: String,
        @Path("userID") userId: Long
    ): ApiResponse<Unit>

    @DELETE("api/clubs/{clubID}/bans/{userID}")
    suspend fun unblockMember(
        @Path("clubID") clubId: String,
        @Path("userID") userId: Long
    ): ApiResponse<Unit>
}
