package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.LoginRequest
import com.abhinav.caisarra.data.remote.dto.LoginResponse
import com.abhinav.caisarra.data.remote.dto.RegisterRequest
import com.abhinav.caisarra.data.remote.dto.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.PUT

interface AuthService {

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse


}