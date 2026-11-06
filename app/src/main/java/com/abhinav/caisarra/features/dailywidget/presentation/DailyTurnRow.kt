package com.abhinav.caisarra.features.dailywidget.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.abhinav.caisarra.app.MainActivity
import com.abhinav.caisarra.features.dailywidget.domain.DailyTurn
import com.caisaara.ui.theme.EmeraldNormal
import com.caisaara.ui.theme.SlateDark
import com.caisaara.ui.theme.SlateLight
import com.caisaara.ui.theme.SlateLightActive
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeCaption

@Composable
fun DailyTurnRow(turn: DailyTurn) {
    val context = LocalContext.current
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("caisarra://game/online/${turn.gameId}"), context, MainActivity::class.java)
    val accent = ColorProvider(if (turn.isMyTurn) EmeraldNormal else SlateLightActive)
    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp).clickable(actionStartActivity(intent))
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().background(SlateDark).cornerRadius(12.dp).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(GlanceModifier.size(8.dp).background(accent).cornerRadius(4.dp))
            Spacer(GlanceModifier.size(10.dp))
            Column {
                Text(text = turn.opponent, style = TextStyle(color = ColorProvider(SlateLight), fontSize = TextSizeBody, fontWeight = FontWeight.Bold), maxLines = 1)
                Text(text = if (turn.isMyTurn) "Your move" else "Waiting for opponent", style = TextStyle(color = accent, fontSize = TextSizeCaption, fontWeight = FontWeight.Medium))
            }
        }
    }
}
