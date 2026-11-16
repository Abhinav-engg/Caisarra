package com.abhinav.caisarra.features.mascot.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.features.mascot.domain.PuzzleMascotLines
import com.abhinav.caisarra.presentation.puzzles.CustomTacticsUiState

private const val UNLIMITED_ATTEMPTS = 3

@Composable
fun CustomTacticsMascot(
    state: CustomTacticsUiState.Playing,
    modifier: Modifier = Modifier,
    mascotSize: Dp = 56.dp
) {
    val transient = rememberTransientMascotReaction()
    val feedback = state.moveFeedback
    val seed = state.puzzle.hashCode()

    LaunchedEffect(feedback?.id) {
        if (feedback != null) {
            transient.show(
                PuzzleMascotLines.moveResult(feedback.isCorrect, UNLIMITED_ATTEMPTS, feedback.id)
            )
        }
    }
    LaunchedEffect(state.hintCount) {
        if (state.hintCount > 0) {
            transient.show(PuzzleMascotLines.hint(seed + state.hintCount))
        }
    }

    val reaction = transient.current
        ?: PuzzleMascotLines.idle(UNLIMITED_ATTEMPTS, state.message, seed)

    MascotSays(reaction = reaction, modifier = modifier, mascotSize = mascotSize)
}