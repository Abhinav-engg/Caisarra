package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.local.TokenExpiryChecker
import com.abhinav.caisarra.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.HttpException

internal val PUBLIC_PATHS = setOf(
    "register", "login", "guest-login", "refresh", "logout", "logout-all",
    "verify-registration",
    "forgot-password", "verify-reset-code", "reset-password"
)

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath.trimStart('/')

        if (path in PUBLIC_PATHS) {
            return chain.proceed(request)
        }

        val accessToken = runBlocking { validAccessToken() }
        val authorizedRequest = if (accessToken != null) {
            request.newBuilder().header("Authorization", "Bearer $accessToken").build()
        } else {
            request
        }

        return chain.proceed(authorizedRequest)
    }

    private suspend fun validAccessToken(): String? {
        val accessToken = tokenManager.getAccessToken()
        if (accessToken != null && !TokenExpiryChecker.isExpiredOrExpiringSoon(accessToken)) {
            return accessToken
        }

        return tokenManager.refreshMutex.withLock {
            val currentToken = tokenManager.getAccessToken()
            if (currentToken != null && !TokenExpiryChecker.isExpiredOrExpiringSoon(currentToken)) {
                return@withLock currentToken
            }

            val refreshToken = tokenManager.getRefreshToken() ?: return@withLock null

            try {
                val response = RetrofitInstance.plainService.refresh(
                    RefreshTokenRequest(refreshToken)
                )
                tokenManager.saveTokens(response.accessToken, response.refreshToken)
                response.accessToken
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
