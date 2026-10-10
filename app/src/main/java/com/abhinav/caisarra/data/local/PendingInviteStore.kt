package com.abhinav.caisarra.data.local

import android.content.Context

class PendingInviteStore private constructor(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(code: String) {
        prefs.edit()
            .putString(KEY_CODE, code)
            .putLong(KEY_SAVED_AT, System.currentTimeMillis())
            .apply()
    }

    fun consume(): String? {
        val code = prefs.getString(KEY_CODE, null)
        val savedAt = prefs.getLong(KEY_SAVED_AT, 0L)
        clear()
        if (code.isNullOrBlank()) return null
        if (System.currentTimeMillis() - savedAt > MAX_AGE_MS) return null
        return code
    }

    fun clear() {
        prefs.edit().remove(KEY_CODE).remove(KEY_SAVED_AT).apply()
    }

    companion object {
        private const val PREFS_NAME = "pending_invite"
        private const val KEY_CODE = "code"
        private const val KEY_SAVED_AT = "saved_at"
        private const val MAX_AGE_MS = 60 * 60 * 1000L

        @Volatile
        private var instance: PendingInviteStore? = null

        fun get(context: Context): PendingInviteStore =
            instance ?: synchronized(this) {
                instance ?: PendingInviteStore(context).also { instance = it }
            }
    }
}