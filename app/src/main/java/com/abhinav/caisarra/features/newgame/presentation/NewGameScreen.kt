package com.abhinav.caisarra.features.newgame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.components.SettingsSectionHeader
import com.abhinav.caisarra.core.ui.theme.StatsScreenPadding
import com.abhinav.caisarra.presentation.game.viewmodel.GameSetupUiState
import com.caisaara.ui.theme.StatsBg

@Composable
fun NewGameScreen(
    state: GameSetupUiState,
    onOpenTimeControls: () -> Unit,
    onStartMatch: () -> Unit,
    onTournaments: () -> Unit,
    onPlayWithFriend: () -> Unit,
    onPlayBots: () -> Unit,
    onPassAndPlay: () -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(StatsBg).statusBarsPadding()) {
        CaisaaraTopBar(title = "NEW GAME", onBack = onBack, titleFontSize = 18.sp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(StatsScreenPadding)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NewGameOptionCard(Icons.Outlined.Schedule, "${state.mode.label} · ${state.selectedControl}", "Rated · Random side", onOpenTimeControls)
            GeneralButton("START MATCH", onClick = onStartMatch)
            Spacer(Modifier.height(8.dp))
            SettingsSectionHeader("EXPLORE OTHER MODES")
            NewGameOptionCard(Icons.Outlined.People, "Play with a Friend", "Invite a player or share a link", onPlayWithFriend)
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NewGameTileCard(Icons.Outlined.SmartToy, "Play Bots", "Practice your way", onPlayBots, Modifier.weight(1f).fillMaxHeight())
                NewGameTileCard(Icons.Outlined.PhoneAndroid, "Pass & play", "One device, two minds", onPassAndPlay, Modifier.weight(1f).fillMaxHeight())
            }
            NewGameOptionCard(Icons.Outlined.EmojiEvents, "Tournaments", "Compete in rated events", onTournaments)
        }
    }
}
