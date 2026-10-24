
package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.components.MainTabTopBar
import com.abhinav.caisarra.presentation.common.singleClickable
import com.abhinav.caisarra.presentation.puzzles.PuzzlesHubViewModel
import com.caisaara.ui.theme.PuzzleCardBorder
import com.caisaara.ui.theme.PuzzleItemBg
import com.caisaara.ui.theme.PuzzleItemBorder
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsSkeletonChartBg
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeSubhead
import com.caisaara.ui.theme.ThemeSlateAccent

@Composable
fun PuzzlesScreen(
    viewModel: PuzzlesHubViewModel,
    onPlayDailyPuzzle: () -> Unit,
    onPlayPuzzleRush: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current


    var isLaunchingPuzzle by remember { mutableStateOf(false) }
    
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isLaunchingPuzzle = false
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadHubData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PuzzlesHeader()

        Spacer(modifier = Modifier.height(16.dp))

        DailyPuzzleCard(
            streak = state.streak,
            rating = state.puzzleRating,
            enabled = !isLaunchingPuzzle,
            onPlayClick = {
                if (!isLaunchingPuzzle) {
                    isLaunchingPuzzle = true
                    onPlayDailyPuzzle()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ModesSection(
            rushHighScore = state.rushHighScore,
            enabled = !isLaunchingPuzzle,
            onPlayPuzzleRush = { mode ->
                if (!isLaunchingPuzzle) {
                    isLaunchingPuzzle = true
                    onPlayPuzzleRush(mode)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PuzzlesHeader() {
    MainTabTopBar(
        title = "PUZZLES",
        iconPainter = painterResource(id = R.drawable.ic_puzzle_piece)
    )
}

@Composable
private fun DailyPuzzleCard(
    streak: Int,
    rating: Int,
    enabled: Boolean,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = StatsSkeletonChartBg
        ),
        border = BorderStroke(1.dp, PuzzleCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.daily_puzzle_graphic
                ),
                contentDescription = "Daily Puzzle",
                modifier = Modifier
                    .width(187.dp)
                    .height(185.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Daily Puzzle",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            val ratingText =
                if (rating > 0) "#$rating Rated" else "Unrated"

            Text(
                text = "Streak: $streak Days • $ratingText",
                color = ThemeSlateAccent,
                fontSize = TextSizeBadge
            )

            Spacer(modifier = Modifier.height(16.dp))

            GeneralButton(
                text = if (enabled) "Play" else "Opening...",
                enabled = enabled,
                onClick = onPlayClick
            )
        }
    }
}

@Composable
private fun ModesSection(
    rushHighScore: Int,
    enabled: Boolean,
    onPlayPuzzleRush: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = StatsSkeletonChartBg
        ),
        border = BorderStroke(1.dp, PuzzleCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModeItem(
                title = "Puzzle Rush",
                subtitle = "3 min • High Score: $rushHighScore",
                iconRes = R.drawable.ic_puzzle_piece,
                enabled = enabled,
                onClick = {
                    onPlayPuzzleRush("3min")
                }
            )

            ModeItem(
                title = "Puzzle Survival",
                subtitle = "3 Lives • No Time Limit",
                iconRes = R.drawable.ic_puzzle_shield,
                enabled = enabled,
                onClick = {
                    onPlayPuzzleRush("survival")
                }
            )
        }
    }
}

@Composable
private fun ModeItem(
    title: String,
    subtitle: String,
    iconRes: Int,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val itemAlpha = if (enabled) 1f else 0.5f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                PuzzleItemBg.copy(alpha = itemAlpha),
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                PuzzleItemBorder.copy(alpha = itemAlpha),
                RoundedCornerShape(16.dp)
            )
            .singleClickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = StatsCyan.copy(alpha = itemAlpha),
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = itemAlpha),
                    fontSize = TextSizeSubhead,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    color = ThemeSlateAccent.copy(alpha = itemAlpha),
                    fontSize = TextSizeCaption
                )
            }
        }
    }
}
