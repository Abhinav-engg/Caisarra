package com.abhinav.caisarra.data.repository

import android.content.Context
import com.abhinav.caisarra.data.local.TokenExpiryChecker
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.dto.ForgotPasswordRequest
import com.abhinav.caisarra.data.remote.dto.LoginRequest
import com.abhinav.caisarra.data.remote.dto.RefreshTokenRequest
import com.abhinav.caisarra.data.remote.dto.RegisterRequest
import com.abhinav.caisarra.data.remote.dto.ResetPasswordRequest
import com.abhinav.caisarra.data.remote.dto.VerifyRegistrationRequest
import com.abhinav.caisarra.data.remote.dto.VerifyResetCodeRequest

class AuthRepository(context: Context) {

    private val tokenManager = TokenManager(context)
    private val service = RetrofitInstance.service
    private var pendingRegistration: RegisterRequest? = null

    suspend fun register(username: String, email: String, password: String): AuthResult {
        try {
            val response = service.register(RegisterRequest(username, email, password))
            return AuthResult.Success(response.message)
        } catch (e: Exception) {
            val message = ApiErrorInterpreter.toUserMessage(e)
            return AuthResult.Error(message)
        }
    }
    suspend fun resendRegistrationCode(): AuthResult {
        val request = pendingRegistration
            ?: return AuthResult.Error("Please sign up again.")
        try {
            val response = service.register(request)
            return AuthResult.Success(response.message)
        } catch (e: Exception) {
            val message = ApiErrorInterpreter.toUserMessage(e)
            return AuthResult.Error(message)
        }
    }
    suspend fun login(username: String, password: String): AuthResult {
        try {
            val response = service.login(LoginRequest(username, password))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            return AuthResult.Success(response.message)
        } catch (e: Exception) {
            val message = ApiErrorInterpreter.toUserMessage(e)
            return AuthResult.Error(message)
        }
    }

    suspend fun refreshAccessToken(): AuthResult {
        val refreshToken = tokenManager.getRefreshToken()

        if (refreshToken == null) {
            return AuthResult.Error("You are not logged in.")
        }

        try {
            val response = service.refresh(RefreshTokenRequest(refreshToken))
            tokenManager.saveAccessToken(response.accessToken)
            return AuthResult.Success("Token refreshed")
        } catch (e: Exception) {
            if (e is retrofit2.HttpException) tokenManager.clearTokens()
            val message = ApiErrorInterpreter.toUserMessage(e)
            return AuthResult.Error(message)
        }
    }

    suspend fun logout(): AuthResult {
        val refreshToken = tokenManager.getRefreshToken()
        tokenManager.clearTokens()

        if (refreshToken == null) {
            return AuthResult.Success("Logged out")
        }

        try {
            service.logout(RefreshTokenRequest(refreshToken))
        } catch (e: Exception) {
        }

        return AuthResult.Success("Logged out")
    }

    private var resetToken: String? = null

    suspend fun sendResetCode(email: String): AuthResult {
        try {
            val response = service.forgotPassword(ForgotPasswordRequest(email))
            return AuthResult.Success(response.message)
        } catch (e: Exception) {
            val message = ApiErrorInterpreter.toUserMessage(e)
            return AuthResult.Error(message)
        }
    }

    suspend fun verifyResetCode(email: String, code: String): AuthResult {
        try {
            val response = service.verifyResetCode(VerifyResetCodeRequest(email, code))
            resetToken = response.resetToken
            return AuthResult.Success(response.message)
        } catch (e: Exception) {
            val message = ApiErrorInterpreter.toUserMessage(e)
            return AuthResult.Error(message)
        }
    }

    suspend fun resetPassword(newPassword: String, confirmPassword: String): AuthResult {
        val token = resetToken
        if (token == null) {
            return AuthResult.Error("Your reset session expired. Please start again.")
        }

        try {
            val response = service.resetPassword(
                ResetPasswordRequest(token, newPassword, confirmPassword)
            )
            resetToken = null
            return AuthResult.Success(response.message)
        } catch (e: Exception) {
            val message = ApiErrorInterpreter.toUserMessage(e)
            return AuthResult.Error(message)
        }
    }



    suspend fun verifyRegistration(email: String, code: String): AuthResult {
        try {
            val response = service.verifyRegistration(VerifyRegistrationRequest(email, code))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            pendingRegistration = null
            return AuthResult.Success(response.message)
        } catch (e: Exception) {
            val message = ApiErrorInterpreter.toUserMessage(e)
            return AuthResult.Error(message)
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
}