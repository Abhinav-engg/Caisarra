package com.abhinav.caisarra.data.repository

import android.content.Context
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.TokenRefresher
import com.abhinav.caisarra.data.remote.api.AnalysisService
import com.abhinav.caisarra.data.remote.api.BotGameApiService
import com.abhinav.caisarra.data.remote.api.GamesService
import com.abhinav.caisarra.data.remote.api.MatchmakingService
import com.abhinav.caisarra.data.remote.api.NearbyService
import com.abhinav.caisarra.data.remote.api.ProfileService
import com.abhinav.caisarra.data.remote.api.RatingService
import com.abhinav.caisarra.data.remote.dto.ForgotPasswordRequest
import com.abhinav.caisarra.data.remote.dto.LoginRequest
import com.abhinav.caisarra.data.remote.dto.RatingRequest
import com.abhinav.caisarra.data.remote.dto.RefreshTokenRequest
import com.abhinav.caisarra.data.remote.dto.RegisterRequest
import com.abhinav.caisarra.data.remote.dto.ResetPasswordRequest
import com.abhinav.caisarra.data.remote.dto.VerifyRegistrationRequest
import com.abhinav.caisarra.data.remote.dto.VerifyResetCodeRequest
import okhttp3.Headers
import retrofit2.HttpException

class AuthRepository private constructor(context: Context) {

    private val tokenManager = TokenManager.get(context)
    private val refresher = RetrofitInstance.getTokenRefresher(tokenManager)
    private val service = RetrofitInstance.createAuthService(tokenManager)
    private var pendingRegistration: RegisterRequest? = null
    private var resetToken: String? = null

    private fun cookieValue(headers: Headers, name: String): String? =
        headers.values("Set-Cookie")
            .firstOrNull { it.startsWith("$name=") }
            ?.substringAfter("$name=")
            ?.substringBefore(";")

    private suspend fun saveTokensFromCookies(headers: Headers): Boolean {
        val accessToken = cookieValue(headers, "access_token") ?: return false
        val refreshToken = cookieValue(headers, "refresh_token") ?: return false
        tokenManager.saveTokens(accessToken, refreshToken)
        return true
    }

    suspend fun register(username: String, email: String, password: String): AuthResult {
        return try {
            val request = RegisterRequest(username, email, password)
            val response = service.register(request)
            pendingRegistration = request
            AuthResult.Success(response.message)
        } catch (e: Exception) {
            AuthResult.Error(ApiErrorInterpreter.toUserMessage(e))
        }
    }

    suspend fun resendRegistrationCode(): AuthResult {
        val request = pendingRegistration
            ?: return AuthResult.Error("Please sign up again.")
        return try {
            val response = service.register(request)
            AuthResult.Success(response.message)
        } catch (e: Exception) {
            AuthResult.Error(ApiErrorInterpreter.toUserMessage(e))
        }
    }

    suspend fun verifyRegistration(email: String, code: String): AuthResult {
        return try {
            val response = service.verifyRegistration(VerifyRegistrationRequest(email, code))
            if (!response.isSuccessful) throw HttpException(response)
            if (!saveTokensFromCookies(response.headers())) {
                return AuthResult.Error("Verification failed. Please try again.")
            }

            val username = pendingRegistration?.username
            if (!username.isNullOrBlank()) {
                if (!username.contains("@")) {
                tokenManager.saveUsername(username.trim())
            }
            }
            pendingRegistration = null
            AuthResult.Success(response.body()?.message.orEmpty())
        } catch (e: Exception) {
            AuthResult.Error(ApiErrorInterpreter.toUserMessage(e))
        }
    }

    suspend fun login(username: String, password: String): AuthResult {
        return try {
            val response = service.login(LoginRequest(username, password))
            if (!response.isSuccessful) throw HttpException(response)
            if (!saveTokensFromCookies(response.headers())) {
                return AuthResult.Error("Login failed. Please try again.")
            }
            if (!username.contains("@")) {
                tokenManager.saveUsername(username.trim())
            }

            AuthResult.Success(response.body()?.message.orEmpty())
        } catch (e: Exception) {
            AuthResult.Error(ApiErrorInterpreter.toUserMessage(e))
        }
    }

