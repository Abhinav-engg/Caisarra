package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.remote.dto.ApiResponse
import com.abhinav.caisarra.data.remote.dto.CreateInviteRequest
import com.abhinav.caisarra.data.remote.dto.CreateInviteResponse
import com.abhinav.caisarra.data.remote.dto.InvitePreviewResponse
import com.abhinav.caisarra.data.remote.dto.JoinGameResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface InviteService {

    @POST("api/invite")
    suspend fun createInvite(@Body request: CreateInviteRequest): ApiResponse<CreateInviteResponse>

    @GET("api/invite/{code}")
    suspend fun previewInvite(@Path("code") code: String): ApiResponse<InvitePreviewResponse>

    @POST("api/invite/{code}/join")
    suspend fun joinInvite(@Path("code") code: String): ApiResponse<JoinGameResponse>
}