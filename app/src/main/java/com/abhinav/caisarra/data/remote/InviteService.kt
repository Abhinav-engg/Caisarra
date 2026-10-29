package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.remote.dto.AcceptChallengeResponse
import com.abhinav.caisarra.data.remote.dto.ChallengeDto
import com.abhinav.caisarra.data.remote.dto.CreateInviteRequest
import com.abhinav.caisarra.data.remote.dto.CreateInviteResponse
import com.abhinav.caisarra.data.remote.dto.InvitePreviewResponse
import com.abhinav.caisarra.data.remote.dto.JoinGameResponse
import com.abhinav.caisarra.data.remote.dto.MessageResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface InviteService {

    @POST("api/v1/invite")
    suspend fun createInvite(@Body request: CreateInviteRequest): CreateInviteResponse

    @POST("api/invite")
    suspend fun createInviteLegacy(@Body request: CreateInviteRequest): CreateInviteResponse

    @GET("api/v1/challenges")
    suspend fun getReceivedChallenges(): List<ChallengeDto>

    @GET("api/v1/challenges/sent")
    suspend fun getSentChallenges(): List<ChallengeDto>

    @POST("api/v1/challenges/{code}/accept")
    suspend fun acceptChallenge(@Path("code") code: String): AcceptChallengeResponse

    @POST("api/v1/challenges/{code}/decline")
    suspend fun declineChallenge(@Path("code") code: String): MessageResponse

    @POST("api/v1/challenges/{code}/cancel")
    suspend fun cancelChallenge(@Path("code") code: String): MessageResponse

    @GET("api/v1/invite/{code}")
    suspend fun previewInvite(@Path("code") code: String): InvitePreviewResponse

    @GET("api/v1/api/invite/{code}")
    suspend fun previewInviteV1Api(@Path("code") code: String): InvitePreviewResponse

    @GET("api/invite/{code}")
    suspend fun previewInviteLegacy(@Path("code") code: String): InvitePreviewResponse

    @POST("api/v1/invite/{code}/join")
    suspend fun joinInvite(@Path("code") code: String): AcceptChallengeResponse

    @POST("api/invite/{code}/join")
    suspend fun joinInviteLegacy(@Path("code") code: String): JoinGameResponse
}
