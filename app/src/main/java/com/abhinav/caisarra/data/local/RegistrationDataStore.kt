package com.abhinav.caisarra.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.registrationDataStore by preferencesDataStore(
    name = "registration_data"
)

class RegistrationDataStore(
    private val context: Context
) {

    private object Keys {
        val USERNAME = stringPreferencesKey("registration_username")
        val EMAIL = stringPreferencesKey("registration_email")
    }

    val username: Flow<String> =
        context.registrationDataStore.data.map {
            it[Keys.USERNAME] ?: ""
        }

    val email: Flow<String> =
        context.registrationDataStore.data.map {
            it[Keys.EMAIL] ?: ""
        }

    suspend fun saveUsername(username: String) {
        context.registrationDataStore.edit {
            it[Keys.USERNAME] = username
        }
    }

    suspend fun saveEmail(email: String) {
        context.registrationDataStore.edit {
            it[Keys.EMAIL] = email
        }
    }

    suspend fun clear() {
        context.registrationDataStore.edit {
            it.remove(Keys.USERNAME)
            it.remove(Keys.EMAIL)
        }
    }
}

