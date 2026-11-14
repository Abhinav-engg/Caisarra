package com.abhinav.caisarra.features.minimateswidget.presentation

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.abhinav.caisarra.features.minimateswidget.data.loadMiniMatesWidget

class MiniMatesWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = loadMiniMatesWidget(context)
        provideContent { MiniMatesWidgetContent(state) }
    }
}
