package com.abhinav.caisarra.features.gameplay.data

import android.os.SystemClock
import com.abhinav.caisarra.data.remote.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

suspend fun measureLatency(): Long? = withContext(Dispatchers.IO) {
    runCatching {
        val request = Request.Builder().url(RetrofitInstance.BASE_URL).head().build()
        val start = SystemClock.elapsedRealtime()
        RetrofitInstance.socketClient.newCall(request).execute().close()
        SystemClock.elapsedRealtime() - start
    }.getOrNull()
}
