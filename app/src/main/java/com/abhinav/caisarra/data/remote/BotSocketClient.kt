package com.abhinav.caisarra.data.remote

import android.content.Context
import com.abhinav.caisarra.data.local.SessionExpiredEvent
import com.abhinav.caisarra.data.remote.dto.BotErrorMessage
import com.abhinav.caisarra.data.remote.dto.BotMoveMessage
import com.abhinav.caisarra.data.remote.dto.BotResignMessage
import com.abhinav.caisarra.data.remote.dto.BotSocketIncoming
import com.abhinav.caisarra.data.remote.dto.BotSocketOutgoing
import com.abhinav.caisarra.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class BotSocketClient(context: Context) {

    private val appContext = context.applicationContext
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        classDiscriminator = "type"
    }

    private var socket: WebSocket? = null

    @Volatile
    private var generation = 0

    private val _incoming = MutableSharedFlow<BotSocketIncoming>(extraBufferCapacity = 64)
    val incoming: Flow<BotSocketIncoming> = _incoming.asSharedFlow()

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private var currentGameId: String? = null
    private val sentTimestamps = ArrayDeque<Long>()

    suspend fun connect(gameId: String): Boolean {
        close()
        currentGameId = gameId
        if (!AuthRepository.get(appContext).isLoggedIn()) {
            return false
        }
        val token = AuthRepository.get(appContext).getValidAccessToken() ?: return false

        val url = RetrofitInstance.BASE_URL.toHttpUrl().newBuilder()
            .encodedPath("/ws/bot-games/$gameId")
            .addQueryParameter("token", token)
            .build()

        val id = ++generation
        socket = RetrofitInstance.socketClient.newWebSocket(
            Request.Builder().url(url).build(),
            listenerFor(id)
        )
        return true
    }

    suspend fun reconnect(): Boolean {
        val gameId = currentGameId ?: return false
        return connect(gameId)
    }

    fun sendMove(move: String): Boolean = send(BotMoveMessage(move))

    fun sendResign(): Boolean = send(BotResignMessage)

    fun send(message: BotSocketOutgoing): Boolean {
        val now = System.currentTimeMillis()
        synchronized(sentTimestamps) {
            while (sentTimestamps.isNotEmpty() && now - sentTimestamps.first() > 1000L) {
                sentTimestamps.removeFirst()
            }
            if (sentTimestamps.size >= 10) {
                return false
            }
            sentTimestamps.addLast(now)
        }
        val text = json.encodeToString(BotSocketOutgoing.serializer(), message)
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
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (id != generation) return
            try {
                val parsed = json.decodeFromString<BotSocketIncoming>(text)
                if (parsed is BotErrorMessage && parsed.message == "invalid or revoked token") {
                    SessionExpiredEvent.emit()
                }
                _incoming.tryEmit(parsed)
            } catch (_: Exception) {}
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
                if (response?.code == 401) {
                    SessionExpiredEvent.emit()
                }
                _incoming.tryEmit(BotErrorMessage(response?.message ?: t.message ?: "Connection failed"))
            }
        }
    }
}
