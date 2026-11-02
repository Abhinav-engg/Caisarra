package com.abhinav.caisarra.presentation.screens.components.stats


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsPillSelectedBg
import com.caisaara.ui.theme.StatsPillSelectedBorder
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.TextSizeBadge
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RecentGameDateFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.US)

@Composable
fun ModeSelectorBar(
    selectedMode: GameMode,
    onSelectMode: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GameMode.entries.forEach { mode ->
                val isSelected = mode == selectedMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) StatsPillSelectedBg else Color.Transparent)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) StatsPillSelectedBorder else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelectMode(mode) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        fontFamily = JetBrainsMono,
                        fontSize = TextSizeBadge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) StatsCyan else StatsTextMuted
                    )
                }
            }
        }
    }
}


