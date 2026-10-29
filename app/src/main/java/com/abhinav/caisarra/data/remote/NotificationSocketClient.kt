package com.abhinav.caisarra.data.remote

import android.content.Context
import android.util.Log
import com.abhinav.caisarra.data.remote.dto.WsNotificationData
import com.abhinav.caisarra.data.remote.dto.WsNotificationEnvelope
import com.abhinav.caisarra.data.remote.dto.WsNotificationPayload
import com.abhinav.caisarra.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

sealed interface ChallengeNotificationEvent {
    data class Received(
        val code: String,
        val gameId: String,
        val link: String?,
        val challengerId: Long,
        val challengerUsername: String,
        val title: String,
        val body: String
    ) : ChallengeNotificationEvent

    data class Accepted(
        val code: String?,
        val gameId: String,
        val link: String?,
        val opponentId: Long?,
        val opponentUsername: String?,
        val title: String,
        val body: String
    ) : ChallengeNotificationEvent

    data class Declined(
        val code: String? = null,
        val gameId: String? = null,
        val opponentId: Long?,
        val opponentUsername: String?,
        val title: String,
        val body: String
    ) : ChallengeNotificationEvent

    data class Cancelled(
        val code: String? = null,
        val gameId: String? = null,
        val challengerId: Long?,
        val challengerUsername: String?,
        val title: String,
        val body: String
    ) : ChallengeNotificationEvent
}

class NotificationSocketClient(private val context: Context) {

    private val authRepository = AuthRepository.get(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private var socket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var generation = 0
    private var isExplicitlyStopped = false

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _challengeEvents = MutableSharedFlow<ChallengeNotificationEvent>(extraBufferCapacity = 64)
    val challengeEvents: SharedFlow<ChallengeNotificationEvent> = _challengeEvents.asSharedFlow()

    fun connect() {
        scope.launch {
            mutex.withLock {
                isExplicitlyStopped = false
                connectInternal()
            }
        }
    }

    fun disconnect() {
        scope.launch {
            mutex.withLock {
                isExplicitlyStopped = true
                reconnectJob?.cancel()
                reconnectJob = null
                val curGen = ++generation
                socket?.close(1000, "Normal closure")
                socket = null
                _connected.value = false
            }
        }
    }

    private suspend fun connectInternal() {
        if (!authRepository.isLoggedIn() || authRepository.isGuest()) {
            Log.d(TAG, "Not connecting notification socket: not logged in or guest")
            return
        }

        val token = authRepository.getValidAccessToken()
        if (token.isNullOrBlank()) {
            Log.w(TAG, "No valid access token for notification socket")
            return
        }

        val currentSocket = socket
        if (_connected.value && currentSocket != null) {
            return
        }

        val baseUrl = RetrofitInstance.BASE_URL.toHttpUrl()
        val webSocketUrl = baseUrl.newBuilder()
            .encodedPath("/ws/notifications")
            .query(null)
            .addQueryParameter("token", token)
            .build()

        val request = Request.Builder()
            .url(webSocketUrl)
            .build()

        val attemptGen = ++generation
        Log.d(TAG, "Opening notification WebSocket to $webSocketUrl")

        socket = RetrofitInstance.socketClient.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    if (attemptGen != generation) {
                        webSocket.close(1000, "Connection superseded")
                        return
                    }
                    _connected.value = true
                    Log.i(TAG, "Notification WebSocket connected; HTTP ${response.code}")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    if (attemptGen != generation) return
                    handleIncomingMessage(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(code, reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (attemptGen != generation) return
                    _connected.value = false
                    Log.d(TAG, "Notification WebSocket closed; code=$code reason=$reason")
                    scheduleReconnect()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    if (attemptGen != generation) return
                    _connected.value = false
                    Log.w(TAG, "Notification WebSocket failure; HTTP ${response?.code}: ${t.message}")
                    scheduleReconnect()
                }
            }
        )
    }

