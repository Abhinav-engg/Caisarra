package com.abhinav.caisarra.features.puzzles.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.features.gameplay.presentation.GameActionButton
import com.abhinav.caisarra.presentation.puzzles.RushUiState

@Composable
fun RushActions(
    state: RushUiState.Playing,
    onHint: () -> Unit,
    onSkip: () -> Unit,
    onEndRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    val busy = state.isAwaitingServer || state.isSkipping
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        GameActionButton(Icons.Outlined.Lightbulb, "Hint", onHint, Modifier.weight(1f), !busy, state.highlightedSquare != null)
        GameActionButton(
            icon = Icons.Outlined.SkipNext,
            label = when {
                state.isSkipping -> "Skipping…"
                state.secondsRemaining != null -> "Skip −45s"
                else -> "Skip"
            },
            onClick = onSkip,
            modifier = Modifier.weight(1f),
            enabled = !busy,
            selected = state.isSkipping
        )
        GameActionButton(Icons.Outlined.Flag, "End run", onEndRun, Modifier.weight(1f))
    }
}
