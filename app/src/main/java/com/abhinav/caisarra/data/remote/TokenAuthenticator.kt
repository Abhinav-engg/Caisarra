package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.api.AuthService
import com.abhinav.caisarra.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val refreshService: AuthService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath.trimStart('/')
        if (path in PUBLIC_PATHS) return null
        if (responseCount(response) >= 2) return null

        return runBlocking {
            tokenManager.refreshMutex.withLock {
                val sentToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                val currentToken = tokenManager.getAccessToken()
                if (currentToken != null && currentToken != sentToken) {
                    return@withLock withToken(response, currentToken)
                }

                val refreshToken = tokenManager.getRefreshToken() ?: return@withLock null

                try {
                    val refreshResponse = refreshService.refresh(RefreshTokenRequest(refreshToken))
                    tokenManager.saveTokens(refreshResponse.accessToken, refreshResponse.refreshToken)
                    withToken(response, refreshResponse.accessToken)
                } catch (e: HttpException) {
                    if (e.code() == 401 || e.code() == 403) {
                        tokenManager.clearTokens()
                    }
                    null
                } catch (e: Exception) {
                    null
                }
            }
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