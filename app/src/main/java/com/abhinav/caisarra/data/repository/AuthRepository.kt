package com.abhinav.caisarra.data.repository

import android.content.Context
import com.abhinav.caisarra.data.local.TokenExpiryChecker
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.dto.ForgotPasswordRequest
import com.abhinav.caisarra.data.remote.dto.LoginRequest
import com.abhinav.caisarra.data.remote.dto.RatingRequest
import com.abhinav.caisarra.data.remote.dto.RefreshTokenRequest
import com.abhinav.caisarra.data.remote.dto.RegisterRequest
import com.abhinav.caisarra.data.remote.dto.ResetPasswordRequest
import com.abhinav.caisarra.data.remote.dto.VerifyRegistrationRequest
import com.abhinav.caisarra.data.remote.dto.VerifyResetCodeRequest
import kotlinx.coroutines.sync.withLock
import okhttp3.Headers
import retrofit2.HttpException

class AuthRepository private constructor(context: Context) {

    private val tokenManager = TokenManager(context)
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
            AuthResult.Success(response.body()?.message.orEmpty())
        } catch (e: Exception) {
            AuthResult.Error(ApiErrorInterpreter.toUserMessage(e))
        }
    }





    suspend fun refreshAccessToken(): AuthResult = tokenManager.refreshMutex.withLock {
        val refreshToken = tokenManager.getRefreshToken()
            ?: return@withLock AuthResult.Error("You are not logged in.")

        try {
            val response = RetrofitInstance.plainService.refresh(RefreshTokenRequest(refreshToken))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            AuthResult.Success("Token refreshed")
        } catch (e: Exception) {
            if (e is HttpException && (e.code() == 401 || e.code() == 403)) tokenManager.clearTokens()
            AuthResult.Error(ApiErrorInterpreter.toUserMessage(e))
        }
    }

    suspend fun logout(): AuthResult {
        val refreshToken = tokenManager.getRefreshToken()
        tokenManager.clearTokens()

        if (refreshToken != null) {
            try {
                service.logout(RefreshTokenRequest(refreshToken))
            } catch (e: Exception) {
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

    suspend fun isLoggedIn(): Boolean {
        val accessToken = tokenManager.getAccessToken()
        if (accessToken != null && !TokenExpiryChecker.isExpiredOrExpiringSoon(accessToken)) {
            return true
        }
        refreshAccessToken()
        return tokenManager.getRefreshToken() != null
    }

    suspend fun isGuest(): Boolean = tokenManager.getGuestId() != null

    suspend fun getGuestId(): String? = tokenManager.getGuestId()

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun get(context: Context): AuthRepository =
            instance ?: synchronized(this) {
                instance ?: AuthRepository(context.applicationContext).also { instance = it }
            }
    }

}