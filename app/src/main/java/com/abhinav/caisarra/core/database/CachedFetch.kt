package com.abhinav.caisarra.core.database

import kotlinx.serialization.KSerializer
import java.io.IOException

suspend fun <T> cachedFetch(
    key: String,
    serializer: KSerializer<T>,
    fetch: suspend () -> T
): T = try {
    fetch().also { ResponseCache.put(key, serializer, it) }
} catch (e: IOException) {
    ResponseCache.get(key, serializer) ?: throw e
}
