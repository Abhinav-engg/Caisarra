package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.AccessTokenResponse
import com.abhinav.caisarra.data.remote.dto.ApiResponse
import com.abhinav.caisarra.data.remote.dto.AuthResponse
import com.abhinav.caisarra.data.remote.dto.DeleteAccountRequest
import com.abhinav.caisarra.data.remote.dto.DeleteAccountResponse
import com.abhinav.caisarra.data.remote.dto.ForgotPasswordRequest
import com.abhinav.caisarra.data.remote.dto.GuestLoginResponse
import com.abhinav.caisarra.data.remote.dto.LoginData
import com.abhinav.caisarra.data.remote.dto.LoginRequest
import com.abhinav.caisarra.data.remote.dto.MessageResponse
import com.abhinav.caisarra.data.remote.dto.NeedsRatingData
import com.abhinav.caisarra.data.remote.dto.ProfileResponse
import com.abhinav.caisarra.data.remote.dto.RatingData
import com.abhinav.caisarra.data.remote.dto.RatingRequest
import com.abhinav.caisarra.data.remote.dto.RefreshTokenRequest
import com.abhinav.caisarra.data.remote.dto.RegisterRequest
import com.abhinav.caisarra.data.remote.dto.ResetPasswordRequest
import com.abhinav.caisarra.data.remote.dto.VerifyRegistrationRequest
import com.abhinav.caisarra.data.remote.dto.VerifyResetCodeRequest
import com.abhinav.caisarra.data.remote.dto.VerifyResetCodeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST

interface AuthService {

    @POST("register")
    suspend fun register(@Body body: RegisterRequest): MessageResponse

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<ApiResponse<LoginData>>

    @POST("verify-registration")
    suspend fun verifyRegistration(@Body body: VerifyRegistrationRequest): Response<ApiResponse<NeedsRatingData>>

    @POST("refresh")
    suspend fun refresh(@Body body: RefreshTokenRequest): AccessTokenResponse

    @POST("logout")
    suspend fun logout(@Body body: RefreshTokenRequest): MessageResponse

    @POST("logout-all")
    suspend fun logoutAll(@Body body: RefreshTokenRequest): MessageResponse

    @POST("forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): MessageResponse

    @POST("reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): MessageResponse



    @POST("guest-login")
    suspend fun guestLogin(): ApiResponse<GuestLoginResponse>

    @POST("verify-reset-code")
    suspend fun verifyResetCode(@Body body: VerifyResetCodeRequest): ApiResponse<VerifyResetCodeResponse>

    @POST("api/rating")
    suspend fun submitRating(@Body request: RatingRequest): ApiResponse<RatingData>

    /**
     * Permanently deletes the logged-in player's account after confirming their password.
     *
     * Rules & Constraints:
     * - Endpoint: DELETE /api/account
     * - Authentication: Bearer access token, registered users only (no guests)
     * - Rate limit: 5 requests per 15 minutes per IP
     * - Body: { "password": "<current_password>" } (required, max 72 characters)
     */
    @HTTP(method = "DELETE", path = "api/account", hasBody = true)
    suspend fun deleteAccount(
        @Body body: DeleteAccountRequest
    ): Response<DeleteAccountResponse>
}