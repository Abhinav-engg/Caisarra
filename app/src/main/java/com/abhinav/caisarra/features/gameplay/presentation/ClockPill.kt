package com.abhinav.caisarra.features.gameplay.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.presentation.game.components.formatClockTime
import com.caisaara.ui.theme.PuzzleCardBorder
import com.caisaara.ui.theme.PuzzleHeaderBg
import com.caisaara.ui.theme.StatsCyan

@Composable
fun ClockPill(timeMillis: Long, isActive: Boolean, isDailyMode: Boolean, modifier: Modifier = Modifier) {
    val background by animateColorAsState(if (isActive) StatsCyan else PuzzleHeaderBg, label = "clockBg")
    val border by animateColorAsState(if (isActive) StatsCyan else PuzzleCardBorder, label = "clockBorder")
    val shape = RoundedCornerShape(14.dp)
    Text(
        text = formatClockTime(timeMillis, isDailyMode),
        color = if (isActive) Color.Black else Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = JetBrainsMono,
        modifier = modifier
            .background(background, shape)
            .border(1.dp, border, shape)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    )
}
