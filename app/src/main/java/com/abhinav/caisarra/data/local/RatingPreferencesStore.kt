package com.abhinav.caisarra.data.local

import android.content.Context
import android.content.SharedPreferences
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode

class RatingPreferencesStore private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSelectedMode(): GameMode {
        val saved = prefs.getString(KEY_SELECTED_MODE, GameMode.RAPID.name)
        return try {
            GameMode.valueOf(saved ?: GameMode.RAPID.name)
        } catch (e: Exception) {
            GameMode.RAPID
        }
    }

    fun setSelectedMode(mode: GameMode) {
        prefs.edit().putString(KEY_SELECTED_MODE, mode.name).apply()
    }

    fun getRatingChange(mode: GameMode): Int? {
        val key = "$KEY_RATING_CHANGE_PREFIX${mode.ratingKey}"
        return if (prefs.contains(key)) {
            prefs.getInt(key, 0)
        } else {
            // Default initial seed of 18 for Rapid if not yet saved from game, to match UI specification
            if (mode == GameMode.RAPID) 18 else null
        }
    }

    fun getRatingChange(modeKey: String): Int? {
        val mode = GameMode.entries.firstOrNull { it.ratingKey.equals(modeKey, ignoreCase = true) }
            ?: GameMode.RAPID
        return getRatingChange(mode)
    }

    fun setRatingChange(mode: GameMode, change: Int) {
        setRatingChange(mode.ratingKey, change)
    }

    fun setRatingChange(modeKey: String, change: Int) {
        val normalizedKey = modeKey.trim().lowercase()
        prefs.edit()
            .putInt("$KEY_RATING_CHANGE_PREFIX$normalizedKey", change)
            .putInt(KEY_LAST_RATING_CHANGE, change)
            .putString(KEY_LAST_GAME_MODE, normalizedKey)
            .apply()
    }

    fun getLastRatingChange(): Int? {
        return if (prefs.contains(KEY_LAST_RATING_CHANGE)) {
            prefs.getInt(KEY_LAST_RATING_CHANGE, 0)
        } else null
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "rating_preferences"
        private const val KEY_SELECTED_MODE = "selected_mode"
        private const val KEY_LAST_RATING_CHANGE = "last_rating_change"
        private const val KEY_LAST_GAME_MODE = "last_game_mode"
        private const val KEY_RATING_CHANGE_PREFIX = "rating_change_"

        @Volatile
        private var instance: RatingPreferencesStore? = null

        fun get(context: Context): RatingPreferencesStore =
            instance ?: synchronized(this) {
                instance ?: RatingPreferencesStore(context).also { instance = it }
            }
    }
}
