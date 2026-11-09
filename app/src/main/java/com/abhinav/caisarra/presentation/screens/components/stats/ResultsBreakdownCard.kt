package com.abhinav.caisarra.presentation.screens.components.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.caisaara.ui.theme.StatsBarSlate
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsDivider
import com.caisaara.ui.theme.StatsRed
import com.caisaara.ui.theme.StatsSlate
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeStat
import com.caisaara.ui.theme.TextSizeTitle
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RecentGameDateFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.US)

@Composable
fun ResultsCard(
    stats: ModeStatsData,
    modeName: String
) {
    val totalGames = stats.gamesPlayed
    val winPercent = if (totalGames > 0) (stats.wins * 100 / totalGames) else 0
    val drawPercent = if (totalGames > 0) (stats.draws * 100 / totalGames) else 0
    val lossPercent = if (totalGames > 0) (stats.losses * 100 / totalGames) else 0

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
                    text = "Results",
                    fontFamily = JetBrainsMono,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatsTextWhite
                )

                Text(
                    text = "$totalGames rated games",
                    fontFamily = JetBrainsMono,
                    fontSize = TextSizeCaption,
                    color = StatsTextMuted
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${stats.wins}",
                        fontFamily = JetBrainsMono,
                        fontSize = TextSizeStat,
                        fontWeight = FontWeight.Bold,
                        color = StatsCyan
                    )
                    Text(
                        text = "Wins · $winPercent%",
                        fontFamily = JetBrainsMono,
                        fontSize = TextSizeCaption,
                        color = StatsTextMuted
                    )
                }

                if (stats.mode != "puzzle") {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${stats.draws}",
                            fontFamily = JetBrainsMono,
                            fontSize = TextSizeStat,
                            fontWeight = FontWeight.Bold,
                            color = StatsDivider
                        )
                        Text(
                            text = "Draws · $drawPercent%",
                            fontFamily = JetBrainsMono,
                            fontSize = TextSizeCaption,
                            color = StatsTextMuted
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${stats.losses}",
                        fontFamily = JetBrainsMono,
                        fontSize = TextSizeStat,
                        fontWeight = FontWeight.Bold,
                        color = StatsRed
                    )
                    Text(
                        text = "Losses · $lossPercent%",
                        fontFamily = JetBrainsMono,
                        fontSize = TextSizeCaption,
                        color = StatsTextMuted
                    )
                }
            }

            StackedProgressBar(
                wins = stats.wins,
                draws = stats.draws,
                losses = stats.losses,
                totalGames = totalGames
            )

            Text(
                text = if (totalGames > 0) {
                    "${stats.winPercent}% win rate across your rated $modeName games."
                } else {
                    "No rated $modeName games played yet."
                },
                fontFamily = JetBrainsMono,
                fontSize = TextSizeCaption,
                color = StatsTextMuted,
                lineHeight = TextSizeTitle
            )
        }
    }
}
@Composable
 fun StackedProgressBar(
    wins: Int,
    draws: Int,
    losses: Int,
    totalGames: Int
) {
    if (totalGames <= 0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(StatsBarSlate)
        )
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (wins > 0) {
            Box(
                modifier = Modifier
                    .weight((wins.toFloat() / totalGames).coerceAtLeast(0.01f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StatsCyan)
            )
        }
        if (draws > 0) {
            Box(
                modifier = Modifier
                    .weight((draws.toFloat() / totalGames).coerceAtLeast(0.01f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StatsSlate)
            )
        }
        if (losses > 0) {
            Box(
                modifier = Modifier
                    .weight((losses.toFloat() / totalGames).coerceAtLeast(0.01f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StatsRed)
            )
        }
    }
}
