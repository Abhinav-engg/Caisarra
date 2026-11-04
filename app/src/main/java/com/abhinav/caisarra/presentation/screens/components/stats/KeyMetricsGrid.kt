package com.abhinav.caisarra.presentation.screens.components.stats


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.core.ui.theme.StatsCardPadding
import com.abhinav.caisarra.data.remote.dto.ModeStatsData
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeLarge
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RecentGameDateFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.US)

@Composable
 fun KeyMetricsGrid(
    stats: ModeStatsData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                value = stats.bestStreak.toString(),
                label = "Best win streak",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                value = stats.currentStreak.toString(),
                label = "Current streak",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                value = stats.lowestRating?.toString() ?: if (stats.gamesPlayed > 0) stats.rating.toString() else "—",
                label = "Lowest ELO",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                value = if (stats.gamesPlayed > 0) "${stats.winPercent}%" else "—",
                label = "All-time win rate",
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
 fun MetricCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, RoundedCornerShape(14.dp))
            .padding(StatsCardPadding)
    ) {
        Column {
            Text(
                text = value,
                fontFamily = JetBrainsMono,
                fontSize = TextSizeLarge,
                fontWeight = FontWeight.Bold,
                color = StatsTextWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontFamily = JetBrainsMono,
                fontSize = TextSizeCaption,
                color = StatsTextMuted
            )
        }
    }
}
