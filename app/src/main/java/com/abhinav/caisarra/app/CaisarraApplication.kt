package com.abhinav.caisarra.app

import android.app.Application

class CaisarraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
    }

    companion object {
        const val CHANNEL_GAME_TURNS = NotificationHelper.CHANNEL_GAME_TURNS
        const val CHANNEL_GAME_INVITES = NotificationHelper.CHANNEL_GAME_INVITES
        const val CHANNEL_MESSAGES = NotificationHelper.CHANNEL_MESSAGES
        const val CHANNEL_TOURNAMENTS = NotificationHelper.CHANNEL_TOURNAMENTS
        const val CHANNEL_DAILY_PUZZLES = NotificationHelper.CHANNEL_DAILY_PUZZLES
        const val CHANNEL_DEFAULT = NotificationHelper.CHANNEL_DEFAULT
    }
}
