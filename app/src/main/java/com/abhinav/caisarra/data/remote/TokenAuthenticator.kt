package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.api.AuthService
import com.abhinav.caisarra.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val refreshService: AuthService
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath.trimStart('/')
        if (path in PUBLIC_PATHS) return null
        if (responseCount(response) >= 2) return null

        synchronized(lock) {
            val sentToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            val currentToken = runBlocking { tokenManager.getAccessToken() }
            if (currentToken != null && currentToken != sentToken) {
                return withToken(response, currentToken)
            }

            val refreshToken = runBlocking { tokenManager.getRefreshToken() } ?: return null

            val newToken = try {
                runBlocking {
                    val refreshResponse = refreshService.refresh(RefreshTokenRequest(refreshToken))
                    tokenManager.saveAccessToken(refreshResponse.accessToken)
                    refreshResponse.accessToken
                }
            } catch (e: HttpException) {
                if (e.code() == 401 || e.code() == 403) {
                    runBlocking { tokenManager.clearTokens() }
                }
                return null
            } catch (e: Exception) {
                return null
            }

            return withToken(response, newToken)
        }
    }

    private fun withToken(response: Response, token: String): Request =
        response.request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

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