    suspend fun getValidAccessToken(): String? = refresher.getValidAccessToken()

    suspend fun logout(): AuthResult {
        val refreshToken = tokenManager.getRefreshToken()
        tokenManager.clearTokens()

        if (refreshToken != null) {
            try {
                service.logout(RefreshTokenRequest(refreshToken))
            } catch (_: Exception) {
            }
        }

        return AuthResult.Success("Logged out")
    }

    suspend fun sendResetCode(email: String): AuthResult {
        return try {
            val response = service.forgotPassword(ForgotPasswordRequest(email))
            AuthResult.Success(response.message)
        } catch (e: Exception) {
            AuthResult.Error(ApiErrorInterpreter.toUserMessage(e))
        }
    }

    suspend fun guestLogin(): AuthResult {
        tokenManager.getGuestId()?.let { return AuthResult.Success(it) }

        return try {
            val guestId = service.guestLogin().data?.guestId
                ?: return AuthResult.Error("Something went wrong. Please try again.")
            tokenManager.saveGuestId(guestId)
            AuthResult.Success(guestId)
        } catch (e: Exception) {
            AuthResult.Error(ApiErrorInterpreter.toUserMessage(e))
        }
    }

    suspend fun verifyResetCode(email: String, code: String): AuthResult {
        return try {
            val response = service.verifyResetCode(VerifyResetCodeRequest(email, code))
            resetToken = response.data?.resetToken
                ?: return AuthResult.Error("Something went wrong. Please try again.")
            AuthResult.Success(response.message.orEmpty())
        } catch (e: Exception) {
            AuthResult.Error(ApiErrorInterpreter.toUserMessage(e))
        }
    }

    suspend fun submitRating(level: String): AuthResult {
        return try {
            val response = service.submitRating(RatingRequest(level))
            AuthResult.Success(response.message.orEmpty())
        } catch (e: Exception) {
            AuthResult.Error(ApiErrorInterpreter.toUserMessage(e))
        }
    }

    suspend fun resetPassword(newPassword: String, confirmPassword: String): AuthResult {
        val token = resetToken
            ?: return AuthResult.Error("Your reset session expired. Please start again.")

        return try {
            val response = service.resetPassword(
                ResetPasswordRequest(token, newPassword, confirmPassword)
            )
            resetToken = null
            AuthResult.Success(response.message)
        } catch (e: Exception) {
            AuthResult.Error(ApiErrorInterpreter.toUserMessage(e))
        }
    }

    suspend fun isLoggedIn(): Boolean = tokenManager.getRefreshToken() != null

    suspend fun isGuest(): Boolean = tokenManager.getGuestId() != null

    suspend fun getGuestId(): String? = tokenManager.getGuestId()

    suspend fun getUsername(): String? {
        return tokenManager.getUsername()
    }

    suspend fun saveUsername(username: String) {
        tokenManager.saveUsername(username.trim())
    }

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun get(context: Context): AuthRepository =
            instance ?: synchronized(this) {
                instance ?: AuthRepository(context.applicationContext).also { instance = it }
            }
    }

    fun createGamesService(): GamesService =
        RetrofitInstance.createGamesService(tokenManager)

    fun createProfileService(): ProfileService =
        RetrofitInstance.createProfileService(tokenManager)

    fun createRatingService(): RatingService =
        RetrofitInstance.createRatingService(tokenManager)

    fun createMatchmakingService(): MatchmakingService =
        RetrofitInstance.createMatchmakingService(tokenManager)

    fun createAnalysisService(): AnalysisService =
        RetrofitInstance.createAnalysisService(tokenManager)

    fun createNearbyService(): NearbyService =
        RetrofitInstance.createNearbyService(tokenManager)

    fun createBotGameService(): BotGameApiService =
        RetrofitInstance.createBotGameService(tokenManager)
}
