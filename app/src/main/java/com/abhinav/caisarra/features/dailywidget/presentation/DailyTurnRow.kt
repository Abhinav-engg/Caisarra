package com.abhinav.caisarra.features.dailywidget.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.abhinav.caisarra.core.ui.glance.WidgetCard
import com.abhinav.caisarra.core.ui.glance.WidgetPill
import com.abhinav.caisarra.core.ui.glance.widgetDeepLink
import com.abhinav.caisarra.features.dailywidget.domain.DailyTurn
import com.caisaara.ui.theme.StatsBarSlate
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.ThemeNeonSky

@Composable
fun DailyTurnRow(turn: DailyTurn) {
    Box(GlanceModifier.fillMaxWidth().padding(bottom = 6.dp)) {
        WidgetCard(GlanceModifier.fillMaxWidth(), widgetDeepLink(LocalContext.current, "game/online/${turn.gameId}"), turn.isMyTurn) {
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(GlanceModifier.size(28.dp).background(if (turn.isMyTurn) ThemeNeonSky else StatsBarSlate).cornerRadius(14.dp), contentAlignment = Alignment.Center) {
                    Text(text = turn.opponent.take(1).uppercase(), style = TextStyle(color = ColorProvider(if (turn.isMyTurn) StatsBg else StatsTextWhite), fontSize = TextSizeBody, fontWeight = FontWeight.Bold))
                }
                Spacer(GlanceModifier.width(10.dp))
                Text(text = turn.opponent, modifier = GlanceModifier.defaultWeight(), style = TextStyle(color = ColorProvider(StatsTextWhite), fontSize = TextSizeBody, fontWeight = FontWeight.Bold), maxLines = 1)
                WidgetPill(if (turn.isMyTurn) "Your move" else "Waiting", turn.isMyTurn)
            }
        }
    }
}
