package com.abhinav.caisarra.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.pendingOtpDataStore by preferencesDataStore(
    name = "pending_otp_session"
)

data class PendingVerification(
    val email: String,
    val purpose: String
)
class PendingOtpStore(
    private val context: Context
) {
    private object Keys {
        val EMAIL = stringPreferencesKey("pending_email")
        val PURPOSE = stringPreferencesKey("pending_purpose")
    }

    suspend fun save(email: String, purpose: String) {
        context.pendingOtpDataStore.edit { preferences ->
            preferences[Keys.EMAIL] = email
            preferences[Keys.PURPOSE] = purpose
        }
    }

    suspend fun getPendingVerification(): PendingVerification? {
        val preferences = context.pendingOtpDataStore.data.first()
        val email = preferences[Keys.EMAIL]
        val purpose = preferences[Keys.PURPOSE]
        return if (!email.isNullOrBlank() && !purpose.isNullOrBlank()) {
            PendingVerification(
                email = email,
                purpose = purpose
            )
        } else {
            null
        }
    }

    suspend fun clear() {
        context.pendingOtpDataStore.edit { preferences ->
            preferences.remove(Keys.EMAIL)
            preferences.remove(Keys.PURPOSE)
        }
    }
}
