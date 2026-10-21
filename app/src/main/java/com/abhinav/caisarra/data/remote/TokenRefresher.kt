package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.local.SessionExpiredEvent
import com.abhinav.caisarra.data.local.TokenExpiryChecker
import com.abhinav.caisarra.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class TokenRefresher(
    private val tokenManager: TokenManager,
    private val refreshClient: OkHttpClient
) {

    private val lock = Any()

    fun refreshBlocking(failedAccessToken: String?): RefreshResult {
        synchronized(lock) {
            val currentToken = runBlocking { tokenManager.getAccessToken() }
            if (currentToken != null && currentToken != failedAccessToken) {
                return RefreshResult.Success(currentToken)
            }

            val refreshToken = runBlocking { tokenManager.getRefreshToken() }
                ?: return RefreshResult.Failed

            return try {
                val request = Request.Builder()
                    .url("${RetrofitInstance.BASE_URL}/refresh")
                    .post("{}".toRequestBody("application/json".toMediaType()))
                    .header("Cookie", "refresh_token=$refreshToken")
                    .build()

                val response = refreshClient.newCall(request).execute()
                val code = response.code
                response.close()

                when {
                    code == 200 -> {
                        val cookies = response.headers("Set-Cookie")
                        val newAccess = cookieValue(cookies, "access_token")
                        val newRefresh = cookieValue(cookies, "refresh_token")

                        if (newAccess != null && newRefresh != null) {
                            runBlocking { tokenManager.saveTokens(newAccess, newRefresh) }
                            RefreshResult.Success(newAccess)
                        } else {
                            RefreshResult.Failed
                        }
                    }
                    code == 401 -> {
                        runBlocking { tokenManager.clearTokens() }
                        SessionExpiredEvent.emit()
                        RefreshResult.Rejected
                    }
                    else -> RefreshResult.Failed
                }
            } catch (_: Exception) {
                RefreshResult.Failed
            }
        }
    }

    suspend fun refreshSuspend(): RefreshResult {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val currentToken = tokenManager.getAccessToken()
            refreshBlocking(currentToken)
        }
    }

    suspend fun getValidAccessToken(): String? {
        val token = tokenManager.getAccessToken()
        if (token != null && !TokenExpiryChecker.isExpiredOrExpiringSoon(token)) {
            return token
        }

        return when (val result = refreshSuspend()) {
            is RefreshResult.Success -> result.accessToken
            else -> null
        }
    }

    private fun cookieValue(cookies: List<String>, name: String): String? =
        cookies
            .firstOrNull { it.startsWith("$name=") }
            ?.substringAfter("$name=")
            ?.substringBefore(";")
}
