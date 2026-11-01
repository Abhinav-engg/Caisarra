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
import com.abhinav.caisarra.data.remote.api.PuzzleApiService
import com.abhinav.caisarra.data.remote.api.RatingService
import com.abhinav.caisarra.data.local.ChessDatabase
import com.abhinav.caisarra.data.remote.NotificationSocketClient
import com.abhinav.caisarra.data.remote.dto.ApiError
import com.abhinav.caisarra.data.remote.dto.DeleteAccountRequest
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class AuthRepository private constructor(private val context: Context) {

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
            tokenManager.clearGuestId()

            val username = pendingRegistration?.username
            if (!username.isNullOrBlank()) {
                if (!username.contains("@")) {
                    tokenManager.saveUsername(username.trim())
                }
            }
            pendingRegistration = null
            syncFcmToken()
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
            tokenManager.clearGuestId()
            val userToSave = response.body()?.data?.username?.takeIf { it.isNotBlank() }
                ?: username.takeUnless { it.contains("@") }
            if (!userToSave.isNullOrBlank()) {
                tokenManager.saveUsername(userToSave.trim())
            }
            syncFcmToken()

            AuthResult.Success(response.body()?.message.orEmpty())
        } catch (e: Exception) {
            AuthResult.Error(ApiErrorInterpreter.toUserMessage(e))
        }
    }

    suspend fun getValidAccessToken(): String? = refresher.getValidAccessToken()

    suspend fun logout(): AuthResult {
        val fcmToken = tokenManager.getFcmToken()
        if (!fcmToken.isNullOrBlank()) {
            try {
                NotificationRepository.get(context).unregisterDevice(fcmToken)
            } catch (_: Exception) {}
        }

        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken != null) {
            try {
                service.logout(RefreshTokenRequest(refreshToken))
            } catch (_: Exception) {}
        }

        tokenManager.clearTokens()
        NotificationRepository.get(context).clearLocalState()
        NotificationSocketClient.get(context).disconnect()

        return AuthResult.Success("Logged out")
    }

    suspend fun deleteAccount(password: String): AuthResult {
        if (isGuest()) {
            return AuthResult.Error("Feature requires a registered account. Guests cannot delete accounts.")
        }

        val trimmedPassword = password.trim()
        if (trimmedPassword.isEmpty()) {
            return AuthResult.Error("Please enter your current password to confirm account deletion.")
        }
        if (trimmedPassword.length > 72) {
            return AuthResult.Error("Password cannot exceed 72 characters.")
        }

        return try {
            val request = DeleteAccountRequest(password = password)
            val response = service.deleteAccount(request)

            if (response.isSuccessful) {
                val fcmToken = tokenManager.getFcmToken()
                if (!fcmToken.isNullOrBlank()) {
                    try {
                        NotificationRepository.get(context).unregisterDevice(fcmToken)
                    } catch (_: Exception) {}
                }

                tokenManager.clearTokens()
                NotificationRepository.get(context).clearLocalState()
                NotificationSocketClient.get(context).disconnect()

                try {
                    ChessDatabase.get(context).clearAllTables()
                } catch (_: Exception) {
                }

                val successMessage = response.body()?.message ?: "Account deleted successfully."
                AuthResult.Success(successMessage)
            } else {
                val statusCode = response.code()
                val rawBody = response.errorBody()?.string()

                var parsedError: String? = null
                if (!rawBody.isNullOrBlank()) {
                    try {
                        val decoded = RetrofitInstance.json.decodeFromString(
                            ApiError.serializer(),
                            rawBody
                        )
                        parsedError = decoded.error
                    } catch (_: Exception) {
                    }
                }

                val errorMessage = when {
                    !parsedError.isNullOrBlank() -> parsedError
                    statusCode == 400 -> "Invalid request body. Password must be valid and at most 72 characters."
                    statusCode == 401 -> "Session expired or invalid authorization token. Please log in again."
                    statusCode == 403 -> "Password is incorrect. Please check your password and try again."
                    statusCode == 404 -> "Account not found or already deleted."
                    statusCode == 429 -> "Too many attempts. Please try again later."
                    statusCode in 500..599 -> "Internal server error. Safe to retry in a moment."
                    else -> "Failed to delete account (Error $statusCode)."
                }

                AuthResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            AuthResult.Error(ApiErrorInterpreter.toUserMessage(e))
        }
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

    val isGuestFlow: Flow<Boolean> =
        tokenManager.refreshTokenFlow.combine(tokenManager.guestIdFlow) { refresh, guestId ->
            refresh == null && !guestId.isNullOrBlank()
        }

    val isLoggedInFlow: Flow<Boolean> =
        tokenManager.refreshTokenFlow.map { it != null }

    suspend fun isLoggedIn(): Boolean = tokenManager.getRefreshToken() != null

    suspend fun isGuest(): Boolean = !isLoggedIn() && !tokenManager.getGuestId().isNullOrBlank()

    suspend fun getGuestId(): String? = if (isLoggedIn()) null else tokenManager.getGuestId()

    suspend fun clearGuestId() {
        tokenManager.clearGuestId()
    }

    suspend fun getUsername(): String? {
        return tokenManager.getUsername()
    }

    suspend fun saveUsername(username: String) {
        tokenManager.saveUsername(username.trim())
    }

    suspend fun updateFcmToken(token: String) {
        tokenManager.saveFcmToken(token)
        if (isLoggedIn() && !isGuest()) {
            syncFcmToken(token)
        }
    }

    suspend fun syncFcmToken(token: String? = null) {
        val fcmToken = token ?: tokenManager.getFcmToken() ?: return
        if (!isLoggedIn() || isGuest()) {
            android.util.Log.d("FCM_DEBUG", "syncFcmToken skipped: isLoggedIn=${isLoggedIn()}, isGuest=${isGuest()}")
            return
        }
        try {
            val res = NotificationRepository.get(context).registerDevice(fcmToken)
            android.util.Log.i("FCM_DEBUG", "syncFcmToken registerDevice result: $res")
        } catch (e: Exception) {
            android.util.Log.e("FCM_DEBUG", "syncFcmToken registerDevice threw exception", e)
        }
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

    fun createPuzzleService(): PuzzleApiService =
        RetrofitInstance.createPuzzleService(tokenManager)
}
