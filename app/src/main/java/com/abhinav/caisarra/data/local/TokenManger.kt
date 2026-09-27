package com.abhinav.caisarra.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.tokenDataStore by preferencesDataStore(name = "auth_tokens")

class TokenManager(private val context: Context){

    private object Keys{
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    val accessTokenFlow: Flow<String?> =
        context.tokenDataStore.data.map { it[Keys.ACCESS_TOKEN] }

    val refreshTokenFlow: Flow<String?> =
        context.tokenDataStore.data.map { it[Keys.REFRESH_TOKEN] }

    suspend fun getAccessToken(): String? = accessTokenFlow.first()

    suspend fun getRefreshToken(): String? = refreshTokenFlow.first()

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.tokenDataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveAccessToken(accessToken: String) {
        context.tokenDataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
        }
    }

    suspend fun clearTokens() {
        context.tokenDataStore.edit { it.clear() }
    }

}