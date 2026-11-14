package com.abhinav.caisarra.features.gameplay.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.abhinav.caisarra.presentation.game.components.rememberAutoQueenEnabled
import com.abhinav.caisarra.presentation.game.components.setAutoQueenEnabled
import com.caisaara.ui.theme.StatsTextMuted

@Composable
fun AnimatedVisibilityScope.GameSettingsOptions(
    isBoardFlipped: Boolean,
    onFlip: () -> Unit,
    autoFlip: Boolean? = null,
    onAutoFlipChange: (Boolean) -> Unit = {},
    spectatorCount: Int = 0,
    onSpectate: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val autoQueen by rememberAutoQueenEnabled()
    val count = 2 + (if (autoFlip != null) 1 else 0) + (if (onSpectate != null) 1 else 0)
    SettingsOptionRow(
        icon = Icons.Outlined.Bolt,
        title = "Auto Promote",
        subtitle = if (autoQueen) "Pawns become queens" else "Choose promotion piece",
        onClick = { setAutoQueenEnabled(context, !autoQueen) },
        modifier = Modifier.staggeredEntry(this, 0, count)
    ) {
        SettingsSwitch(checked = autoQueen) { setAutoQueenEnabled(context, it) }
    }
    SettingsOptionRow(
        icon = Icons.Outlined.SwapVert,
        title = "Flip Board",
        subtitle = if (isBoardFlipped) "Viewing from the other side" else "View from the other side",
        onClick = onFlip,
        modifier = Modifier.staggeredEntry(this, 1, count)
    ) {
        SettingsSwitch(checked = isBoardFlipped) { onFlip() }
    }
    autoFlip?.let { enabled ->
        SettingsOptionRow(
            icon = Icons.Outlined.Autorenew,
            title = "Auto Flip",
            subtitle = "Turn the board after every move",
            onClick = { onAutoFlipChange(!enabled) },
            modifier = Modifier.staggeredEntry(this, 2, count)
        ) {
            SettingsSwitch(checked = enabled, onCheckedChange = onAutoFlipChange)
        }
    }
    onSpectate?.let { spectate ->
        SettingsOptionRow(
            icon = Icons.Outlined.Visibility,
            title = "Spectate",
            subtitle = if (spectatorCount > 0) "$spectatorCount watching · Share watch link" else "Share watch link",
            onClick = spectate,
            modifier = Modifier.staggeredEntry(this, count - 1, count)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = StatsTextMuted)
        }
    }
}
