package com.abhinav.caisarra.core.database

import android.content.Context
import com.abhinav.caisarra.data.local.ChessDatabase
import com.abhinav.caisarra.data.remote.RetrofitInstance
import kotlinx.serialization.KSerializer

object ResponseCache {

    @Volatile
    private var dao: CachedResponseDao? = null

    fun install(context: Context) {
        dao = ChessDatabase.get(context).cachedResponseDao()
    }

    suspend fun <T> put(key: String, serializer: KSerializer<T>, value: T) {
        val target = dao ?: return
        runCatching {
            target.upsert(
                CachedResponseEntity(
                    key = key,
                    json = RetrofitInstance.json.encodeToString(serializer, value),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun <T> get(key: String, serializer: KSerializer<T>): T? {
        val entry = dao?.get(key) ?: return null
        return runCatching { RetrofitInstance.json.decodeFromString(serializer, entry.json) }.getOrNull()
    }

    suspend fun clear() {
        dao?.clear()
    }
}
