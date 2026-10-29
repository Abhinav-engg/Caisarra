package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.core.ui.theme.StatsCardCornerRadius
import com.abhinav.caisarra.core.ui.theme.StatsCardItemSpacing
import com.abhinav.caisarra.core.ui.theme.StatsCardPadding
import com.abhinav.caisarra.core.ui.theme.StatsIconSizeLarge
import com.abhinav.caisarra.core.ui.theme.StatsIconSizeMedium
import com.abhinav.caisarra.core.ui.theme.StatsIconSizeSmall
import com.abhinav.caisarra.core.ui.theme.StatsModeBarHeight
import com.abhinav.caisarra.core.ui.theme.StatsProgressBarHeight
import com.abhinav.caisarra.core.ui.theme.StatsRangeChipHeight
import com.abhinav.caisarra.core.ui.theme.StatsScreenPadding
import com.abhinav.caisarra.core.ui.theme.StatsSectionSpacing
import com.abhinav.caisarra.data.remote.dto.ColorStatsDto
import com.abhinav.caisarra.data.remote.dto.ModeStatsData
import com.abhinav.caisarra.data.remote.dto.RatingRecentGame
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
import com.abhinav.caisarra.presentation.screens.components.RatingGraphView
import com.abhinav.caisarra.presentation.screens.components.StatsSkeletonScreen
import com.abhinav.caisarra.presentation.viewmodel.StatsRange
import com.abhinav.caisarra.presentation.viewmodel.StatsViewModel
import com.caisaara.ui.theme.StatsBarSlate
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsChipBorder
import com.caisaara.ui.theme.StatsChipSelectedBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsDivider
import com.caisaara.ui.theme.StatsErrorBadgeBg
import com.caisaara.ui.theme.StatsGreen
import com.caisaara.ui.theme.StatsLossDeltaBg
import com.caisaara.ui.theme.StatsPieceCircleDark
import com.caisaara.ui.theme.StatsPieceCircleDarkBorder
import com.caisaara.ui.theme.StatsPillSelectedBg
import com.caisaara.ui.theme.StatsPillSelectedBorder
import com.caisaara.ui.theme.StatsRed
import com.caisaara.ui.theme.StatsSlate
import com.caisaara.ui.theme.StatsTabBg
import com.caisaara.ui.theme.StatsTabBorder
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.StatsWinDeltaBg
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeHeading
import com.caisaara.ui.theme.TextSizeHuge
import com.caisaara.ui.theme.TextSizeLarge
import com.caisaara.ui.theme.TextSizeMedium
import com.caisaara.ui.theme.TextSizeStat
import com.caisaara.ui.theme.TextSizeSubhead
import com.caisaara.ui.theme.TextSizeTiny
import com.caisaara.ui.theme.TextSizeTitle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RecentGameDateFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.US)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onBack: () -> Unit,
    onGameClick: (String) -> Unit = {},
    onOpponentClick: (String) -> Unit = {},
    onSignIn: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val stats = state.stats
    val pullToRefreshState = rememberPullToRefreshState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
    ) {
        CaisaaraTopBar(
            title = "STATS",
            onBack = onBack
        )

        ModeSelectorBar(
            selectedMode = state.selectedMode,
            onSelectMode = viewModel::selectMode,
            modifier = Modifier.padding(horizontal = StatsScreenPadding, vertical = 8.dp)
        )

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = state.isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = StatsCardBg,
                    color = StatsCyan
                )
            }
        ) {
            when {
                state.isGuest -> {
                    GuestStatsCard(
                        onSignIn = onSignIn,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }

                state.isLoading && stats == null -> {
                    StatsSkeletonScreen()
                }

                state.error != null && stats == null -> {
                    ErrorStateCard(
                        errorMessage = state.error.orEmpty(),
                        onRetry = viewModel::retry,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }

                stats != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(StatsScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(StatsSectionSpacing)
                    ) {
                        if (state.error != null) {
                            Text(
                                text = state.error.orEmpty(),
                                fontFamily = JetBrainsMono,
                                fontSize = TextSizeCaption,
                                color = StatsRed,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(StatsErrorBadgeBg, RoundedCornerShape(8.dp))
                                    .border(1.dp, StatsRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            )
                        }

                        RatingProgressionCard(
                            stats = stats,
                            selectedMode = state.selectedMode,
                            selectedRange = state.selectedRange,
                            onSelectRange = viewModel::selectRange
                        )

                        ResultsCard(
                            stats = stats,
                            modeName = state.selectedMode.label
                        )

                        KeyMetricsGrid(stats = stats)

                        ColorPerformanceCard(
                            asWhite = stats.asWhite,
                            asBlack = stats.asBlack
                        )

                        if (stats.recentGames.isNotEmpty()) {
                            RecentGamesSection(
                                games = stats.recentGames,
                                onGameClick = onGameClick,
                                onOpponentClick = onOpponentClick
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSelectorBar(
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

@Composable
private fun RatingProgressionCard(
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
private fun RangeFilterChips(
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

@Composable
private fun ResultsCard(
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
private fun StackedProgressBar(
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

@Composable
private fun KeyMetricsGrid(
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
private fun MetricCard(
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

@Composable
private fun ColorPerformanceCard(
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

@Composable
private fun RecentGamesSection(
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
private fun RecentGameItem(
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

@Composable
private fun GuestStatsCard(
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StatsCardCornerRadius))
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, RoundedCornerShape(StatsCardCornerRadius))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(StatsCardItemSpacing)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = StatsCyan,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "Player Statistics",
                fontFamily = JetBrainsMono,
                fontSize = TextSizeTitle,
                fontWeight = FontWeight.Bold,
                color = StatsTextWhite
            )
            Text(
                text = "Detailed rating progression charts, win rates, and game histories are available for registered accounts.",
                fontFamily = JetBrainsMono,
                fontSize = TextSizeBadge,
                color = StatsTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            GeneralButton(
                text = "SIGN IN TO VIEW STATS",
                onClick = onSignIn
            )
        }
    }
}

@Composable
private fun ErrorStateCard(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StatsCardCornerRadius))
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, RoundedCornerShape(StatsCardCornerRadius))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(StatsCardItemSpacing)
        ) {
            Text(
                text = "Unable to Load Stats",
                fontFamily = JetBrainsMono,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = StatsRed
            )
            Text(
                text = errorMessage,
                fontFamily = JetBrainsMono,
                fontSize = TextSizeBadge,
                color = StatsTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            GeneralButton(
                text = "RETRY",
                onClick = onRetry
            )
        }
    }
}
