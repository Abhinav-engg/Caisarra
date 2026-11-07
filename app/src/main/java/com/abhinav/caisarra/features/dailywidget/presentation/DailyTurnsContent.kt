package com.abhinav.caisarra.features.dailywidget.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.abhinav.caisarra.features.dailywidget.domain.DailyTurn
import com.caisaara.ui.theme.EmeraldNormal
import com.caisaara.ui.theme.SlateDarker
import com.caisaara.ui.theme.SlateLight
import com.caisaara.ui.theme.SlateLightActive
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeMedium

@Composable
fun DailyTurnsContent(turns: List<DailyTurn>) {
    val myMoves = turns.count { it.isMyTurn }
    Column(GlanceModifier.fillMaxSize().background(SlateDarker).cornerRadius(16.dp).padding(12.dp)) {
        Text(text = "Daily games  ⟳", modifier = GlanceModifier.clickable(actionRunCallback<RefreshDailyTurnsAction>()), style = TextStyle(color = ColorProvider(SlateLight), fontSize = TextSizeMedium, fontWeight = FontWeight.Bold))
        Text(
            text = if (myMoves > 0) "$myMoves waiting on you" else "${turns.size} in progress",
            modifier = GlanceModifier.padding(bottom = 8.dp),
            style = TextStyle(color = ColorProvider(if (myMoves > 0) EmeraldNormal else SlateLightActive), fontSize = TextSizeCaption)
        )
        if (turns.isEmpty()) {
            Text(text = "No active daily games", style = TextStyle(color = ColorProvider(SlateLightActive), fontSize = TextSizeBody))
        } else {
            LazyColumn { items(turns, itemId = { it.gameId.hashCode().toLong() }) { DailyTurnRow(it) } }
        }
    }
}
