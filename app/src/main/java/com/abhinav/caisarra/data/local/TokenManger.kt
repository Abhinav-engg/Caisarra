package com.abhinav.caisarra.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex

private val Context.tokenDataStore by preferencesDataStore(name = "auth_tokens")



class TokenManager(private val context: Context){

    val refreshMutex = Mutex()

    private object Keys{
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val GUEST_ID = stringPreferencesKey("guest_id")
    }

    suspend fun getGuestId(): String? =
        context.tokenDataStore.data.map { it[Keys.GUEST_ID] }.first()

    suspend fun saveGuestId(guestId: String) {
        context.tokenDataStore.edit { it[Keys.GUEST_ID] = guestId }
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
            prefs.remove(Keys.GUEST_ID)
        }
    }

    suspend fun clearTokens() {
        context.tokenDataStore.edit { it.clear() }
    }
}