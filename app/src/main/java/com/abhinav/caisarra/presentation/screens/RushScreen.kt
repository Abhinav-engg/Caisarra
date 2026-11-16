package com.abhinav.caisarra.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.components.FullScreenError
import com.abhinav.caisarra.features.mascot.domain.RushMascotLines
import com.abhinav.caisarra.features.mascot.presentation.MascotSpotlight
import com.abhinav.caisarra.features.puzzles.presentation.RushPlayingContent
import com.abhinav.caisarra.presentation.puzzles.RushUiState
import com.abhinav.caisarra.presentation.puzzles.RushViewModel
import com.abhinav.caisarra.presentation.puzzles.components.RushResultSheet
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCyan

@Composable
fun RushScreen(
    viewModel: RushViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    BackHandler {
        if (state is RushUiState.Playing) {
            viewModel.quitRush()
        }
        onBack()
    }

    when (val uiState = state) {
        is RushUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StatsBg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = StatsCyan)
            }
        }

        is RushUiState.Error -> FullScreenError(uiState.message, viewModel::startRush, onBack)

        is RushUiState.Playing -> {
            RushPlayingContent(
                state = uiState,
                viewModel = viewModel,
                onQuit = onBack
            )
        }

        is RushUiState.GameOver -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StatsBg)
                    .systemBarsPadding(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MascotSpotlight(
                        reaction = RushMascotLines.gameOver(uiState.summary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 16.dp)
                    )
                    RushResultSheet(
                        summary = uiState.summary,
                        onPlayAgain = { viewModel.startRush() },
                        onClose = onBack
                    )
                }
            }
        }
    }
}
