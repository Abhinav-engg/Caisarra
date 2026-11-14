package com.abhinav.caisarra.features.mascot.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.abhinav.caisarra.features.mascot.domain.PuzzleMascotLines
import com.abhinav.caisarra.presentation.puzzles.DailyPuzzleUiState

@Composable
fun DailyPuzzleMascot(
    state: DailyPuzzleUiState.Playing,
    modifier: Modifier = Modifier
) {
    val transient = rememberTransientMascotReaction()
    val feedback = state.moveFeedback
    val seed = state.puzzle.hashCode()

    LaunchedEffect(feedback?.id) {
        if (feedback != null) {
            transient.show(PuzzleMascotLines.moveResult(feedback.isCorrect, state.attemptsLeft, feedback.id))
        }
    }
    LaunchedEffect(state.highlightedSquare) {
        if (state.highlightedSquare != null && !state.hintAvailable) {
            transient.show(PuzzleMascotLines.hint(seed))
        }
    }

    val reaction = when {
        state.isPlayingSolution || state.solutionRevealed -> PuzzleMascotLines.watchingSolution(state.message)
        else -> transient.current ?: PuzzleMascotLines.idle(state.attemptsLeft, state.message, seed)
    }

    MascotSays(reaction = reaction, modifier = modifier)
}
