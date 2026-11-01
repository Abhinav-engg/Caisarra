package com.abhinav.caisarra.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.abhinav.caisarra.R

object NotificationHelper {

    private const val TAG = "NotificationHelper"

    const val CHANNEL_GAME_TURNS = "game_turns"
    const val CHANNEL_GAME_INVITES = "game_invites"
    const val CHANNEL_MESSAGES = "messages"
    const val CHANNEL_TOURNAMENTS = "tournaments"
    const val CHANNEL_DAILY_PUZZLES = "daily_puzzles"
    const val CHANNEL_DEFAULT = "default"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val channels = listOf(
            createChannel(
                id = CHANNEL_GAME_TURNS,
                name = "Game Turns",
                desc = "Alerts when it is your turn",
                importance = NotificationManager.IMPORTANCE_HIGH,
                soundUri = soundUri,
                audioAttributes = audioAttributes
            ),
            createChannel(
                id = CHANNEL_GAME_INVITES,
                name = "Game Invites",
                desc = "Challenges and invitations",
                importance = NotificationManager.IMPORTANCE_HIGH,
                soundUri = soundUri,
                audioAttributes = audioAttributes
            ),
            createChannel(
                id = CHANNEL_MESSAGES,
                name = "Messages",
                desc = "Direct messages and chat notifications",
                importance = NotificationManager.IMPORTANCE_HIGH,
                soundUri = soundUri,
                audioAttributes = audioAttributes
            ),
            createChannel(
                id = CHANNEL_TOURNAMENTS,
                name = "Tournaments",
                desc = "Tournament alerts and pairings",
                importance = NotificationManager.IMPORTANCE_DEFAULT,
                soundUri = soundUri,
                audioAttributes = audioAttributes
            ),
            createChannel(
                id = CHANNEL_DAILY_PUZZLES,
                name = "Daily Puzzles & Streaks",
                desc = "Daily puzzles and streak reminders",
                importance = NotificationManager.IMPORTANCE_DEFAULT,
                soundUri = soundUri,
                audioAttributes = audioAttributes
            ),
            createChannel(
                id = CHANNEL_DEFAULT,
                name = "General",
                desc = "General updates and notifications",
                importance = NotificationManager.IMPORTANCE_HIGH,
                soundUri = soundUri,
                audioAttributes = audioAttributes
            ),
            createChannel(
                id = "general",
                name = "General",
                desc = "General updates and notifications",
                importance = NotificationManager.IMPORTANCE_HIGH,
                soundUri = soundUri,
                audioAttributes = audioAttributes
            ),
            createChannel(
                id = "notifications",
                name = "Notifications",
                desc = "Game and app notifications",
                importance = NotificationManager.IMPORTANCE_HIGH,
                soundUri = soundUri,
                audioAttributes = audioAttributes
            ),
            createChannel(
                id = "caisarra",
                name = "Mosaic",
                desc = "Mosaic alerts",
                importance = NotificationManager.IMPORTANCE_HIGH,
                soundUri = soundUri,
                audioAttributes = audioAttributes
            ),
            createChannel(
                id = "fcm_fallback_notification_channel",
                name = "Alerts",
                desc = "System alerts",
                importance = NotificationManager.IMPORTANCE_HIGH,
                soundUri = soundUri,
                audioAttributes = audioAttributes
            )
        )

        nm.createNotificationChannels(channels)
    }

    private fun createChannel(
        id: String,
        name: String,
        desc: String,
        importance: Int,
        soundUri: Uri?,
        audioAttributes: AudioAttributes?
    ): NotificationChannel {
        return NotificationChannel(id, name, importance).apply {
            description = desc
            if (importance >= NotificationManager.IMPORTANCE_DEFAULT) {
                enableVibration(true)
                if (soundUri != null && audioAttributes != null) {
                    setSound(soundUri, audioAttributes)
                }
            }
        }
    }

    fun resolveChannel(type: String?): String {
        val t = type?.lowercase().orEmpty()
        return when {
            t.contains("invite") || t.contains("challenge") || t.contains("friend") ->
                CHANNEL_GAME_INVITES

            t.contains("tournament") ->
                CHANNEL_TOURNAMENTS

            t.contains("direct_message") || t.contains("chat") || t.contains("message") ||
            t.contains("comment") || t.contains("reaction") || t.contains("club") ->
                CHANNEL_MESSAGES

            t.contains("puzzle") || t.contains("streak") ->
                CHANNEL_DAILY_PUZZLES

            t.contains("turn") || t.contains("move") ->
                CHANNEL_GAME_TURNS

            else -> CHANNEL_DEFAULT
        }
    }

    private fun getValidChannelId(context: Context, channelId: String?, type: String?): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return channelId ?: resolveChannel(type)
        }

        val nm = context.getSystemService(NotificationManager::class.java)
        if (nm != null) {
            if (!channelId.isNullOrBlank() && nm.getNotificationChannel(channelId) != null) {
                return channelId
            }
            val resolved = resolveChannel(type)
            if (nm.getNotificationChannel(resolved) != null) {
                return resolved
            }
            ensureChannels(context)
            if (nm.getNotificationChannel(resolved) != null) {
                return resolved
            }
        }

        return CHANNEL_DEFAULT
    }

    fun showNotification(
        context: Context,
        title: String?,
        body: String?,
        channelId: String?,
        type: String?,
        dataMap: Map<String, String>? = null,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt(),
        deepLink: String? = null
    ) {
        val notificationManager = NotificationManagerCompat.from(context)

        if (!notificationManager.areNotificationsEnabled()) {
            Log.w(TAG, "Notification permission not granted or notifications disabled")
            return
        }

        ensureChannels(context)

        val finalTitle = title?.takeIf { it.isNotBlank() } ?: context.getString(R.string.app_name)
        val finalBody = body?.takeIf { it.isNotBlank() } ?: "You have a new notification"
        val finalChannelId = getValidChannelId(context, channelId, type)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_id", (dataMap?.get("notification_id") ?: notificationId.toString()))
            putExtra("notification_type", type.orEmpty())
            dataMap?.forEach { (k, v) ->
                putExtra(k, v)
            }
            val link = deepLink ?: dataMap?.get("deep_link") ?: dataMap?.get("link")
            if (!link.isNullOrBlank()) {
                data = Uri.parse(link)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, finalChannelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(finalTitle)
            .setContentText(finalBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(finalBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "Posted notification #$notificationId on channel $finalChannelId: $finalTitle - $finalBody")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to post notification #$notificationId", t)
        }
    }
}
