package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.game.components.setup.GameModeRow
import com.abhinav.caisarra.presentation.game.components.setup.SetupColors
import com.abhinav.caisarra.presentation.game.components.setup.SetupHeader
import com.abhinav.caisarra.presentation.game.components.setup.SetupPage
import com.abhinav.caisarra.presentation.game.components.setup.TimeControlRow
import com.abhinav.caisarra.presentation.game.viewmodel.GameSetupUiState

@Composable
fun GameSetupScreen(state: GameSetupUiState, onOpenTimeControls: () -> Unit, onPlayWithFriend: () -> Unit, onPlayBots: () -> Unit, onPassAndPlay: () -> Unit, onBack: () -> Unit) {
    SetupPage {
        SetupHeader("New Game", onBack)
        Spacer(Modifier.height(24.dp))
        TimeControlRow("${state.mode.label} · ${state.selectedControl}", onOpenTimeControls)
        Spacer(Modifier.height(20.dp))
        GeneralButton("START MATCH", {})
        Spacer(Modifier.height(28.dp))
        Text("EXPLORE OTHER MODES", color = SetupColors.Secondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(14.dp))
        GameModeRow("Tournaments", Icons.Default.EmojiEvents)
        Spacer(Modifier.height(12.dp))
        GameModeRow("Play a Friend", Icons.Default.Group, onPlayWithFriend)
        Spacer(Modifier.height(12.dp))
        GameModeRow("Play Bots", Icons.Default.Computer, onPlayBots)
        Spacer(Modifier.height(12.dp))
        GameModeRow("Pass and Play", Icons.Default.PhoneAndroid, onPassAndPlay)
    }
}
