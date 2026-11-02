package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.abhinav.caisarra.core.ui.theme.StatsCardCornerRadius
import com.abhinav.caisarra.core.ui.theme.StatsCardItemSpacing
import com.abhinav.caisarra.data.remote.dto.ColorStatsDto
import com.abhinav.caisarra.presentation.screens.components.stats.StackedProgressBar
import com.caisaara.ui.theme.StatsBarSlate
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsDivider
import com.caisaara.ui.theme.StatsSlate
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeSubhead
import java.time.format.DateTimeFormatter
import java.util.Locale


// Extracted UI components from the original screen during modular refactoring.

private val RecentGameDateFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.US)


@Composable
internal fun ColorPerformanceCard(
    asWhite: ColorStatsDto?,
    asBlack: ColorStatsDto?
) {
    val whiteWins = asWhite?.wins ?: 0
    val whiteDraws = asWhite?.draws ?: 0
    val whiteLosses = asWhite?.losses ?: 0
    val whiteTotal = whiteWins + whiteDraws + whiteLosses
    val whitePct = if (whiteTotal > 0) (whiteWins * 100 / whiteTotal) else 0

    val blackWins = asBlack?.wins ?: 0
    val blackDraws = asBlack?.draws ?: 0
    val blackLosses = asBlack?.losses ?: 0
    val blackTotal = blackWins + blackDraws + blackLosses
    val blackPct = if (blackTotal > 0) (blackWins * 100 / blackTotal) else 0

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
            Text(
                text = "Performance by Color",
                fontFamily = JetBrainsMono,
                fontSize = TextSizeSubhead,
                fontWeight = FontWeight.Bold,
                color = StatsTextWhite
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, StatsDivider, CircleShape)
                        )
                        Text(
                            text = "As White",
                            fontFamily = JetBrainsMono,
                            fontSize = TextSizeBadge,
                            fontWeight = FontWeight.Medium,
                            color = StatsTextWhite
                        )
                    }

                    Text(
                        text = "$whiteWins W • $whiteDraws D • $whiteLosses L ($whitePct%)",
                        fontFamily = JetBrainsMono,
                        fontSize = TextSizeCaption,
                        color = StatsTextMuted
                    )
                }

                StackedProgressBar(whiteWins, whiteDraws, whiteLosses, whiteTotal)
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(StatsBarSlate)
                                .border(1.dp, StatsSlate, CircleShape)
                        )
                        Text(
                            text = "As Black",
                            fontFamily = JetBrainsMono,
                            fontSize = TextSizeBadge,
                            fontWeight = FontWeight.Medium,
                            color = StatsTextWhite
                        )
                    }

                    Text(
                        text = "$blackWins W • $blackDraws D • $blackLosses L ($blackPct%)",
                        fontFamily = JetBrainsMono,
                        fontSize = TextSizeCaption,
                        color = StatsTextMuted
                    )
                }

                StackedProgressBar(blackWins, blackDraws, blackLosses, blackTotal)
            }
        }
    }
}
