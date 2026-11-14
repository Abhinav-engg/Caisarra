package com.abhinav.caisarra.features.gameplay.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTextMuted

@Composable
fun OnlineGameStatusRow(
    isRated: Boolean,
    isDailyMode: Boolean,
    pingMs: Long?,
    connected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${if (isRated) "RATED" else "UNRATED"} · ${if (isDailyMode) "DAILY" else "LIVE"}",
            color = if (isRated) StatsCyan else StatsTextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        PingIndicator(pingMs = pingMs, connected = connected)
    }
}
