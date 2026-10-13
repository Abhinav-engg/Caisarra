package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.game.viewmodel.MatchmakingViewModel

@Composable
fun MatchmakingScreen(
    viewModel: MatchmakingViewModel,
    minutes: Int,
    incrementSeconds: Int,
    onMatched: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.matchedGameId) {
        state.matchedGameId?.let { gameId ->
            viewModel.clearMatchedGame()
            onMatched(gameId)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080D13))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "MOSAIC",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(42.dp))

        if (state.isSearching) {
            CircularProgressIndicator(
                color = Color(0xFF10B981),
                strokeWidth = 4.dp
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = state.message.ifBlank {
                    "Finding opponent..."
                },
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            Text(
                text = "Looking for a player with a similar rating",
                color = Color(0xFFB8C2CC),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(20.dp))
            Text(
                text = "$minutes min + ${incrementSeconds}s",
                color = Color(0xFF10B981),
                fontSize = 16.sp
            )

            Spacer(Modifier.height(36.dp))
            GeneralButton(
                text = "CANCEL SEARCH",
                onClick = viewModel::cancelMatch,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = state.error ?: "Search ended",
                color = if (state.error != null) {
                    Color(0xFFFF7777)
                } else {
                    Color.White
                },
                fontSize = 17.sp
            )
            Spacer(Modifier.height(24.dp))

            GeneralButton(
                text = "TRY AGAIN",
                onClick = {
                    viewModel.startMatch(
                        minutes = minutes,
                        incrementSeconds = incrementSeconds
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onBack) {
                Text("BACK", color = Color.White)
            }
        }
    }

    state.tiltWarning?.let {
        AlertDialog(
            onDismissRequest = viewModel::dismissTiltWarning,
            title = {
                Text("Take a short break?")
            },
            text = {
                Text(
                    "The server detected a possible tilt pattern. "+
                            "You can take a break or continue with a rated game."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.retryAfterTiltWarning(
                            minutes,
                            incrementSeconds
                        )
                    }
                ) {
                    Text("CONTINUE")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::dismissTiltWarning
                ) {
                    Text("CANCEL")
                }
            }
        )
    }
}
