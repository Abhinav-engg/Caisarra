package com.abhinav.caisarra.data.remote

import android.content.Context
import android.util.Log
import com.abhinav.caisarra.data.remote.dto.ClubMessageDto
import com.abhinav.caisarra.data.remote.dto.SocketOutgoing
import com.abhinav.caisarra.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class ClubChatSocket(context: Context) {

    private val appContext = context.applicationContext
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    @Volatile
    private var socket: WebSocket? = null

    @Volatile
    private var generation = 0

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    private val _messages = MutableSharedFlow<ClubMessageDto>(extraBufferCapacity = 64)
    val messages: SharedFlow<ClubMessageDto> = _messages

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val errors: SharedFlow<String> = _errors

    suspend fun connect(clubId: String) {
        close()

        try {
            val auth = AuthRepository.get(appContext)
            if (!auth.isLoggedIn()) {
                _errors.tryEmit("Sign in with a registered account to use club chat.")
                return
            }

            val token = auth.getValidAccessToken()
            if (token.isNullOrBlank()) {
                _errors.tryEmit("Your session has expired. Please sign in again.")
                return
            }
            val base = RetrofitInstance.BASE_URL.toHttpUrl()
            val url = base.newBuilder()
                .addPathSegment("ws")
                .addPathSegment("clubs")
                .addPathSegment(clubId)
                .addQueryParameter("token", token)
                .build()

            val thisGeneration = ++generation
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .build()

            socket = RetrofitInstance.socketClient.newWebSocket(
                request,
                object : WebSocketListener() {

                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        if (thisGeneration == generation) {
                            _connected.value = true
                            Log.d("ClubChatSocket", "Connected to club $clubId")
                        } else {
                            webSocket.close(1000, "Superseded connection")
                        }
                    }


                    override fun onMessage(webSocket: WebSocket, text: String) {
                        if (thisGeneration != generation) return

                        try {
                            val root = json.parseToJsonElement(text).jsonObject

                            val type = root["type"]
                                ?.jsonPrimitive?.contentOrNull
                                ?.lowercase()
                                .orEmpty()
                            if (
                                type.contains("error") ||
                                type.contains("unsupported") ||
                                type == "ack" ||
                                type == "connected" ||
                                type == "pong"
                            ) {
                                Log.w("ClubChatSocket", "Server event: $text")
                                if (type.contains("error") || type.contains("unsupported")) {
                                    _errors.tryEmit(
                                        "The chat server rejected the message format. Please try again."
                                    )
                                }
                                return
                            }

                            val payload = root["data"]?.let {
                                runCatching { it.jsonObject }.getOrNull()
                            } ?: root

                            val messageText = payload["message"]
                                ?.jsonPrimitive?.contentOrNull
                                ?: payload["text"]?.jsonPrimitive?.contentOrNull

                            if (messageText.isNullOrBlank()) {
                                Log.d("ClubChatSocket", "Ignoring non-chat WebSocket frame: $text")
                                return
                            }

                            val message = ClubMessageDto(
                                id = payload["id"]
                                    ?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0L,
                                userId = payload["user_id"]
                                    ?.jsonPrimitive?.contentOrNull?.toLongOrNull()
                                    ?: payload["sender_id"]
                                        ?.jsonPrimitive?.contentOrNull?.toLongOrNull()
                                    ?: 0L,
                                username = payload["username"]
                                    ?.jsonPrimitive?.contentOrNull
                                    ?: payload["sender_name"]
                                        ?.jsonPrimitive?.contentOrNull
                                    ?: "Player",
                                message = messageText,
                                createdAt = payload["created_at"]
                                    ?.jsonPrimitive?.contentOrNull
                            )

                            _messages.tryEmit(message)
                        } catch (e: Exception) {
                            Log.e("ClubChatSocket", "Could not parse WebSocket event", e)
                        }
                    }


                    override fun onClosing(
                        webSocket: WebSocket,
                        code: Int,
                        reason: String
                    ) {
                        webSocket.close(code, reason)
                    }

                    override fun onClosed(
                        webSocket: WebSocket,
                        code: Int,
                        reason: String
                    ) {
                        if (thisGeneration == generation) {
                            _connected.value = false
                            Log.d("ClubChatSocket", "Closed: $code $reason")
                        }
                    }

                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: Response?
                    ) {
                        if (thisGeneration == generation) {
                            _connected.value = false
                            Log.e(
                                "ClubChatSocket",
                                "WebSocket failed; HTTP ${response?.code}",
                                t
                            )
                            _errors.tryEmit(
                                when (response?.code) {
                                    401, 403 ->
                                        "Chat authentication failed. Please sign in again."
                                    else ->
                                        "Chat disconnected. Check your connection and reconnect."
                                }
                            )
                        }
                    }
                }
            )
        } catch (e: Exception) {
            _connected.value = false
            Log.e("ClubChatSocket", "Unable to start club chat connection", e)
            _errors.tryEmit("Couldn't connect to club chat. Please retry.")
        }
    }

    fun sendMessage(message: String): Boolean {
        val clean = message.trim()
        if (clean.isEmpty()) return false

        if (!_connected.value) {
            _errors.tryEmit("Chat is disconnected. Reconnect before sending.")
            return false
        }

        return try {
            val payload = json.encodeToString<SocketOutgoing>(
                SocketOutgoing.Chat(clean)
            )
            val sent = socket?.send(payload) ?: false
            if (!sent) {
                _errors.tryEmit("Message could not be sent. Please try again.")
            }
            sent
        } catch (e: Exception) {
            Log.e("ClubChatSocket", "Unable to send club message", e)
            _errors.tryEmit("Message could not be sent. Please try again.")
            false
        }
    }

    fun close() {
        generation++
        socket?.close(1000, "Leaving club chat")
        socket = null
        _connected.value = false
    }
}
