package com.abhinav.caisarra.presentation.screens

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.presentation.common.singleClickable
import com.abhinav.caisarra.presentation.puzzles.PuzzlesHubViewModel

@Composable
fun PuzzlesScreen(
    viewModel: PuzzlesHubViewModel,
    onPlayDailyPuzzle: () -> Unit,
    onPlayPuzzleRush: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHubData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080D13))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PuzzlesHeader()

        Spacer(modifier = Modifier.height(16.dp))

        DailyPuzzleCard(
            streak = state.streak,
            rating = state.puzzleRating,
            isLoading = state.isLoading,
            onPlayClick = onPlayDailyPuzzle
        )

        Spacer(modifier = Modifier.height(16.dp))

        ModesSection(
            rushHighScore = state.rushHighScore,
            onPlayPuzzleRush = onPlayPuzzleRush
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PuzzlesHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF0D141E),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF1B2A3A),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
            )
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_puzzle_piece),
                contentDescription = null,
                tint = Color(0xFF23CDE8),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "PUZZLES",
                color = Color.White,
                fontFamily = JetBrainsMono,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun DailyPuzzleCard(
    streak: Int,
    rating: Int,
    isLoading: Boolean,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C131D)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B2A3A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.daily_puzzle_graphic),
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

            val ratingText = if (rating > 0) "#$rating Rated" else "Unrated"
            Text(
                text = "Streak: $streak Days • $ratingText",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFF23CDE8),
                    modifier = Modifier.size(32.dp)
                )
            } else {
                GeneralButton(
                    text = "Play",
                    onClick = onPlayClick
                )
            }
        }
    }
}

@Composable
private fun ModesSection(
    rushHighScore: Int,
    onPlayPuzzleRush: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C131D)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B2A3A))
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
                onClick = { onPlayPuzzleRush("3min") }
            )

            ModeItem(
                title = "Puzzle Survival",
                subtitle = "3 Lives • No Time Limit",
                iconRes = R.drawable.ic_puzzle_shield,
                onClick = { onPlayPuzzleRush("survival") }
            )
        }
    }
}

@Composable
private fun ModeItem(
    title: String,
    subtitle: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF131D2A), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF1F2E40), RoundedCornerShape(16.dp))
            .singleClickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = Color(0xFF23CDE8),
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }
    }
}
