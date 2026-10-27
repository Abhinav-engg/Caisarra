package com.abhinav.caisarra.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class CaisarraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java) ?: return
            val high = NotificationManager.IMPORTANCE_HIGH
            val default = NotificationManager.IMPORTANCE_DEFAULT

            nm.createNotificationChannels(
                listOf(
                    channel(CHANNEL_GAME_TURNS, "Game Turns", "Alerts when it is your turn", high),
                    channel(CHANNEL_GAME_INVITES, "Game Invites", "Challenges and invitations", high),
                    channel(CHANNEL_MESSAGES, "Messages", "Direct messages and chat notifications", high),
                    channel(CHANNEL_TOURNAMENTS, "Tournaments", "Tournament alerts and pairings", default),
                    channel(CHANNEL_DAILY_PUZZLES, "Daily Puzzles & Streaks", "Daily puzzles and streak reminders", default)
                )
            )
        }
    }

    private fun channel(id: String, name: String, desc: String, importance: Int) =
        NotificationChannel(id, name, importance).apply {
            description = desc
            if (importance == NotificationManager.IMPORTANCE_HIGH) enableVibration(true)
        }

    companion object {
        const val CHANNEL_GAME_TURNS = "game_turns"
        const val CHANNEL_GAME_INVITES = "game_invites"
        const val CHANNEL_MESSAGES = "messages"
        const val CHANNEL_TOURNAMENTS = "tournaments"
        const val CHANNEL_DAILY_PUZZLES = "daily_puzzles"
    }
}
