package com.abhinav.caisarra.features.minimateswidget.presentation

import android.content.Context
import androidx.glance.appwidget.updateAll

suspend fun refreshMiniMatesWidget(context: Context) {
    runCatching { MiniMatesWidget().updateAll(context.applicationContext) }
}
