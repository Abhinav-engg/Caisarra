package com.abhinav.caisarra.presentation.screens

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

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

        Spacer(modifier = Modifier.height(20.dp))

        ModeSelectionSection(
            onPlayPuzzleRush = onPlayPuzzleRush,
            onCustomTacticsClick = {
                Toast.makeText(context, "Custom Tactics is coming soon", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PuzzlesHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B1017))
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_puzzle_piece),
                contentDescription = null,
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1520)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF131D2A))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_puzzle_piece),
                    contentDescription = null,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Daily Puzzle",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            val ratingText = if (rating > 0) "#$rating Rated" else "Unrated"
            Text(
                text = "Streak: $streak Days • $ratingText",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

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
private fun ModeSelectionSection(
    onPlayPuzzleRush: (String) -> Unit,
    onCustomTacticsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ModeCard(
            title = "Puzzle Rush",
            subtitle = "3 min • High Score: 24",
            iconContent = {
                Image(
                    painter = painterResource(id = R.drawable.ic_puzzle_piece),
                    contentDescription = "Puzzle Rush",
                    modifier = Modifier.size(24.dp)
                )
            },
            onClick = { onPlayPuzzleRush("3min") }
        )
        ModeCard(
            title = "Puzzle Survival",
            subtitle = "3 Lives • No Time Limit",
            iconContent = {
                Image(
                    painter = painterResource(id = R.drawable.ic_puzzle_shield),
                    contentDescription = "Puzzle Survival",
                    modifier = Modifier.size(24.dp)
                )
            },
            onClick = { onPlayPuzzleRush("survival") }
        )
        ModeCard(
            title = "Custom Tactics",
            subtitle = "Select themes & difficulty",
            iconContent = {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = "Custom Tactics",
                    tint = Color(0xFF23CDE8),
                    modifier = Modifier.size(22.dp)
                )
            },
            onClick = onCustomTacticsClick
        )
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1520), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .singleClickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                iconContent()
            }

            Spacer(modifier = Modifier.width(14.dp))

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