    private fun scheduleReconnect() {
        if (isExplicitlyStopped) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(3000L)
            mutex.withLock {
                if (!isExplicitlyStopped && !_connected.value) {
                    connectInternal()
                }
            }
        }
    }

    private fun handleIncomingMessage(text: String) {
        try {
            Log.d(TAG, "Notification raw payload: $text")
            val payload = parseNotificationPayload(text) ?: return
            dispatchNotification(payload)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse notification payload: $text", e)
        }
    }

    private fun parseNotificationPayload(text: String): WsNotificationPayload? {
        // Try decoding envelope first: { "type": "notification", "notification": { ... } }
        val envelope = runCatching {
            RetrofitInstance.json.decodeFromString(WsNotificationEnvelope.serializer(), text)
        }.getOrNull()

        if (envelope?.notification != null) {
            return envelope.notification
        }

        // Or payload directly: { "type": "game_challenge", ... }
        return runCatching {
            RetrofitInstance.json.decodeFromString(WsNotificationPayload.serializer(), text)
        }.getOrNull()
    }

    private fun dispatchNotification(payload: WsNotificationPayload) {
        val data = payload.data
        val code = data?.code ?: payload.code.orEmpty()
        val gameId = data?.gameId ?: payload.gameId.orEmpty()
        val userId = data?.userId ?: payload.userId
        val username = data?.username ?: payload.username.orEmpty()
        val link = data?.link ?: payload.link
        val title = payload.title.orEmpty()
        val body = payload.body.orEmpty()
        val eventType = payload.type.ifBlank { payload.event.orEmpty() }.lowercase()

        when (eventType) {
            "game_challenge", "challenge_received" -> {
                val challengerId = userId ?: 0L
                val challengerUsername = username
                if (code.isNotBlank() || gameId.isNotBlank()) {
                    _challengeEvents.tryEmit(
                        ChallengeNotificationEvent.Received(
                            code = code,
                            gameId = gameId,
                            link = link,
                            challengerId = challengerId,
                            challengerUsername = challengerUsername,
                            title = title.ifBlank { "New challenge" },
                            body = body.ifBlank { "$challengerUsername challenged you to a game" }
                        )
                    )
                }
            }

            "game_challenge_accepted", "challenge_accepted" -> {
                if (gameId.isNotBlank()) {
                    _challengeEvents.tryEmit(
                        ChallengeNotificationEvent.Accepted(
                            code = code,
                            gameId = gameId,
                            link = link,
                            opponentId = userId,
                            opponentUsername = username,
                            title = title.ifBlank { "Challenge accepted" },
                            body = body.ifBlank { "${username.ifBlank { "Opponent" }} accepted your challenge!" }
                        )
                    )
                }
            }

            "game_challenge_declined", "challenge_declined" -> {
                _challengeEvents.tryEmit(
                    ChallengeNotificationEvent.Declined(
                        code = code.ifBlank { null },
                        gameId = gameId.ifBlank { null },
                        opponentId = userId,
                        opponentUsername = username,
                        title = title.ifBlank { "Challenge declined" },
                        body = body.ifBlank { "${username.ifBlank { "Opponent" }} declined your challenge" }
                    )
                )
            }

            "game_challenge_cancelled", "challenge_cancelled" -> {
                _challengeEvents.tryEmit(
                    ChallengeNotificationEvent.Cancelled(
                        code = code.ifBlank { null },
                        gameId = gameId.ifBlank { null },
                        challengerId = userId,
                        challengerUsername = username,
                        title = title.ifBlank { "Challenge cancelled" },
                        body = body.ifBlank { "${username.ifBlank { "Challenger" }} cancelled the challenge" }
                    )
                )
            }

            else -> {
                Log.d(TAG, "Unhandled notification type: ${payload.type}")
            }
        }
    }

    companion object {
        private const val TAG = "NotificationSocket"

        @Volatile
        private var instance: NotificationSocketClient? = null

        fun get(context: Context): NotificationSocketClient =
            instance ?: synchronized(this) {
                instance ?: NotificationSocketClient(context.applicationContext).also { instance = it }
            }
    }
}