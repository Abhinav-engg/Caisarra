package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.game.components.setup.CustomClockCard
import com.abhinav.caisarra.presentation.game.components.setup.SetupColors
import com.abhinav.caisarra.presentation.game.components.setup.SetupHeader
import com.abhinav.caisarra.presentation.game.components.setup.SetupPage
import com.abhinav.caisarra.presentation.game.components.setup.TimeControlOption
import com.abhinav.caisarra.presentation.game.components.setup.TimeControlSection
import com.abhinav.caisarra.presentation.game.viewmodel.GameSetupEvent
import com.abhinav.caisarra.presentation.game.viewmodel.GameSetupUiState

@Composable
fun TimeControlsScreen(state: GameSetupUiState, onEvent: (GameSetupEvent) -> Unit, onBack: () -> Unit) {
    SetupPage {
        SetupHeader("Time Controls", onBack)
        Spacer(Modifier.height(24.dp))
        TimeControlSection("BULLET", Icons.Default.EmojiEvents, SetupColors.Gold, listOf(TimeControlOption("1 min", 1f), TimeControlOption("1 + 1", 1f), TimeControlOption("2 + 1", 2f)), state.selectedControl) { onEvent(GameSetupEvent.ControlSelected(it.label, it.minutes)) }
        Spacer(Modifier.height(24.dp))
        TimeControlSection("BLITZ", Icons.Default.FlashOn, SetupColors.Gold, listOf(TimeControlOption("3 min", 3f), TimeControlOption("3 + 2", 3f), TimeControlOption("5 min", 5f), TimeControlOption("5 + 5", 5f), TimeControlOption("5 + 2", 5f)), state.selectedControl) { onEvent(GameSetupEvent.ControlSelected(it.label, it.minutes)) }
        Spacer(Modifier.height(24.dp))
        TimeControlSection("RAPID", Icons.Default.Schedule, SetupColors.Accent, listOf(TimeControlOption("10 min", 10f), TimeControlOption("15 + 10", 15f), TimeControlOption("30 min", 30f), TimeControlOption("10 + 5", 10f), TimeControlOption("20 min", 20f), TimeControlOption("60 min", 60f)), state.selectedControl) { onEvent(GameSetupEvent.ControlSelected(it.label, it.minutes)) }
        Spacer(Modifier.height(24.dp))
        TimeControlSection("DAILY", Icons.Default.WbSunny, SetupColors.Orange, listOf(TimeControlOption("1 day", 1f), TimeControlOption("2 days", 2f), TimeControlOption("3 days", 3f), TimeControlOption("5 days", 5f), TimeControlOption("7 days", 7f), TimeControlOption("14 days", 14f)), state.selectedControl) { onEvent(GameSetupEvent.ControlSelected(it.label, it.minutes)) }
        Spacer(Modifier.height(30.dp))
        CustomClockCard(state.minutes, state.bonusSeconds, { onEvent(GameSetupEvent.TimeChanged(it)) }, { onEvent(GameSetupEvent.BonusChanged(it)) })
        Spacer(Modifier.height(28.dp))
        GeneralButton("Confirm Time Control  ✓", {})
    }
}
