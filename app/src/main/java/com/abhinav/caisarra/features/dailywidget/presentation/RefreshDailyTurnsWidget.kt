package com.abhinav.caisarra.features.dailywidget.presentation

import android.content.Context
import androidx.glance.appwidget.updateAll

suspend fun refreshDailyTurnsWidget(context: Context) {
    runCatching { DailyTurnsWidget().updateAll(context.applicationContext) }
}
