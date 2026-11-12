package com.abhinav.caisarra.features.streakwidget.presentation

import android.content.Context
import androidx.glance.appwidget.updateAll

suspend fun refreshStreakWidget(context: Context) {
    runCatching { StreakWidget().updateAll(context.applicationContext) }
}
