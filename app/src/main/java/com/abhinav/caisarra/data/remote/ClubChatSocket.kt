
package com.abhinav.caisarra.data.remote

import android.content.Context
import android.util.Log
import com.abhinav.caisarra.data.remote.dto.ClubMessageDto
import com.abhinav.caisarra.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
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

    private val _messages =
        MutableSharedFlow<ClubMessageDto>(extraBufferCapacity = 64)
    val messages: SharedFlow<ClubMessageDto> = _messages

    private val _errors =
        MutableSharedFlow<String>(extraBufferCapacity = 8)
    val errors: SharedFlow<String> = _errors

    suspend fun connect(clubId: String) {
        close()

        try {
            val auth = AuthRepository.get(appContext)

            if (!auth.isLoggedIn()) {
                _errors.tryEmit(
                    "Sign in with a registered account to use club chat."
                )
                return
            }

            val token = auth.getValidAccessToken()

            if (token.isNullOrBlank()) {
                _errors.tryEmit(
                    "Your session has expired. Please sign in again."
                )
                return
            }

            val url = RetrofitInstance.BASE_URL
                .toHttpUrl()
                .newBuilder()
                .addPathSegment("ws")
                .addPathSegment("clubs")
                .addPathSegment(clubId)
                .addQueryParameter("token", token)
                .build()

            val thisGeneration = ++generation

            val request = Request.Builder()
                .url(url)
                .build()

            socket = RetrofitInstance.socketClient.newWebSocket(
                request,
                object : WebSocketListener() {

                    override fun onOpen(
                        webSocket: WebSocket,
                        response: Response
                    ) {
                        if (thisGeneration != generation) {
                            webSocket.close(1000, "Superseded connection")
                            return
                        }

                        _connected.value = true
                        Log.d(
                            "ClubChatSocket",
                            "Connected to club $clubId"
                        )
                    }

                    override fun onMessage(
                        webSocket: WebSocket,
                        text: String
                    ) {
                        if (thisGeneration != generation) return

                        try {
                            val root = json.parseToJsonElement(text)
                                    as? JsonObject ?: return

                            val type = root["type"]
                                ?.jsonPrimitive
                                ?.contentOrNull
                                ?: root["event"]
                                    ?.jsonPrimitive
                                    ?.contentOrNull
                                ?: ""

                            val normalizedType = type.lowercase()
                            if (normalizedType == "history") {
                                Log.d(
                                    "ClubChatSocket",
                                    "Received initial chat history event"
                                )
                                return
                            }

                            if (
                                normalizedType == "ack" ||
                                normalizedType == "connected" ||
                                normalizedType == "pong" ||
                                normalizedType == "ping"
                            ) {
                                return
                            }

                            if (
                                normalizedType.contains("error") ||
                                normalizedType.contains("unsupported")
                            ) {
                                Log.w(
                                    "ClubChatSocket",
                                    "Server rejected chat event"
                                )
                                _errors.tryEmit(
                                    (root["message"] as? kotlinx.serialization.json.JsonPrimitive)
                                        ?.contentOrNull
                                        ?: (root["error"] as? kotlinx.serialization.json.JsonPrimitive)
                                            ?.contentOrNull
                                        ?: "The chat server rejected the request."
                                )
                                return
                            }

                            val payload = (root["data"] as? JsonObject)
                                ?: (root["message"] as? JsonObject)
                                ?: root

                            fun stringField(
                                obj: JsonObject,
                                vararg keys: String
                            ): String? {
                                for (key in keys) {
                                    val value = obj[key] as? kotlinx.serialization.json.JsonPrimitive
                                    val content = value?.contentOrNull
                                    if (!content.isNullOrBlank()) return content
                                }
                                return null
                            }

                            val messageText = stringField(payload, "message", "text")

                            if (messageText.isNullOrBlank()) {
                                Log.d(
                                    "ClubChatSocket",
                                    "Ignoring non-message event"
                                )
                                return
                            }

                            val message = ClubMessageDto(
                                id = stringField(payload, "id", "message_id") ?: "",
                                userId = stringField(payload, "user_id", "sender_id")
                                    ?.toLongOrNull() ?: 0L,
                                username = stringField(payload, "username", "sender_name")
                                    ?: "Club member",
                                message = messageText,
                                createdAt = stringField(payload, "created_at")
                            )

                            _messages.tryEmit(message)

                        } catch (e: Exception) {
                            Log.e(
                                "ClubChatSocket",
                                "Could not parse club chat event",
                                e
                            )
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
                            Log.d(
                                "ClubChatSocket",
                                "Closed: $code $reason"
                            )
                        }
                    }

                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: Response?
                    ) {
                        if (thisGeneration != generation) return

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
            )
        } catch (e: Exception) {
            _connected.value = false

            Log.e(
                "ClubChatSocket",
                "Unable to start club chat connection",
                e
            )

            _errors.tryEmit(
                "Couldn't connect to club chat. Please retry."
            )
        }
    }

    fun sendMessage(message: String): Boolean {
        val clean = message.trim()

        if (clean.isEmpty()) return false

        if (!_connected.value) {
            _errors.tryEmit(
                "Chat is disconnected. Reconnect before sending."
            )
            return false
        }

        return try {
            val payload = buildJsonObject {
                put("message", clean)
            }

            val encoded = json.encodeToString(
                JsonObject.serializer(),
                payload
            )

            val sent = socket?.send(encoded) ?: false

            if (!sent) {
                _errors.tryEmit(
                    "Message could not be sent. Please try again."
                )
            }

            sent
        } catch (e: Exception) {
            Log.e(
                "ClubChatSocket",
                "Unable to send club message",
                e
            )
            _errors.tryEmit(
                "Message could not be sent. Please try again."
            )
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
