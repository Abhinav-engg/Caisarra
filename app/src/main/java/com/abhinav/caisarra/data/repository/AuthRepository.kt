package com.abhinav.caisarra.data.repository

import android.content.Context
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.dto.LoginRequest
import com.abhinav.caisarra.data.remote.dto.RefreshTokenRequest
import com.abhinav.caisarra.data.remote.dto.RegisterRequest

class AuthRepository(context: Context) {

    private val tokenManager = TokenManager(context)
    private val service = RetrofitInstance.service
    suspend fun register(username: String, email: String, password: String): AuthResult {
        try {
            val response = service.register(RegisterRequest(username, email, password))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
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
            tokenManager.clearTokens()
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
}