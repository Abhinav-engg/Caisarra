package com.abhinav.caisarra.features.streakwidget.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.abhinav.caisarra.R
import com.abhinav.caisarra.app.MainActivity
import com.abhinav.caisarra.data.remote.dto.DailyStreakResponse
import com.abhinav.caisarra.data.remote.dto.StreakDayDto
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeLarge
import com.caisaara.ui.theme.ThemeNeonSky

@Composable
fun StreakWidgetContent(streak: DailyStreakResponse) {
    val week = streak.week.ifEmpty { "MTWTFSS".map { StreakDayDto(weekday = it.toString()) } }
    Row(
        modifier = GlanceModifier.fillMaxSize().background(StatsBg).cornerRadius(20.dp).padding(10.dp).clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = GlanceModifier.width(100.dp).fillMaxHeight().background(StatsCardBg).cornerRadius(16.dp).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(provider = ImageProvider(R.drawable.ic_streak_flame), contentDescription = null, modifier = GlanceModifier.size(52.dp))
            Text(text = "${streak.currentStreak}", style = TextStyle(color = ColorProvider(StatsTextWhite), fontSize = TextSizeLarge, fontWeight = FontWeight.Bold))
            Text(text = "day streak", style = TextStyle(color = ColorProvider(StatsTextMuted), fontSize = TextSizeCaption))
        }
        Spacer(GlanceModifier.width(10.dp))
        Column(modifier = GlanceModifier.defaultWeight().fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (streak.todayCompleted) "Done for today" else "Play today to keep it",
                style = TextStyle(color = ColorProvider(if (streak.todayCompleted) ThemeNeonSky else StatsTextWhite), fontSize = TextSizeBody, fontWeight = FontWeight.Bold),
                maxLines = 1
            )
            Text(text = "Best ${maxOf(streak.bestStreak, streak.currentStreak)} days", style = TextStyle(color = ColorProvider(StatsTextMuted), fontSize = TextSizeCaption))
            Spacer(GlanceModifier.height(10.dp))
            Row(GlanceModifier.fillMaxWidth().background(StatsCardBg).cornerRadius(14.dp).padding(vertical = 8.dp, horizontal = 2.dp)) {
                week.forEach { StreakWeekDay(it) }
            }
        }
    }
}
