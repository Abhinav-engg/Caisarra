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
import com.abhinav.caisarra.data.repository.ChallengeManager
import com.abhinav.caisarra.data.repository.NotificationRepository
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

        val title = remoteMessage.notification?.title
            ?: data["title"]
            ?: getString(R.string.app_name)

        val body = remoteMessage.notification?.body
            ?: data["body"]
            ?: ""

        val type = data["type"].orEmpty()
        val notificationIdStr = data["notification_id"]
        val notificationId = notificationIdStr?.toIntOrNull() ?: (System.currentTimeMillis() % 100000).toInt()

        val channelId = data["channel_id"]?.takeIf { it.isNotBlank() } ?: resolveChannel(type)

        if (type.contains("challenge", ignoreCase = true)) {
            ChallengeManager.get(applicationContext).refreshChallenges()
        }

        serviceScope.launch {
            NotificationRepository.get(applicationContext).fetchUnreadCount()
        }

        showNotification(
            title = title,
            body = body,
            channelId = channelId,
            type = type,
            dataMap = data,
            notificationId = notificationId
        )
    }

    private fun resolveChannel(type: String): String = when (type.lowercase()) {
        "friend_request", "friend_accepted", "game_challenge", "challenge_declined", "game_invite", "challenge" ->
            CaisarraApplication.CHANNEL_GAME_INVITES

        "tournament_round_started", "tournament_finished", "tournament_cancelled", "tournament" ->
            CaisarraApplication.CHANNEL_TOURNAMENTS

        "direct_message", "chat", "message" ->
            CaisarraApplication.CHANNEL_MESSAGES

        "post_comment", "post_reaction", "club_promoted", "club_demoted", "club_new_owner", "club_kicked", "club_banned" ->
            CaisarraApplication.CHANNEL_MESSAGES

        "daily_puzzle", "puzzle", "streak" ->
            CaisarraApplication.CHANNEL_DAILY_PUZZLES

        else -> CaisarraApplication.CHANNEL_GAME_TURNS
    }

    private fun showNotification(
        title: String,
        body: String,
        channelId: String,
        type: String,
        dataMap: Map<String, String>,
        notificationId: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_id", dataMap["notification_id"])
            putExtra("notification_type", type)
            for ((key, value) in dataMap) {
                putExtra(key, value)
            }
            val deepLink = dataMap["deep_link"]
            if (!deepLink.isNullOrBlank()) {
                data = Uri.parse(deepLink)
            }
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
