package com.abhinav.caisarra.data.remote

import android.content.Context
import android.util.Log
import com.abhinav.caisarra.app.NotificationHelper
import com.abhinav.caisarra.data.remote.dto.NotificationItemDto
import com.abhinav.caisarra.data.remote.dto.WsNotificationEnvelope
import com.abhinav.caisarra.data.remote.dto.WsNotificationPayload
import com.abhinav.caisarra.data.remote.dto.getLongData
import com.abhinav.caisarra.data.remote.dto.getStringData
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.NotificationRepository
import com.abhinav.caisarra.presentation.invite.InviteCodeParser
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
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
    private val notificationRepository = NotificationRepository.get(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private var socket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var generation = 0
    private var isExplicitlyStopped = false
    private var reconnectAttempts = 0

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
                reconnectAttempts = 0
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
                    reconnectAttempts = 0
                    Log.i(TAG, "Notification WebSocket connected; HTTP ${response.code}")
                    scope.launch {
                        notificationRepository.fetchUnreadCount()
                    }
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
            reconnectAttempts++
            val delayMs = minOf(30_000L, 2_000L * (1 shl minOf(reconnectAttempts, 4)))
            Log.d(TAG, "Scheduling notification socket reconnect in ${delayMs}ms (attempt $reconnectAttempts)")
            delay(delayMs)
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

            val envelope = runCatching {
                RetrofitInstance.json.decodeFromString(WsNotificationEnvelope.serializer(), text)
            }.getOrNull()

            if (envelope?.notification != null) {
                val item = envelope.notification
                notificationRepository.onNotificationReceived(item)
                dispatchNotificationItemToChallenges(item)
                showTrayNotificationForItem(item)
                return
            }

            val legacy = runCatching {
                RetrofitInstance.json.decodeFromString(WsNotificationPayload.serializer(), text)
            }.getOrNull()

            if (legacy != null) {
                dispatchLegacyPayload(legacy)
                showTrayNotificationForLegacy(legacy)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse notification payload: $text", e)
        }
    }

    private fun isChallengeOrInvite(type: String): Boolean {
        val t = type.lowercase()
        return t.contains("challenge") || t.contains("invite") || t.contains("invitation")
    }

    private fun isAccepted(type: String): Boolean {
        val t = type.lowercase()
        return t.contains("accepted") || t.contains("accept")
    }

    private fun isDeclined(type: String): Boolean {
        val t = type.lowercase()
        return t.contains("declined") || t.contains("decline")
    }

    private fun isCancelled(type: String): Boolean {
        val t = type.lowercase()
        return t.contains("cancelled") || t.contains("canceled") || t.contains("cancel")
    }

    private fun extractCode(directCode: String?, inviteCode: String?, challengeCode: String?, link: String?): String {
        val candidate = directCode?.takeIf { it.isNotBlank() }
            ?: inviteCode?.takeIf { it.isNotBlank() }
            ?: challengeCode?.takeIf { it.isNotBlank() }
        if (!candidate.isNullOrBlank()) {
            return InviteCodeParser.extractCode(candidate)
        }
        if (!link.isNullOrBlank()) {
            return InviteCodeParser.extractCode(link)
        }
        return ""
    }

    private fun showTrayNotificationForItem(item: NotificationItemDto) {
        val notifId = if (item.id > 0) (item.id % 100000).toInt() else (System.currentTimeMillis() % 100000).toInt()
        val dataMap = mutableMapOf<String, String>()
        dataMap["notification_id"] = item.id.toString()
        dataMap["notification_type"] = item.type
        dataMap["type"] = item.type

        item.data?.forEach { (key, element) ->
            val str = runCatching { element.jsonPrimitive.contentOrNull }.getOrNull() ?: element.toString().trim('"')
            dataMap[key] = str
        }

        val link = item.getStringData("deep_link") ?: item.getStringData("link")
        val code = extractCode(
            directCode = item.getStringData("code"),
            inviteCode = item.getStringData("invite_code"),
            challengeCode = item.getStringData("challenge_code"),
            link = link
        )
        if (code.isNotBlank()) {
            dataMap["code"] = code
        }

        val channelId = item.getStringData("channel_id") ?: NotificationHelper.resolveChannel(item.type)
        val deepLink = link ?: if (code.isNotBlank()) "https://caisaara.app/play/$code" else null

        val challengerName = item.getStringData("challenger_username")
            ?: item.getStringData("challengerUsername")
            ?: item.getStringData("username")
            ?: ""

        val finalTitle = item.title.ifBlank {
            item.getStringData("title")
                ?: if (isChallengeOrInvite(item.type)) "Game Challenge" else ""
        }
        val finalBody = item.body.ifBlank {
            item.getStringData("body")
                ?: item.getStringData("message")
                ?: if (isChallengeOrInvite(item.type) && challengerName.isNotBlank()) "$challengerName challenged you to a game"
                else if (isChallengeOrInvite(item.type)) "You have a new game challenge"
                else ""
        }

        NotificationHelper.showNotification(
            context = context,
            title = finalTitle,
            body = finalBody,
            channelId = channelId,
            type = item.type,
            dataMap = dataMap,
            notificationId = notifId,
            deepLink = deepLink
        )
    }

    private fun showTrayNotificationForLegacy(payload: WsNotificationPayload) {
        val type = payload.type.ifBlank { payload.event.orEmpty() }
        val data = payload.data
        val dataMap = mutableMapOf<String, String>()
        dataMap["notification_type"] = type
        dataMap["type"] = type
        val link = data?.link ?: payload.link
        val code = extractCode(
            directCode = data?.code ?: payload.code,
            inviteCode = null,
            challengeCode = null,
            link = link
        )
        val gameId = data?.gameId ?: payload.gameId
        val userId = data?.userId ?: payload.userId
        val username = data?.username ?: payload.username

        if (code.isNotBlank()) dataMap["code"] = code
        if (!gameId.isNullOrBlank()) dataMap["game_id"] = gameId
        if (userId != null) dataMap["user_id"] = userId.toString()
        if (!username.isNullOrBlank()) dataMap["username"] = username
        if (!link.isNullOrBlank()) dataMap["deep_link"] = link

        val title = payload.title?.takeIf { it.isNotBlank() }
            ?: if (isChallengeOrInvite(type)) "Game Challenge" else null
        val body = payload.body?.takeIf { it.isNotBlank() }
            ?: if (isChallengeOrInvite(type) && !username.isNullOrBlank()) "$username challenged you to a game"
            else if (isChallengeOrInvite(type)) "You have a new game challenge"
            else null

        if (title == null && body == null) return

        NotificationHelper.showNotification(
            context = context,
            title = title,
            body = body,
            channelId = NotificationHelper.resolveChannel(type),
            type = type,
            dataMap = dataMap,
            deepLink = link ?: if (code.isNotBlank()) "https://caisaara.app/play/$code" else null
        )
    }

    private fun dispatchNotificationItemToChallenges(item: NotificationItemDto) {
        val eventType = item.type.lowercase()
        val link = item.getStringData("link") ?: item.getStringData("deep_link")
        val code = extractCode(
            directCode = item.getStringData("code"),
            inviteCode = item.getStringData("invite_code"),
            challengeCode = item.getStringData("challenge_code"),
            link = link
        )
        val gameId = item.getStringData("game_id") ?: item.getStringData("gameId").orEmpty()
        val userId = item.getLongData("challenger_id")
            ?: item.getLongData("challengerId")
            ?: item.getLongData("user_id")
            ?: item.getLongData("userId")
            ?: item.getLongData("opponent_id")
            ?: item.getLongData("opponentId")
        val username = item.getStringData("challenger_username")
            ?: item.getStringData("challengerUsername")
            ?: item.getStringData("username")
            ?: item.getStringData("opponent_username")
            ?: item.getStringData("opponentUsername")
            ?: item.getStringData("sender").orEmpty()
        val title = item.title.ifBlank { item.getStringData("title").orEmpty() }
        val body = item.body.ifBlank { item.getStringData("body") ?: item.getStringData("message").orEmpty() }

        when {
            isAccepted(eventType) -> {
                if (gameId.isNotBlank() || code.isNotBlank()) {
                    _challengeEvents.tryEmit(
                        ChallengeNotificationEvent.Accepted(
                            code = code.ifBlank { null },
                            gameId = gameId,
                            link = link,
                            opponentId = userId,
                            opponentUsername = username.ifBlank { null },
                            title = title.ifBlank { "Challenge accepted" },
                            body = body.ifBlank { "${username.ifBlank { "Opponent" }} accepted your challenge!" }
                        )
                    )
                }
            }

            isDeclined(eventType) -> {
                _challengeEvents.tryEmit(
                    ChallengeNotificationEvent.Declined(
                        code = code.ifBlank { null },
                        gameId = gameId.ifBlank { null },
                        opponentId = userId,
                        opponentUsername = username.ifBlank { null },
                        title = title.ifBlank { "Challenge declined" },
                        body = body.ifBlank { "${username.ifBlank { "Opponent" }} declined your challenge" }
                    )
                )
            }

            isCancelled(eventType) -> {
                _challengeEvents.tryEmit(
                    ChallengeNotificationEvent.Cancelled(
                        code = code.ifBlank { null },
                        gameId = gameId.ifBlank { null },
                        challengerId = userId,
                        challengerUsername = username.ifBlank { null },
                        title = title.ifBlank { "Challenge cancelled" },
                        body = body.ifBlank { "${username.ifBlank { "Challenger" }} cancelled the challenge" }
                    )
                )
            }

            isChallengeOrInvite(eventType) -> {
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
                            title = title.ifBlank { "Game Challenge" },
                            body = body.ifBlank { "${challengerUsername.ifBlank { "A player" }} challenged you to a game" }
                        )
                    )
                }
            }

            else -> {
                Log.d(TAG, "Processed general notification item of type: $eventType")
            }
        }
    }

    private fun dispatchLegacyPayload(payload: WsNotificationPayload) {
        val data = payload.data
        val link = data?.link ?: payload.link
        val code = extractCode(
            directCode = data?.code ?: payload.code,
            inviteCode = null,
            challengeCode = null,
            link = link
        )
        val gameId = data?.gameId ?: payload.gameId.orEmpty()
        val userId = data?.userId ?: payload.userId
        val username = data?.username ?: payload.username.orEmpty()
        val title = payload.title.orEmpty()
        val body = payload.body.orEmpty()
        val eventType = payload.type.ifBlank { payload.event.orEmpty() }.lowercase()

        when {
            isAccepted(eventType) -> {
                if (gameId.isNotBlank() || code.isNotBlank()) {
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

            isDeclined(eventType) -> {
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

            isCancelled(eventType) -> {
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

            isChallengeOrInvite(eventType) -> {
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
                            title = title.ifBlank { "Game Challenge" },
                            body = body.ifBlank { "${challengerUsername.ifBlank { "A player" }} challenged you to a game" }
                        )
                    )
                }
            }

            else -> {
                Log.d(TAG, "Unhandled legacy notification type: ${payload.type}")
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
