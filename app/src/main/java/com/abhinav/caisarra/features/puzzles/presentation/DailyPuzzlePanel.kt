package com.abhinav.caisarra.features.puzzles.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.puzzles.DailyPuzzleUiState
import com.abhinav.caisarra.presentation.puzzles.DailyPuzzleViewModel
import com.abhinav.caisarra.presentation.puzzles.components.AttemptsIndicator

@Composable
fun DailyPuzzlePanel(state: DailyPuzzleUiState.Playing, viewModel: DailyPuzzleViewModel, modifier: Modifier = Modifier) {
    val busy = state.isAwaitingServer || state.isPlayingSolution
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PuzzleInfoRow(
            title = "Puzzle",
            subtitle = when {
                state.isAwaitingServer -> "Checking move…"
                else -> "${if (state.playerColor == PieceColor.WHITE) "White" else "Black"} to move · ${state.puzzle.rating}"
            }
        ) {
            AttemptsIndicator(attemptsLeft = state.attemptsLeft)
        }
        if (state.puzzle.themes.isNotEmpty()) {
            PuzzleThemeChips(state.puzzle.themes)
        }
        if (state.solutionRevealed && state.solutionMoves.isNotEmpty()) {
            PuzzleSolutionLine(state.solutionMoves, state.solutionPly)
        }
        PuzzleActions(
            onHint = viewModel::requestHint,
            onRestart = viewModel::resetPuzzleToStart,
            onSolution = if (state.solutionRevealed) viewModel::replaySolution else viewModel::requestSolution,
            hintEnabled = state.hintAvailable && !busy,
            hintActive = state.highlightedSquare != null,
            restartEnabled = !busy,
            solutionEnabled = !busy && (state.solutionRevealed || state.canViewSolution),
            solutionRevealed = state.solutionRevealed
        )
    }
}
