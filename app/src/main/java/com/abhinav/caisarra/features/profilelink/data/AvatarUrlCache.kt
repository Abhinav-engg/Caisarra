package com.abhinav.caisarra.features.profilelink.data

import android.content.Context
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.repository.ProfileRepository
import java.util.concurrent.ConcurrentHashMap

object AvatarUrlCache {
    private val urls = ConcurrentHashMap<String, String>()
    private val loaded = ConcurrentHashMap.newKeySet<String>()

    fun peek(username: String): String? = urls[username.lowercase()]

    suspend fun load(context: Context, username: String): String? {
        val key = username.lowercase()
        if (key in loaded) return urls[key]
        val url = runCatching {
            ProfileRepository(
                service = RetrofitInstance.createProfileService(TokenManager.get(context))
            ).getPublicProfile(username).avatarUrl
        }.onSuccess { loaded += key }.getOrNull()
        url?.takeIf { it.isNotBlank() }?.let { urls[key] = it }
        return url
    }
}
