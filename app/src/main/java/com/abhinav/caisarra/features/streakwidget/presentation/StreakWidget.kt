package com.abhinav.caisarra.features.streakwidget.presentation

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.abhinav.caisarra.features.streakwidget.data.loadStreak

class StreakWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val streak = loadStreak(context)
        provideContent { StreakWidgetContent(streak) }
    }
}
