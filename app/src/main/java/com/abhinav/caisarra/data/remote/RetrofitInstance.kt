package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.api.AuthService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    const val BASE_URL = "https://caisaara.duckdns.org"
    val json = Json { ignoreUnknownKeys = true }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val plainClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val socketClient: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    val plainService: AuthService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(plainClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create()

    private fun authenticatedRetrofit(tokenManager: TokenManager): Retrofit {
        val client = plainClient.newBuilder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .authenticator(TokenAuthenticator(tokenManager, plainService))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    fun createAuthService(tokenManager: TokenManager): AuthService =
        authenticatedRetrofit(tokenManager).create()

    fun createInviteService(tokenManager: TokenManager): InviteService =
        authenticatedRetrofit(tokenManager).create()

    fun createChatService(tokenManager: TokenManager): ChatService =
        authenticatedRetrofit(tokenManager).create()
}