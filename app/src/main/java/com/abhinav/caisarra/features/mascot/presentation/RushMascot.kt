package com.abhinav.caisarra.features.mascot.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.abhinav.caisarra.features.mascot.domain.RushMascotLines
import com.abhinav.caisarra.presentation.puzzles.RushUiState

@Composable
fun RushMascot(
    state: RushUiState.Playing,
    modifier: Modifier = Modifier
) {
    val transient = rememberTransientMascotReaction(holdMillis = 2600L)
    var lastScore by remember(state.sessionId) { mutableIntStateOf(state.score) }
    var lastStrikes by remember(state.sessionId) { mutableIntStateOf(state.strikes) }
    val feedback = state.moveFeedback

    LaunchedEffect(feedback?.id) {
        if (feedback != null && feedback.isCorrect) {
            transient.show(RushMascotLines.step(feedback.id))
        }
    }
    LaunchedEffect(state.highlightedSquare) {
        if (state.highlightedSquare != null) {
            transient.show(RushMascotLines.hint(state.score + state.strikes))
        }
    }
    LaunchedEffect(state.strikes) {
        if (state.strikes > lastStrikes) {
            transient.show(RushMascotLines.strike(state.mode, state.strikes, state.strikesLeft))
        }
        lastStrikes = state.strikes
    }
    LaunchedEffect(state.score) {
        if (state.score > lastScore) {
            transient.show(RushMascotLines.solved(state.score))
        }
        lastScore = state.score
    }

    val reaction = transient.current
        ?: RushMascotLines.idle(state.mode, state.strikesLeft, state.secondsRemaining, state.sessionId.hashCode())

    MascotSays(reaction = reaction, modifier = modifier)
}
