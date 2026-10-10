package com.abhinav.caisarra.data.remote

import android.content.Context
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.dto.SocketIncoming
import com.abhinav.caisarra.data.remote.dto.SocketOutgoing
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

class GameSocketClient(context: Context) {

    private val appContext = context.applicationContext
    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    private var socket: WebSocket? = null

    @Volatile
    private var generation = 0

    private val _incoming = MutableSharedFlow<SocketIncoming>(extraBufferCapacity = 64)
    val incoming: SharedFlow<SocketIncoming> = _incoming

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    suspend fun connect(gameId: String): Boolean {
        close()
        AuthRepository.get(appContext).isLoggedIn()
        val token = TokenManager(appContext).getAccessToken() ?: return false

        val url = RetrofitInstance.BASE_URL.toHttpUrl().newBuilder()
            .addPathSegment("ws")
            .addPathSegment("games")
            .addPathSegment(gameId)
            .addQueryParameter("token", token)
            .build()

        val id = ++generation
        socket = RetrofitInstance.socketClient.newWebSocket(
            Request.Builder().url(url).build(),
            listenerFor(id)
        )
        return true
    }

    fun send(message: SocketOutgoing): Boolean {
        val text = json.encodeToString(SocketOutgoing.serializer(), message)
        return socket?.send(text) ?: false
    }

    fun close() {
        generation++
        socket?.close(1000, null)
        socket = null
        _connected.value = false
    }

    private fun listenerFor(id: Int) = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            if (id == generation) _connected.value = true
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (id != generation) return
            try {
                _incoming.tryEmit(json.decodeFromString(SocketIncoming.serializer(), text))
            } catch (e: Exception) {
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (id == generation) _connected.value = false
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (id == generation) _connected.value = false
        }
    }
}