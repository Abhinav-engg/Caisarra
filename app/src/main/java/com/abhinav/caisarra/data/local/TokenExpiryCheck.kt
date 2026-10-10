package com.abhinav.caisarra.data.local

import android.util.Base64
import org.json.JSONObject

object TokenExpiryChecker {

    private fun payload(jwt: String): JSONObject? {
        return try {
            val decoded = Base64.decode(
                jwt.split(".")[1],
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
            JSONObject(String(decoded, Charsets.UTF_8))
        } catch (e: Exception) {
            null
        }
    }

    fun getExpiry(jwt: String): Long? =
        payload(jwt)?.optLong("exp", -1L)?.takeIf { it >= 0 }

    fun getUserId(jwt: String): Long? =
        payload(jwt)?.optLong("user_id", -1L)?.takeIf { it >= 0 }

    fun isExpiredOrExpiringSoon(jwt: String, bufferSeconds: Long = 30): Boolean {
        val exp = getExpiry(jwt) ?: return true
        val nowSeconds = System.currentTimeMillis() / 1000
        return nowSeconds >= (exp - bufferSeconds)
    }
}