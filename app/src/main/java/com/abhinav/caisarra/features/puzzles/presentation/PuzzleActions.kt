package com.abhinav.caisarra.features.puzzles.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.features.gameplay.presentation.GameActionButton

@Composable
fun PuzzleActions(
    onHint: () -> Unit,
    onRestart: () -> Unit,
    onSolution: () -> Unit,
    modifier: Modifier = Modifier,
    hintEnabled: Boolean = true,
    hintActive: Boolean = false,
    restartEnabled: Boolean = true,
    solutionEnabled: Boolean = true,
    solutionRevealed: Boolean = false
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        GameActionButton(Icons.Outlined.Lightbulb, "Hint", onHint, Modifier.weight(1f), hintEnabled, hintActive)
        GameActionButton(Icons.Outlined.Refresh, "Restart", onRestart, Modifier.weight(1f), restartEnabled)
        GameActionButton(
            icon = if (solutionRevealed) Icons.Outlined.PlayArrow else Icons.Outlined.Flag,
            label = if (solutionRevealed) "Replay" else "Solution",
            onClick = onSolution,
            modifier = Modifier.weight(1f),
            enabled = solutionEnabled
        )
    }
}
