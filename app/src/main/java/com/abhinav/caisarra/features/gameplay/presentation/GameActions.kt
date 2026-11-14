package com.abhinav.caisarra.features.gameplay.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.SpeakerNotesOff
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.presentation.game.components.GameDecision
import com.abhinav.caisarra.presentation.game.components.GameDecisionDialog

@Composable
fun GameActions(
    gameOngoing: Boolean,
    isBoardFlipped: Boolean,
    onResign: () -> Unit,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
    opponentName: String = "Your opponent",
    onOfferDraw: (() -> Unit)? = null,
    onChat: (() -> Unit)? = null,
    chatUnread: Int = 0,
    chatMuted: Boolean = false,
    autoFlip: Boolean? = null,
    onAutoFlipChange: (Boolean) -> Unit = {},
    spectatorCount: Int = 0,
    onSpectate: (() -> Unit)? = null
) {
    var showSettings by remember { mutableStateOf(false) }
    var pendingDecision by remember { mutableStateOf<GameDecision?>(null) }
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GameActionButton(Icons.Outlined.Flag, "Resign", { pendingDecision = GameDecision.Resign }, Modifier.weight(1f), gameOngoing)
            if (onOfferDraw != null) {
                GameActionButton(Icons.Outlined.Handshake, "Offer draw", { pendingDecision = GameDecision.OfferDraw }, Modifier.weight(1f), gameOngoing)
            }
            if (onChat != null) {
                GameActionButton(
                    icon = if (chatMuted) Icons.Outlined.SpeakerNotesOff else Icons.Outlined.ChatBubbleOutline,
                    label = "Chat",
                    onClick = onChat,
                    modifier = Modifier.weight(1f),
                    badgeCount = chatUnread
                )
            }
            GameActionButton(
                icon = Icons.Outlined.Tune,
                label = "Settings",
                onClick = { showSettings = !showSettings },
                modifier = Modifier.weight(1f),
                selected = showSettings,
                iconRotation = if (showSettings) 180f else 0f
            )
        }
        SettingsPopup(expanded = showSettings, width = maxWidth, onDismiss = { showSettings = false }) {
            GameSettingsOptions(
                isBoardFlipped = isBoardFlipped,
                onFlip = onFlip,
                autoFlip = autoFlip,
                onAutoFlipChange = onAutoFlipChange,
                spectatorCount = spectatorCount,
                onSpectate = onSpectate?.let { spectate ->
                    {
                        showSettings = false
                        spectate()
                    }
                }
            )
        }
    }
    pendingDecision?.let { decision ->
        GameDecisionDialog(
            decision = decision,
            opponentName = opponentName,
            onConfirm = {
                pendingDecision = null
                if (decision == GameDecision.Resign) onResign() else onOfferDraw?.invoke()
            },
            onDismiss = { pendingDecision = null }
        )
    }
}
