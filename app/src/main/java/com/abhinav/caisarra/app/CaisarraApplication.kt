package com.abhinav.caisarra.app

import android.app.Application
import com.abhinav.caisarra.features.dailywidget.presentation.refreshDailyTurnsWidget
import com.abhinav.caisarra.features.minimateswidget.presentation.refreshMiniMatesWidget
import com.abhinav.caisarra.features.streakwidget.presentation.refreshStreakWidget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CaisarraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
        MainScope().launch { refreshDailyTurnsWidget(this@CaisarraApplication) }
        MainScope().launch { refreshStreakWidget(this@CaisarraApplication) }
        MainScope().launch { refreshMiniMatesWidget(this@CaisarraApplication) }
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
