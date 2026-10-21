package com.abhinav.caisarra.data.remote

import android.content.Context
import android.util.Log
import com.abhinav.caisarra.data.remote.dto.TournamentSocketEvent
import com.abhinav.caisarra.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class TournamentSocketClient(context: Context) {

    private val appContext = context.applicationContext
    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    private var socket: WebSocket? = null

    @Volatile
    private var generation = 0

    private val _incoming = MutableSharedFlow<TournamentSocketEvent>(extraBufferCapacity = 16)
    val incoming: SharedFlow<TournamentSocketEvent> = _incoming

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    suspend fun connect(tournamentId: String): Boolean {
        close()
        if (!AuthRepository.get(appContext).isLoggedIn()) return false
        val token = AuthRepository.get(appContext).getValidAccessToken() ?: return false

        val url = RetrofitInstance.BASE_URL.toHttpUrl().newBuilder()
            .addPathSegment("ws")
            .addPathSegment("tournament")
            .addPathSegment(tournamentId)
            .addQueryParameter("token", token)
            .build()

        val id = ++generation
        socket = RetrofitInstance.socketClient.newWebSocket(
            Request.Builder().url(url).build(),
            listenerFor(id)
        )
        return true
    }

    fun close() {
        generation++
        socket?.close(1000, null)
        socket = null
        _connected.value = false
    }

    private fun listenerFor(id: Int) = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            if (id == generation) {
                _connected.value = true
                Log.d(TAG, "connected")
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (id != generation) return
            Log.d(TAG, "message=$text")
            try {
                val event = json.decodeFromString(TournamentSocketEvent.serializer(), text)
                _incoming.tryEmit(event)
            } catch (e: Exception) {
                // Unknown or malformed event types are ignored on purpose.
                Log.w(TAG, "ignored message: ${e.message}")
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(code, reason)
            if (id == generation) _connected.value = false
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (id == generation) _connected.value = false
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (id == generation) {
                _connected.value = false
                Log.e(TAG, "connection failed code=${response?.code}", t)
            }
        }
    }

    private companion object {
        const val TAG = "TournamentSocket"
    }
}