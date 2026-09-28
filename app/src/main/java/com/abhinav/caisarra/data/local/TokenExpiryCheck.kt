package com.abhinav.caisarra.data.local

import android.util.Base64
import org.json.JSONObject


object TokenExpiryChecker {

    fun getExpiry(jwt: String): Long? {
        return try {
            val payload = jwt.split(".")[1]
            val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val json = JSONObject(String(decoded, Charsets.UTF_8))
            json.getLong("exp")
        } catch (e: Exception) {

            null
        }
    }

    fun isExpiredOrExpiringSoon(jwt: String, bufferSeconds: Long = 30): Boolean {
        val exp = getExpiry(jwt) ?: return true
        val nowSeconds = System.currentTimeMillis() / 1000
        return nowSeconds >= (exp - bufferSeconds)
    }
}