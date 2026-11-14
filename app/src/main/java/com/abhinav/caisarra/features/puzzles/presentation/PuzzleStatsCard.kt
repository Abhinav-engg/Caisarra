package com.abhinav.caisarra.features.puzzles.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.caisaara.ui.theme.PuzzleCardBorder
import com.caisaara.ui.theme.PuzzleHeaderBg
import com.caisaara.ui.theme.StatsTextMuted

@Composable
fun PuzzleStatsCard(stats: List<PuzzleStat>, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PuzzleHeaderBg, shape)
            .border(1.dp, PuzzleCardBorder, shape)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        stats.forEach { stat ->
            Column(modifier = Modifier.weight(1f)) {
                AnimatedContent(targetState = stat.value, label = stat.label) { value ->
                    Text(text = value, color = stat.color, fontSize = 26.sp, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMono)
                }
                Text(text = stat.label, color = StatsTextMuted, fontSize = 13.sp)
            }
        }
    }
}
