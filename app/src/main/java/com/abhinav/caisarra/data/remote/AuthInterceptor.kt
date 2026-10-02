package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

internal val PUBLIC_PATHS = setOf(
    "register", "login", "guest-login", "refresh", "logout", "logout-all",
    "verify-registration",
    "auth/forgot-password", "auth/verify-reset-code", "auth/reset-password"
)

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath.trimStart('/')

        if (path in PUBLIC_PATHS) {
            return chain.proceed(request)
        }

        val accessToken = runBlocking { tokenManager.getAccessToken() }
        val authorizedRequest = if (accessToken != null) {
            request.newBuilder().addHeader("Authorization", "Bearer $accessToken").build()
        } else {
            request
        }

        return chain.proceed(authorizedRequest)
    }
}