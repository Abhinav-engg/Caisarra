package com.abhinav.caisarra.data.remote

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.abhinav.caisarra.R
import com.abhinav.caisarra.app.CaisarraApplication
import com.abhinav.caisarra.app.MainActivity
import com.abhinav.caisarra.data.repository.AuthRepository
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
            AuthRepository.get(applicationContext).updateFcmToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data

        val title = data["title"]
            ?: remoteMessage.notification?.title
            ?: getString(R.string.app_name)

        val body = data["body"]
            ?: remoteMessage.notification?.body
            ?: ""

        val type = data["type"].orEmpty()
        val targetId = data["target_id"].orEmpty()
        val code = data["code"]
        val gameId = data["game_id"] ?: data["gameId"]
        val deepLink = data["deep_link"]
            ?: when {
                !gameId.isNullOrBlank() && type.contains("accepted", ignoreCase = true) ->
                    "https://caisaara.duckdns.org/game/$gameId"
                !code.isNullOrBlank() ->
                    "https://caisaara.duckdns.org/play/$code"
                !gameId.isNullOrBlank() ->
                    "https://caisaara.duckdns.org/game/$gameId"
                else -> null
            }
        val channelId = data["channel_id"]?.takeIf { it.isNotBlank() } ?: resolveChannel(type)

        if (type.contains("challenge", ignoreCase = true)) {
            com.abhinav.caisarra.data.repository.ChallengeManager.get(applicationContext).refreshChallenges()
        }

        showNotification(
            title = title,
            body = body,
            channelId = channelId,
            deepLink = deepLink,
            type = type,
            targetId = targetId,
            notificationId = data["notification_id"]?.toIntOrNull() ?: (System.currentTimeMillis() % 100000).toInt()
        )
    }

    private fun resolveChannel(type: String): String = when (type.uppercase()) {
        "GAME_MOVE", "TURN", "GAME_TURN" -> CaisarraApplication.CHANNEL_GAME_TURNS
        "GAME_INVITE", "CHALLENGE", "GAME_CHALLENGE" -> CaisarraApplication.CHANNEL_GAME_INVITES
        "CHAT", "DIRECT_MESSAGE", "CLUB_CHAT", "MESSAGE" -> CaisarraApplication.CHANNEL_MESSAGES
        "TOURNAMENT", "TOURNAMENT_ROUND", "TOURNAMENT_START" -> CaisarraApplication.CHANNEL_TOURNAMENTS
        "DAILY_PUZZLE", "PUZZLE", "STREAK" -> CaisarraApplication.CHANNEL_DAILY_PUZZLES
        else -> CaisarraApplication.CHANNEL_GAME_TURNS
    }

    private fun showNotification(
        title: String,
        body: String,
        channelId: String,
        deepLink: String?,
        type: String,
        targetId: String,
        notificationId: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_type", type)
            putExtra("target_id", targetId)
            if (!deepLink.isNullOrBlank()) data = Uri.parse(deepLink)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(notificationId, notification)
        } catch (_: SecurityException) {}
    }
}
