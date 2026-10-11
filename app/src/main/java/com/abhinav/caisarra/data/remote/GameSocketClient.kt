package com.abhinav.caisarra.data.remote

import android.util.Log
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
        Log.d("GameSocketClient", "send=$text")
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
            if (id == generation) {
                _connected.value = true
                Log.d("GameSocketClient", "connected")
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (id != generation) return
            Log.d("GameSocketClient", "message=$text")
            try {
                val element = json.parseToJsonElement(text)
                val objectMessage = element.jsonObject
                val eventType = objectMessage["type"]?.jsonPrimitive?.content
                    ?: objectMessage["event"]?.jsonPrimitive?.content
                val normalized = if (objectMessage["type"] == null && eventType != null) {
                    buildJsonObject {
                        put("type", eventType)
                        objectMessage.forEach { (key, value) ->
                            if (key != "event") put(key, value)
                        }
                    }
                } else {
                    element
                }
                val message = json.decodeFromString(
                    SocketIncoming.serializer(),
                    normalized.toString()
                )
                _incoming.tryEmit(message)
                if (message is SocketIncoming.GameStart) {
                    val asState = buildJsonObject {
                        put("type", "game_state")
                        objectMessage.forEach { (key, value) ->
                            if (key != "type" && key != "event") put(key, value)
                        }
                    }
                    _incoming.tryEmit(
                        json.decodeFromString(SocketIncoming.serializer(), asState.toString())
                    )
                }
            } catch (e: Exception) {
                Log.e("GameSocketClient", "decode failed", e)
                val type = runCatching {
                    json.parseToJsonElement(text).jsonObject["type"]?.jsonPrimitive?.content
                }.getOrNull()
                if (type == "game_start" || type == "game_started") {
                    _incoming.tryEmit(SocketIncoming.GameStart())
                }
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("GameSocketClient", "closing code=$code reason=$reason")
            webSocket.close(code, reason)
            if (id == generation) _connected.value = false
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("GameSocketClient", "closed code=$code reason=$reason")
            if (id == generation) _connected.value = false
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (id == generation) {
                _connected.value = false
                Log.e("GameSocketClient", "connection failed code=${response?.code}", t)
            }
        }
    }
}