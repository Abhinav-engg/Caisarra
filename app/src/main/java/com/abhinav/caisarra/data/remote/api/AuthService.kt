package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.AccessTokenResponse
import com.abhinav.caisarra.data.remote.dto.AuthResponse
import com.abhinav.caisarra.data.remote.dto.LoginRequest
import com.abhinav.caisarra.data.remote.dto.MessageResponse
import com.abhinav.caisarra.data.remote.dto.RegisterRequest
import com.abhinav.caisarra.data.remote.dto.RefreshTokenRequest
import retrofit2.http.Body
import retrofit2.http.POST


interface AuthService {

    @POST("register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("refresh")
    suspend fun refresh(@Body body: RefreshTokenRequest): AccessTokenResponse

    @POST("logout")
    suspend fun logout(@Body body: RefreshTokenRequest): MessageResponse

    @POST("logout-all")
    suspend fun logoutAll(@Body body: RefreshTokenRequest): MessageResponse
}