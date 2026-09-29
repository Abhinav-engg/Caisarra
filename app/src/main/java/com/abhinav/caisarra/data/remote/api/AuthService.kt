package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.AccessTokenResponse
import com.abhinav.caisarra.data.remote.dto.AuthResponse
import com.abhinav.caisarra.data.remote.dto.ForgotPasswordRequest
import com.abhinav.caisarra.data.remote.dto.LoginRequest
import com.abhinav.caisarra.data.remote.dto.MessageResponse
import com.abhinav.caisarra.data.remote.dto.RegisterRequest
import com.abhinav.caisarra.data.remote.dto.RefreshTokenRequest
import com.abhinav.caisarra.data.remote.dto.ResetPasswordRequest
import com.abhinav.caisarra.data.remote.dto.VerifyRegistrationRequest
import com.abhinav.caisarra.data.remote.dto.VerifyRegistrationResponse
import com.abhinav.caisarra.data.remote.dto.VerifyResetCodeRequest
import com.abhinav.caisarra.data.remote.dto.VerifyResetCodeResponse
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

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): MessageResponse

    @POST("auth/verify-reset-code")
    suspend fun verifyResetCode(@Body body: VerifyResetCodeRequest): VerifyResetCodeResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): MessageResponse
    @POST("verify-registration")
    suspend fun verifyRegistration(@Body body: VerifyRegistrationRequest): VerifyRegistrationResponse

}