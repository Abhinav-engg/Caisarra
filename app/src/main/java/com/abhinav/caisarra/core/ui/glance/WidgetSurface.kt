package com.abhinav.caisarra.core.ui.glance

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import com.caisaara.ui.theme.StatsBg

@Composable
fun WidgetSurface(onClick: Action, content: @Composable ColumnScope.() -> Unit) {
    Column(GlanceModifier.fillMaxSize().background(StatsBg).cornerRadius(20.dp).padding(12.dp).clickable(onClick), content = content)
}
