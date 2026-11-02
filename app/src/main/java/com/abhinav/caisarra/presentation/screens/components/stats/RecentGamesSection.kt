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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.core.ui.theme.StatsIconSizeMedium
import com.abhinav.caisarra.core.ui.theme.StatsIconSizeSmall
import com.abhinav.caisarra.data.remote.dto.RatingRecentGame
import com.caisaara.ui.theme.StatsBarSlate
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsGreen
import com.caisaara.ui.theme.StatsLossDeltaBg
import com.caisaara.ui.theme.StatsPieceCircleDark
import com.caisaara.ui.theme.StatsPieceCircleDarkBorder
import com.caisaara.ui.theme.StatsRed
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.StatsWinDeltaBg
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeSubhead
import com.caisaara.ui.theme.TextSizeTiny
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RecentGameDateFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.US)

@Composable
internal fun RecentGamesSection(
    games: List<RatingRecentGame>,
    onGameClick: (String) -> Unit,
    onOpponentClick: (String) -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Recent Rated Games",
            fontFamily = JetBrainsMono,
            fontSize = TextSizeSubhead,
            fontWeight = FontWeight.Bold,
            color = StatsTextWhite
        )

        games.forEach { game ->
            RecentGameItem(
                game = game,
                onClick = { onGameClick(game.gameId) },
                onOpponentClick = { onOpponentClick(game.opponentUsername) }
            )
        }
    }
}
@Composable
internal fun RecentGameItem(
    game: RatingRecentGame,
    onClick: () -> Unit,
    onOpponentClick: () -> Unit = {}
) {
    val isWin = game.outcome.equals("win", ignoreCase = true)
    val isLoss = game.outcome.equals("loss", ignoreCase = true)
    val isWhite = game.color.equals("white", ignoreCase = true)

    val outcomeText = when {
        isWin -> "WIN"
        isLoss -> "LOSS"
        else -> "DRAW"
    }

    val outcomeColor = when {
        isWin -> StatsGreen
        isLoss -> StatsRed
        else -> StatsTextMuted
    }

    val outcomeBg = when {
        isWin -> StatsWinDeltaBg
        isLoss -> StatsLossDeltaBg
        else -> StatsBarSlate
    }

    val formattedDate = game.endedAt?.let {
        try {
            val instant = Instant.parse(it)
            RecentGameDateFormatter.format(instant.atZone(ZoneId.systemDefault()))
        } catch (_: Exception) {
            it.take(10)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(StatsIconSizeSmall)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isWhite) Color.White else StatsPieceCircleDark)
                        .border(1.dp, StatsPieceCircleDarkBorder, CircleShape)
                )

                Column {
                    Text(
                        text = "vs ${game.opponentUsername}",
                        fontFamily = JetBrainsMono,
                        fontSize = TextSizeBody,
                        fontWeight = FontWeight.Bold,
                        color = StatsCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { onOpponentClick() }
                    )
                    val detail = buildString {
                        if (!game.endReason.isNullOrBlank()) append(game.endReason)
                        if (formattedDate != null) {
                            if (isNotEmpty()) append(" • ")
                            append(formattedDate)
                        }
                    }
                    if (detail.isNotBlank()) {
                        Text(
                            text = detail,
                            fontFamily = JetBrainsMono,
                            fontSize = TextSizeTiny,
                            color = StatsTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(outcomeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = outcomeText,
                        fontFamily = JetBrainsMono,
                        fontSize = TextSizeTiny,
                        fontWeight = FontWeight.Bold,
                        color = outcomeColor
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = StatsTextMuted,
                    modifier = Modifier.size(StatsIconSizeMedium)
                )
            }
        }
    }
}
