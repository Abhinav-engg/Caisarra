package com.abhinav.caisarra.data.local

import android.content.Context
import android.content.SharedPreferences

class OnboardingPreferencesStore private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasSeenOnboarding(): Boolean = prefs.getBoolean(KEY_HAS_SEEN_ONBOARDING, false)

    fun setHasSeenOnboarding(hasSeen: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, hasSeen).apply()
    }

    companion object {
        private const val PREFS_NAME = "onboarding_preferences"
        private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"

        @Volatile
        private var instance: OnboardingPreferencesStore? = null

        fun get(context: Context): OnboardingPreferencesStore =
            instance ?: synchronized(this) {
                instance ?: OnboardingPreferencesStore(context).also { instance = it }
            }
    }
}
