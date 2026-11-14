package com.abhinav.caisarra.core.ui.glance

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.caisaara.ui.theme.StatsBarSlate
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.TextSizeTiny
import com.caisaara.ui.theme.ThemeNeonSky

@Composable
fun WidgetPill(text: String, filled: Boolean) {
    Text(
        text = text,
        modifier = GlanceModifier.background(if (filled) ThemeNeonSky else StatsBarSlate).cornerRadius(10.dp).padding(horizontal = 10.dp, vertical = 4.dp),
        style = TextStyle(color = ColorProvider(if (filled) StatsBg else StatsTextMuted), fontSize = TextSizeTiny, fontWeight = FontWeight.Bold),
        maxLines = 1
    )
}
