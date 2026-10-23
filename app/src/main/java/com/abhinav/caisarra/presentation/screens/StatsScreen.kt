package com.abhinav.caisarra.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.data.remote.dto.ColorStatsDto
import com.abhinav.caisarra.data.remote.dto.ModeStatsData
import com.abhinav.caisarra.data.remote.dto.RatingRecentGame
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
import com.abhinav.caisarra.presentation.screens.components.RatingGraphView
import com.abhinav.caisarra.presentation.screens.components.StatsSkeletonScreen
import com.abhinav.caisarra.presentation.viewmodel.StatsRange
import com.abhinav.caisarra.presentation.viewmodel.StatsViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val BackgroundColor = Color(0xFF080D13)
private val CardBg = Color(0xFF0F1722)
private val CardBorderColor = Color(0xFF1B2838)
private val TextWhiteColor = Color(0xFFF1F5F9)
private val TextMutedColor = Color(0xFF82909E)
private val CyanColor = Color(0xFF22D3EE)
private val GreenColor = Color(0xFF2DD4BF)
private val RedColor = Color(0xFFF87171)
private val SlateColor = Color(0xFF475569)
private val PillSelectedBg = Color(0xFF123347)
private val PillSelectedBorder = Color(0xFF1D5473)

private val RecentGameDateFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.US)

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onBack: () -> Unit,
    onGameClick: (String) -> Unit = {},
    onSignIn: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val stats = state.stats

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
    ) {
        // Top App Bar
        TopStatsBar(
            onBack = onBack,
            onRefresh = { viewModel.refresh() },
            isRefreshing = state.isRefreshing
        )

        // Mode Selector Tabs (Bullet | Blitz | Rapid | Daily)
        ModeSelectorBar(
            selectedMode = state.selectedMode,
            onSelectMode = viewModel::selectMode,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Main Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (state.error != null) {
                            Text(
                                text = state.error.orEmpty(),
                                fontFamily = JetBrainsMono,
                                fontSize = 12.sp,
                                color = RedColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF281116), RoundedCornerShape(8.dp))
                                    .border(1.dp, RedColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            )
                        }

                        // 1. Rating Progression Card (Figma Top Card)
                        RatingProgressionCard(
                            stats = stats,
                            selectedMode = state.selectedMode,
                            selectedRange = state.selectedRange,
                            onSelectRange = viewModel::selectRange
                        )

                        // 2. Results Card (Figma Middle Card)
                        ResultsCard(
                            stats = stats,
                            modeName = state.selectedMode.label
                        )

                        // 3. Improvised Key Metric Cards (Figma Bottom Cards + Extra)
                        KeyMetricsGrid(stats = stats)

                        // 4. White vs Black Record Card
                        ColorPerformanceCard(
                            asWhite = stats.asWhite,
                            asBlack = stats.asBlack
                        )

                        // 5. Recent Games List (if any)
                        if (stats.recentGames.isNotEmpty()) {
                            RecentGamesSection(
                                games = stats.recentGames,
                                onGameClick = onGameClick
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
private fun TopStatsBar(
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF101924))
                .border(
                    width = 1.dp,
                    color = Color(0xFF202E3E),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhiteColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "STATS",
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            letterSpacing = 2.sp,
            color = TextWhiteColor
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF101924))
                .border(
                    width = 1.dp,
                    color = Color(0xFF202E3E),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing,
                modifier = Modifier.fillMaxSize()
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = CyanColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = TextMutedColor,
                        modifier = Modifier.size(20.dp)
                    )
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
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F1722))
            .border(1.dp, CardBorderColor, RoundedCornerShape(24.dp))
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
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) PillSelectedBg else Color.Transparent)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) PillSelectedBorder else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onSelectMode(mode) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        fontFamily = JetBrainsMono,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) CyanColor else TextMutedColor
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
        delta > 0 -> Color(0xFF0D3836)
        delta < 0 -> Color(0xFF38151D)
        else -> Color(0xFF1B2838)
    }

    val deltaTextColor = when {
        delta > 0 -> GreenColor
        delta < 0 -> RedColor
        else -> TextMutedColor
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: MODE RATING + Delta Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedMode.label.uppercase()} RATING",
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = TextMutedColor
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
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = deltaTextColor
                        )
                    }

                    Text(
                        text = "this period",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = TextMutedColor
                    )
                }
            }

            // Big Rating + ELO Row
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${stats.rating}${if (stats.provisional) "?" else ""}",
                    fontFamily = JetBrainsMono,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhiteColor,
                    lineHeight = 46.sp
                )

                Text(
                    text = "ELO",
                    fontFamily = JetBrainsMono,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMutedColor,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                if (stats.provisional) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2E2612))
                            .border(1.dp, Color(0xFF6B551C), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PROVISIONAL",
                            fontFamily = JetBrainsMono,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                    }
                }
            }

            // Range Filter Chips (7D | 30D | 90D | 1Y | ALL)
            RangeFilterChips(
                selectedRange = selectedRange,
                onSelectRange = onSelectRange
            )

            // Time-wise Rating Graph
            RatingGraphView(
                ratingHistory = stats.ratingHistory,
                startRating = stats.startRating,
                currentRating = stats.rating,
                modifier = Modifier.fillMaxWidth()
            )

            // Card Footer: Started at XXXX | Peak XXXX
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Started at ${stats.startRating}",
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    color = TextMutedColor
                )

                val peakText = stats.bestRating?.toString() ?: if (stats.gamesPlayed > 0) stats.rating.toString() else "—"
                Text(
                    text = "Peak $peakText",
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextWhiteColor
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
                    .background(if (isSelected) Color(0xFF133246) else Color(0xFF101924))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) CyanColor.copy(alpha = 0.8f) else Color(0xFF1E2C3D),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelectRange(range) }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = range.label,
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) CyanColor else TextMutedColor
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
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Results | XX rated games
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
                    color = TextWhiteColor
                )

                Text(
                    text = "$totalGames rated games",
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    color = TextMutedColor
                )
            }

            // 3-Column Metrics: Wins, Draws, Losses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Wins
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${stats.wins}",
                        fontFamily = JetBrainsMono,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanColor
                    )
                    Text(
                        text = "Wins · $winPercent%",
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        color = TextMutedColor
                    )
                }

                // Draws
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${stats.draws}",
                        fontFamily = JetBrainsMono,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "Draws · $drawPercent%",
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        color = TextMutedColor
                    )
                }

                // Losses
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${stats.losses}",
                        fontFamily = JetBrainsMono,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedColor
                    )
                    Text(
                        text = "Losses · $lossPercent%",
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        color = TextMutedColor
                    )
                }
            }

            // Stacked Segmented Progress Bar
            StackedProgressBar(
                wins = stats.wins,
                draws = stats.draws,
                losses = stats.losses,
                totalGames = totalGames
            )

            // Subtitle
            Text(
                text = if (totalGames > 0) {
                    "${stats.winPercent}% win rate across your rated $modeName games."
                } else {
                    "No rated $modeName games played yet."
                },
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                color = TextMutedColor,
                lineHeight = 18.sp
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
                .background(Color(0xFF1E293B))
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
                    .background(CyanColor)
            )
        }
        if (draws > 0) {
            Box(
                modifier = Modifier
                    .weight((draws.toFloat() / totalGames).coerceAtLeast(0.01f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SlateColor)
            )
        }
        if (losses > 0) {
            Box(
                modifier = Modifier
                    .weight((losses.toFloat() / totalGames).coerceAtLeast(0.01f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(RedColor)
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
            .background(CardBg)
            .border(1.dp, CardBorderColor, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = value,
                fontFamily = JetBrainsMono,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhiteColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                color = TextMutedColor
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
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Performance by Color",
                fontFamily = JetBrainsMono,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhiteColor
            )

            // As White
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
                                .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                        )
                        Text(
                            text = "As White",
                            fontFamily = JetBrainsMono,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextWhiteColor
                        )
                    }

                    Text(
                        text = "$whiteWins W • $whiteDraws D • $whiteLosses L ($whitePct%)",
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        color = TextMutedColor
                    )
                }

                StackedProgressBar(whiteWins, whiteDraws, whiteLosses, whiteTotal)
            }

            // As Black
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
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF475569), CircleShape)
                        )
                        Text(
                            text = "As Black",
                            fontFamily = JetBrainsMono,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextWhiteColor
                        )
                    }

                    Text(
                        text = "$blackWins W • $blackDraws D • $blackLosses L ($blackPct%)",
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        color = TextMutedColor
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
    onGameClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Recent Rated Games",
            fontFamily = JetBrainsMono,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhiteColor
        )

        games.forEach { game ->
            RecentGameItem(
                game = game,
                onClick = { onGameClick(game.gameId) }
            )
        }
    }
}

