package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.api.AuthService
import com.abhinav.caisarra.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val refreshService: AuthService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val refreshToken = runBlocking { tokenManager.getRefreshToken() } ?: return null

        val newAccessToken = runBlocking {
            try {
                val refreshResponse = refreshService.refresh(RefreshTokenRequest(refreshToken))
                tokenManager.saveAccessToken(refreshResponse.accessToken)
                refreshResponse.accessToken
            } catch (e: Exception) {
                tokenManager.clearTokens()
                null
            }
        } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
