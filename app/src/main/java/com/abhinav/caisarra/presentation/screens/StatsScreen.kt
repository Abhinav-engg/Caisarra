package com.abhinav.caisarra.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.core.ui.theme.StatsScreenPadding
import com.abhinav.caisarra.core.ui.theme.StatsSectionSpacing
import com.abhinav.caisarra.presentation.screens.components.StatsSkeletonScreen
import com.abhinav.caisarra.presentation.screens.components.stats.KeyMetricsGrid
import com.abhinav.caisarra.presentation.screens.components.stats.RatingProgressionCard
import com.abhinav.caisarra.presentation.screens.components.stats.RecentGamesSection
import com.abhinav.caisarra.presentation.screens.components.stats.ResultsCard
import com.abhinav.caisarra.presentation.screens.components.stats.ModeSelectorBar
import com.abhinav.caisarra.presentation.screens.components.stats.GuestStatsCard
import com.abhinav.caisarra.presentation.screens.components.stats.ErrorStateCard
import com.abhinav.caisarra.presentation.viewmodel.StatsViewModel
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsErrorBadgeBg
import com.caisaara.ui.theme.StatsRed
import com.caisaara.ui.theme.TextSizeCaption
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

    BackHandler {
        onBack()
    }

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

                        if (stats.asWhite != null || stats.asBlack != null) {
                            ColorPerformanceCard(
                                asWhite = stats.asWhite,
                                asBlack = stats.asBlack
                            )
                        }

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

