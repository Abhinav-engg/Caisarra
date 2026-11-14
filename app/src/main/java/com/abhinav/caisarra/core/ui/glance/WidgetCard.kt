package com.abhinav.caisarra.core.ui.glance

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.padding
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsPillSelectedBg

@Composable
fun WidgetCard(
    modifier: GlanceModifier = GlanceModifier,
    onClick: Action? = null,
    highlighted: Boolean = false,
    contentAlignment: Alignment = Alignment.CenterStart,
    content: @Composable () -> Unit
) {
    val base = modifier.background(if (highlighted) StatsPillSelectedBg else StatsCardBg).cornerRadius(14.dp).padding(10.dp)
    Box(onClick?.let { base.clickable(it) } ?: base, contentAlignment = contentAlignment) { content() }
}
