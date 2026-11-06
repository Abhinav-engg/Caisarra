package com.abhinav.caisarra.features.dailywidget.presentation

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.abhinav.caisarra.features.dailywidget.data.loadDailyTurns

class DailyTurnsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val turns = loadDailyTurns(context)
        provideContent { DailyTurnsContent(turns) }
    }
}
