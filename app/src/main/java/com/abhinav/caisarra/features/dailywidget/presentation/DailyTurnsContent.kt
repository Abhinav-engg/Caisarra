package com.abhinav.caisarra.features.dailywidget.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.abhinav.caisarra.app.MainActivity
import com.abhinav.caisarra.core.ui.glance.WidgetCard
import com.abhinav.caisarra.core.ui.glance.WidgetHeader
import com.abhinav.caisarra.core.ui.glance.WidgetPill
import com.abhinav.caisarra.core.ui.glance.WidgetSurface
import com.abhinav.caisarra.core.ui.glance.widgetDeepLink
import com.abhinav.caisarra.features.dailywidget.domain.DailyTurn
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.ThemeNeonSky

@Composable
fun DailyTurnsContent(turns: List<DailyTurn>) {
    val myMoves = turns.count { it.isMyTurn }
    WidgetSurface(actionStartActivity<MainActivity>()) {
        WidgetHeader("Daily Games", if (myMoves > 0) "$myMoves waiting on you" else "${turns.size} in progress", myMoves > 0) {
            Box(GlanceModifier.size(28.dp).background(StatsCardBg).cornerRadius(14.dp).clickable(actionRunCallback<RefreshDailyTurnsAction>()), contentAlignment = Alignment.Center) {
                Text(text = "⟳", style = TextStyle(color = ColorProvider(ThemeNeonSky), fontSize = TextSizeBody, fontWeight = FontWeight.Bold))
            }
        }
        if (turns.isEmpty()) {
            WidgetCard(GlanceModifier.fillMaxWidth(), widgetDeepLink(LocalContext.current, "play/1440/0")) {
                Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "No active daily games", modifier = GlanceModifier.defaultWeight(), style = TextStyle(color = ColorProvider(StatsTextMuted), fontSize = TextSizeBody))
                    WidgetPill("New game", true)
                }
            }
        } else {
            LazyColumn { items(turns, itemId = { it.gameId.hashCode().toLong() }) { DailyTurnRow(it) } }
        }
    }
}
