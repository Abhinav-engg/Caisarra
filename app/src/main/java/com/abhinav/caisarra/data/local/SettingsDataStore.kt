package com.abhinav.caisarra.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore by preferencesDataStore(
    name = "app_settings"
)

val HAPTICS_KEY = booleanPreferencesKey("haptics_enabled")

val SOUND_EFFECTS_KEY = booleanPreferencesKey("sound_effects_enabled")

val BOARD_COORDINATES_KEY = booleanPreferencesKey("board_coordinates_enabled")