@Composable
private fun RecentGameItem(
    game: RatingRecentGame,
    onClick: () -> Unit
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
        isWin -> GreenColor
        isLoss -> RedColor
        else -> TextMutedColor
    }

    val outcomeBg = when {
        isWin -> Color(0xFF0D3836)
        isLoss -> Color(0xFF38151D)
        else -> Color(0xFF1E293B)
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
            .background(CardBg)
            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
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
                // Color circle indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isWhite) Color.White else Color(0xFF334155))
                        .border(1.dp, Color(0xFF64748B), CircleShape)
                )

                Column {
                    Text(
                        text = "vs ${game.opponentUsername}",
                        fontFamily = JetBrainsMono,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhiteColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                            fontSize = 11.sp,
                            color = TextMutedColor,
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
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = outcomeColor
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMutedColor,
                    modifier = Modifier.size(18.dp)
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
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = CyanColor,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "Player Statistics",
                fontFamily = JetBrainsMono,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhiteColor
            )
            Text(
                text = "Detailed rating progression charts, win rates, and game histories are available for registered accounts.",
                fontFamily = JetBrainsMono,
                fontSize = 13.sp,
                color = TextMutedColor,
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
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Unable to Load Stats",
                fontFamily = JetBrainsMono,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = RedColor
            )
            Text(
                text = errorMessage,
                fontFamily = JetBrainsMono,
                fontSize = 13.sp,
                color = TextMutedColor,
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
