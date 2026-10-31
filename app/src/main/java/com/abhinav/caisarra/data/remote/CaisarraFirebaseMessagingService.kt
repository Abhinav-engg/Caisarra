package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.R
import com.abhinav.caisarra.app.NotificationHelper
import com.abhinav.caisarra.data.remote.dto.NotificationItemDto
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.ChallengeManager
import com.abhinav.caisarra.data.repository.NotificationRepository
import com.abhinav.caisarra.presentation.invite.InviteCodeParser
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CaisarraFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            try {
                AuthRepository.get(applicationContext).updateFcmToken(token)
            } catch (e: Exception) {
                android.util.Log.e("FCM_DEBUG", "Failed to save new FCM token", e)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data.toMutableMap()

        val type = data["type"].orEmpty()
        val isChallenge = isChallengeOrInvite(type)

        val link = data["deep_link"] ?: data["link"]
        val code = extractCode(
            directCode = data["code"],
            inviteCode = data["invite_code"],
            challengeCode = data["challenge_code"],
            link = link
        )
        if (code.isNotBlank()) {
            data["code"] = code
        }

        val gameId = data["game_id"] ?: data["gameId"].orEmpty()
        val challengerId = data["challenger_id"]?.toLongOrNull()
            ?: data["challengerId"]?.toLongOrNull()
            ?: data["user_id"]?.toLongOrNull()
            ?: data["userId"]?.toLongOrNull()
            ?: 0L

        val challengerUsername = data["challenger_username"]
            ?: data["challengerUsername"]
            ?: data["username"]
            ?: data["opponent_username"]
            ?: data["opponentUsername"]
            ?: ""

        val defaultTitle = if (isChallenge) "Game Challenge" else getString(R.string.app_name)
        val defaultBody = if (isChallenge && challengerUsername.isNotBlank()) {
            "$challengerUsername challenged you to a game"
        } else if (isChallenge) {
            "You have a new game challenge"
        } else {
            ""
        }

        val title = remoteMessage.notification?.title
            ?: data["title"]
            ?: defaultTitle

        val body = remoteMessage.notification?.body
            ?: data["body"]
            ?: data["message"]
            ?: defaultBody

        val notificationIdStr = data["notification_id"]
        val notificationId = notificationIdStr?.toIntOrNull() ?: (System.currentTimeMillis() % 100000).toInt()

        val channelId = remoteMessage.notification?.channelId
            ?: data["channel_id"]?.takeIf { it.isNotBlank() }
            ?: NotificationHelper.resolveChannel(type)

        val deepLink = link ?: when {
            gameId.isNotBlank() && isAccepted(type) ->
                "https://caisaara.app/game/$gameId"
            code.isNotBlank() ->
                "https://caisaara.app/play/$code"
            gameId.isNotBlank() ->
                "https://caisaara.app/game/$gameId"
            else -> null
        }
        if (!deepLink.isNullOrBlank()) {
            data["deep_link"] = deepLink
        }

        if (isChallenge) {
            val challengeManager = ChallengeManager.get(applicationContext)
            when {
                isAccepted(type) -> {
                    challengeManager.postChallengeAccepted(code.ifBlank { null }, gameId, challengerId, challengerUsername)
                }
                isDeclined(type) -> {
                    challengeManager.postChallengeDeclined(code.ifBlank { null }, gameId.ifBlank { null }, challengerId, challengerUsername)
                }
                isCancelled(type) -> {
                    challengeManager.postChallengeCancelled(code.ifBlank { null }, gameId.ifBlank { null }, challengerId, challengerUsername)
                }
                else -> {
                    challengeManager.postChallengeReceived(
                        code = code,
                        gameId = gameId,
                        link = deepLink,
                        challengerId = challengerId,
                        challengerUsername = challengerUsername,
                        title = title,
                        body = body
                    )
                }
            }
        }

        serviceScope.launch {
            NotificationRepository.get(applicationContext).fetchUnreadCount()
            val notifItem = NotificationItemDto(
                id = notificationId.toLong(),
                type = type,
                title = title,
                body = body,
                isRead = false
            )
            NotificationRepository.get(applicationContext).onNotificationReceived(notifItem)
        }

        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            body = body,
            channelId = channelId,
            type = type,
            dataMap = data,
            notificationId = notificationId,
            deepLink = deepLink
        )
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
}
