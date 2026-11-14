package com.abhinav.caisarra.features.streakwidget.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.abhinav.caisarra.data.remote.dto.StreakDayDto
import com.caisaara.ui.theme.StatsBarSlate
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeMicro
import com.caisaara.ui.theme.ThemeNeonDeep
import com.caisaara.ui.theme.ThemeNeonSky

@Composable
fun RowScope.StreakWeekDay(day: StreakDayDto) {
    val (fill, mark, markColor) = when (day.status.lowercase()) {
        "played" -> Triple(ThemeNeonSky, "✓", StatsBg)
        "frozen" -> Triple(ThemeNeonDeep, "❄", StatsCyan)
        else -> Triple(StatsBarSlate, "", StatsBg)
    }
    Column(GlanceModifier.defaultWeight(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(GlanceModifier.size(24.dp).background(fill).cornerRadius(12.dp), contentAlignment = Alignment.Center) {
            Text(text = mark, style = TextStyle(color = ColorProvider(markColor), fontSize = TextSizeCaption, fontWeight = FontWeight.Bold))
        }
        Spacer(GlanceModifier.height(4.dp))
        Text(text = day.weekday.take(1).uppercase(), style = TextStyle(color = ColorProvider(StatsTextMuted), fontSize = TextSizeMicro, fontWeight = FontWeight.Medium))
    }
}
