package com.abhinav.caisarra.presentation.screens.components.stats


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.core.ui.theme.StatsCardCornerRadius
import com.abhinav.caisarra.core.ui.theme.StatsCardItemSpacing
import com.abhinav.caisarra.data.remote.dto.ModeStatsData
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
import com.abhinav.caisarra.presentation.screens.components.RatingGraphView
import com.abhinav.caisarra.presentation.viewmodel.StatsRange
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsChipBorder
import com.caisaara.ui.theme.StatsChipSelectedBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsGreen
import com.caisaara.ui.theme.StatsLossDeltaBg
import com.caisaara.ui.theme.StatsRed
import com.caisaara.ui.theme.StatsTabBg
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.StatsWinDeltaBg
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeHuge
import com.caisaara.ui.theme.TextSizeTiny
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RecentGameDateFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.US)

@Composable
 fun RatingProgressionCard(
    stats: ModeStatsData,
    selectedMode: GameMode,
    selectedRange: StatsRange,
    onSelectRange: (StatsRange) -> Unit
) {
    val delta = stats.rating - stats.startRating
    val deltaText = when {
        delta > 0 -> "+$delta"
        delta < 0 -> "$delta"
        else -> "+0"
    }
    val deltaBgColor = when {
        delta > 0 -> StatsWinDeltaBg
        delta < 0 -> StatsLossDeltaBg
        else -> StatsCardBorder
    }
    val deltaTextColor = when {
        delta > 0 -> StatsGreen
        delta < 0 -> StatsRed
        else -> StatsTextMuted
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StatsCardCornerRadius))
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, RoundedCornerShape(StatsCardCornerRadius))
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(StatsCardItemSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedMode.label.uppercase()} RATING",
                    fontFamily = JetBrainsMono,
                    fontSize = TextSizeCaption,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = StatsTextMuted
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(deltaBgColor)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = deltaText,
                            fontFamily = JetBrainsMono,
                            fontSize = TextSizeCaption,
                            fontWeight = FontWeight.Bold,
                            color = deltaTextColor
                        )
                    }

                    Text(
                        text = "this period",
                        fontFamily = JetBrainsMono,
                        fontSize = TextSizeTiny,
                        color = StatsTextMuted
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${stats.rating}",
                    fontFamily = JetBrainsMono,
                    fontSize = TextSizeHuge,
                    fontWeight = FontWeight.Bold,
                    color = StatsTextWhite,
                    lineHeight = 46.sp
                )

                Text(
                    text = "ELO",
                    fontFamily = JetBrainsMono,
                    fontSize = TextSizeBody,
                    fontWeight = FontWeight.Medium,
                    color = StatsTextMuted,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            RangeFilterChips(
                selectedRange = selectedRange,
                onSelectRange = onSelectRange
            )

            RatingGraphView(
                ratingHistory = stats.ratingHistory,
                startRating = stats.startRating,
                currentRating = stats.rating,
                bestRating = stats.bestRating,
                bestRatingAt = stats.bestRatingAt,
                lowestRating = stats.lowestRating,
                lowestRatingAt = stats.lowestRatingAt,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Started at ${stats.startRating}",
                    fontFamily = JetBrainsMono,
                    fontSize = TextSizeCaption,
                    color = StatsTextMuted
                )

                val peakText = stats.bestRating?.toString() ?: if (stats.gamesPlayed > 0) stats.rating.toString() else "—"
                Text(
                    text = "Peak $peakText",
                    fontFamily = JetBrainsMono,
                    fontSize = TextSizeCaption,
                    fontWeight = FontWeight.Medium,
                    color = StatsTextWhite
                )
            }
        }
    }
}
@Composable
fun RangeFilterChips(
    selectedRange: StatsRange,
    onSelectRange: (StatsRange) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatsRange.entries.forEach { range ->
            val isSelected = range == selectedRange
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) StatsChipSelectedBg else StatsTabBg)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) StatsCyan.copy(alpha = 0.8f) else StatsChipBorder,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelectRange(range) }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = range.label,
                    fontFamily = JetBrainsMono,
                    fontSize = TextSizeTiny,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) StatsCyan else StatsTextMuted
                )
            }
        }
    }
}
