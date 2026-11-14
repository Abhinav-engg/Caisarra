package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.features.puzzles.presentation.DailyPuzzlePlayingContent
import com.abhinav.caisarra.features.mascot.domain.PuzzleMascotLines
import com.abhinav.caisarra.features.mascot.presentation.MascotSays
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.puzzles.DailyPuzzleUiState
import com.abhinav.caisarra.presentation.puzzles.DailyPuzzleViewModel
import com.abhinav.caisarra.presentation.puzzles.components.PuzzleResultSheet
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsRed
import com.caisaara.ui.theme.TextSizeSubhead

@Composable
fun DailyPuzzleScreen(
    viewModel: DailyPuzzleViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    when (val uiState = state) {
        is DailyPuzzleUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StatsBg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = StatsCyan)
            }
        }

        is DailyPuzzleUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StatsBg)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.message,
                        color = StatsRed,
                        fontSize = TextSizeSubhead
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (uiState.canRetry) {
                        GeneralButton(
                            text = "Retry",
                            onClick = { viewModel.loadDailyPuzzle() }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    GeneralButton(
                        text = "Go Back",
                        onClick = onBack
                    )
                }
            }
        }

        is DailyPuzzleUiState.Playing -> {
            DailyPuzzlePlayingContent(
                state = uiState,
                viewModel = viewModel,
                onBack = onBack
            )
        }

        is DailyPuzzleUiState.Solved -> {
            DailyPuzzleSolvedContent(
                state = uiState,
                onBack = onBack
            )
        }

        is DailyPuzzleUiState.Failed -> {
            DailyPuzzleFailedContent(
                state = uiState,
                viewModel = viewModel,
                onBack = onBack
            )
        }
    }
}

@Composable
private fun DailyPuzzleSolvedContent(
    state: DailyPuzzleUiState.Solved,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .systemBarsPadding()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            val boardSize = minOf(maxWidth, maxHeight)
            Box(
                modifier = Modifier
                    .size(boardSize)
                    .aspectRatio(1f)
                    .align(Alignment.Center)
            ) {
                ChessBoard(
                    board = state.board,
                    selectedSquare = null,
                    legalMoves = emptyList(),
                    lastMoveFrom = null,
                    lastMoveTo = null,
                    isWhiteInCheck = false,
                    isBlackInCheck = false,
                    isFlipped = state.isBoardFlipped,
                    onSquareClick = {}
                )
            }
        }

        MascotSays(
            reaction = PuzzleMascotLines.solved(state.streak, state.ratingChange, state.puzzle.hashCode()),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            mascotSize = 64.dp
        )

        PuzzleResultSheet(
            isSolved = true,
            ratingChange = state.ratingChange,
            newRating = state.userRatingAfter,
            streak = state.streak,
            message = state.message,
            onClose = onBack
        )
    }
}

@Composable
private fun DailyPuzzleFailedContent(
    state: DailyPuzzleUiState.Failed,
    viewModel: DailyPuzzleViewModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .systemBarsPadding()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            val boardSize = minOf(maxWidth, maxHeight)
            Box(
                modifier = Modifier
                    .size(boardSize)
                    .aspectRatio(1f)
                    .align(Alignment.Center)
            ) {
                ChessBoard(
                    board = state.board,
                    selectedSquare = null,
                    legalMoves = emptyList(),
                    lastMoveFrom = null,
                    lastMoveTo = null,
                    isWhiteInCheck = false,
                    isBlackInCheck = false,
                    isFlipped = state.isBoardFlipped,
                    onSquareClick = {}
                )
            }
        }

        MascotSays(
            reaction = PuzzleMascotLines.failed(state.puzzle.hashCode()),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            mascotSize = 64.dp
        )

        PuzzleResultSheet(
            isSolved = false,
            ratingChange = state.ratingChange,
            newRating = state.userRatingAfter,
            streak = state.streak,
            message = state.message,
            onViewSolution = { viewModel.requestSolution() },
            onClose = onBack
        )
    }
}
