package com.abhinav.caisarra.data.remote

import android.content.Context
import android.util.Log
import com.abhinav.caisarra.data.remote.dto.DirectMessageDto
import com.abhinav.caisarra.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.HttpUrl.Companion.toHttpUrl

class DirectMessageSocket(context: Context) {

    private val appContext = context.applicationContext

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val connectMutex = Mutex()

    @Volatile
    private var socket: WebSocket? = null

    @Volatile
    private var generation = 0

    private val _connected = MutableStateFlow(false)
    val connected = _connected.asStateFlow()

    private val _connecting = MutableStateFlow(false)
    val connecting = _connecting.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError = _connectionError.asStateFlow()

    private val _incoming = MutableSharedFlow<DirectMessageDto>(
        extraBufferCapacity = 128
    )
    val incoming = _incoming.asSharedFlow()

    suspend fun connect() = connectMutex.withLock {
        if (_connected.value || _connecting.value) {
            return@withLock
        }

        _connecting.value = true
        _connectionError.value = null

        val attemptGeneration = ++generation

        try {
            val authRepository = AuthRepository.get(appContext)

            if (!authRepository.isLoggedIn()) {
                failConnection(
                    attemptGeneration,
                    "Please sign in to use direct messages."
                )
                return@withLock
            }

            val token = authRepository.getValidAccessToken()

            if (token.isNullOrBlank()) {
                failConnection(
                    attemptGeneration,
                    "Your session has expired. Please sign in again."
                )
                return@withLock
            }


            val baseUrl = RetrofitInstance.BASE_URL.toHttpUrl()
            val webSocketUrl = baseUrl.newBuilder()
                .scheme("https")
                .encodedPath("/ws/messages")
                .query(null)
                .addQueryParameter("token", token)
                .build()

            val request = Request.Builder()
                .url(webSocketUrl)
                .build()

            Log.d(TAG, "Opening direct-message WebSocket")

            val newSocket = RetrofitInstance.socketClient.newWebSocket(
                request,
                object : WebSocketListener() {

                    override fun onOpen(
                        webSocket: WebSocket,
                        response: Response
                    ) {
                        if (attemptGeneration != generation) {
                            webSocket.close(1000, "Connection superseded")
                            return
                        }

                        socket = webSocket
                        _connecting.value = false
                        _connected.value = true
                        _connectionError.value = null

                        Log.i(
                            TAG,
                            "Direct-message WebSocket connected; HTTP ${response.code}"
                        )
                    }

                    override fun onMessage(
                        webSocket: WebSocket,
                        text: String
                    ) {
                        if (attemptGeneration != generation) return
                        handleIncomingEvent(text)
                    }

                    override fun onClosing(
                        webSocket: WebSocket,
                        code: Int,
                        reason: String
                    ) {
                        Log.d(TAG, "Server closing WebSocket; code=$code")
                        webSocket.close(code, reason)
                    }

                    override fun onClosed(
                        webSocket: WebSocket,
                        code: Int,
                        reason: String
                    ) {
                        if (attemptGeneration != generation) return

                        _connected.value = false
                        _connecting.value = false

                        Log.w(TAG, "WebSocket closed; code=$code")
                    }

                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: Response?
                    ) {
                        if (attemptGeneration != generation) return

                        _connected.value = false
                        _connecting.value = false

                        val statusCode = response?.code
                        _connectionError.value = when (statusCode) {
                            401, 403 ->
                                "WebSocket authentication failed. Please sign in again."
                            404 ->
                                "Direct-message WebSocket endpoint was not found."
                            400 ->
                                "The server rejected the WebSocket request."
                            426 ->
                                "The server rejected the WebSocket upgrade."
                            else ->
                                "Live chat connection failed. Retrying automatically."
                        }


                        Log.e(
                            TAG,
                            "WebSocket failure; HTTP status=$statusCode, " +
                                    "exception=${t.javaClass.simpleName}: ${t.message}"
                        )
                    }
                }
            )

            if (attemptGeneration == generation) {
                socket = newSocket
            } else {
                newSocket.cancel()
            }

        } catch (e: Exception) {
            failConnection(
                attemptGeneration,
                "Unable to start live chat. Retrying automatically."
            )
            Log.e(TAG, "Unable to create direct-message WebSocket", e)
        }
    }

    private fun handleIncomingEvent(text: String) {
        try {
            val root = json.parseToJsonElement(text) as? JsonObject
                ?: return

            val eventType = root["type"]
                ?.jsonPrimitive
                ?.contentOrNull
                ?: root["event"]
                    ?.jsonPrimitive
                    ?.contentOrNull
                ?: ""

            when (eventType.lowercase()) {
                "connected", "ping", "pong", "ack", "history" -> return

                "error" -> {
                    _connectionError.value =
                        root["message"]
                            ?.jsonPrimitive
                            ?.contentOrNull
                            ?: root["error"]
                                ?.jsonPrimitive
                                ?.contentOrNull
                                    ?: "The chat server reported an error."
                    Log.w(TAG, "Server returned a WebSocket error event")
                    return
                }

                "direct_message" -> {
                    val payload = root["message"] as? JsonObject
                    if (payload == null) {
                        Log.w(TAG, "direct_message event had no message object")
                        return
                    }

                    fun getLong(key: String): Long =
                        payload[key]
                            ?.jsonPrimitive
                            ?.contentOrNull
                            ?.toLongOrNull()
                            ?: 0L

                    val messageId = getLong("id")
                    val senderId = getLong("sender_id")
                    val receiverId = getLong("receiver_id")
                    val messageText = payload["message"]
                        ?.jsonPrimitive
                        ?.contentOrNull
                    val createdAt = payload["created_at"]
                        ?.jsonPrimitive
                        ?.contentOrNull

                    if (
                        messageId <= 0L ||
                        senderId <= 0L ||
                        receiverId <= 0L ||
                        messageText.isNullOrBlank()
                    ) {
                        Log.w(TAG, "Ignoring incomplete direct-message event")
                        return
                    }

                    val message = DirectMessageDto(
                        id = messageId,
                        senderId = senderId,
                        receiverId = receiverId,
                        message = messageText,
                        createdAt = createdAt
                    )

                    if (_incoming.tryEmit(message)) {
                        Log.d(TAG, "Incoming direct message emitted; id=$messageId")
                    } else {
                        Log.w(TAG, "Incoming direct message could not be emitted")
                    }
                }

                else -> Log.d(TAG, "Ignoring WebSocket event type: $eventType")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse incoming WebSocket event", e)
        }
    }

    private fun failConnection(
        attemptGeneration: Int,
        message: String
    ) {
        if (attemptGeneration != generation) return

        _connecting.value = false
        _connected.value = false
        _connectionError.value = message
    }

    @Synchronized
    fun close() {
        generation++

        val oldSocket = socket
        socket = null

        _connected.value = false
        _connecting.value = false
        _connectionError.value = null

        oldSocket?.close(1000, "Leaving direct chat")
    }

    companion object {
        private const val TAG = "DirectMessageSocket"
    }
}
