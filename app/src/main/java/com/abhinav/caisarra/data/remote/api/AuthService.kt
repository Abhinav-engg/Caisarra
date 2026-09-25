package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.AuthRequest
import com.abhinav.caisarra.data.remote.dto.AuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("login")
    suspend fun login(@Body request: AuthRequest): AuthResponse


}