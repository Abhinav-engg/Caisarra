package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.presentation.game.components.setup.CustomClockCard
import com.abhinav.caisarra.presentation.game.components.setup.SetupColors
import com.abhinav.caisarra.presentation.game.components.setup.SetupHeader
import com.abhinav.caisarra.presentation.game.components.setup.SetupPage
import com.abhinav.caisarra.presentation.game.components.setup.TimeControlOption
import com.abhinav.caisarra.presentation.game.components.setup.TimeControlSection
import com.abhinav.caisarra.presentation.game.viewmodel.GameSetupEvent
import com.abhinav.caisarra.presentation.game.viewmodel.GameSetupUiState
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode

@Composable
fun TimeControlsScreen(
    state: GameSetupUiState,
    onEvent: (GameSetupEvent) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    SetupPage {
        SetupHeader("Time Controls", onBack)
        Spacer(Modifier.height(24.dp))
        TimeControlSection(
            title = "BULLET",
            icon = Icons.Default.EmojiEvents,
            color = SetupColors.Bullet,
            options = listOf(
                TimeControlOption("1 min", 1f, 0f, GameMode.BULLET),
                TimeControlOption("1 + 1", 1f, 1f, GameMode.BULLET),
                TimeControlOption("2 + 1", 2f, 1f, GameMode.BULLET)
            ),
            selected = state.selectedControl,
            selectedMode = state.mode
        ) {
            onEvent(GameSetupEvent.ControlSelected(it.label, it.minutes, it.incrementSeconds, it.mode))
        }
        Spacer(Modifier.height(24.dp))
        TimeControlSection(
            title = "BLITZ",
            icon = Icons.Default.FlashOn,
            color = SetupColors.Blitz,
            options = listOf(
                TimeControlOption("3 min", 3f, 0f, GameMode.BLITZ),
                TimeControlOption("3 + 2", 3f, 2f, GameMode.BLITZ),
                TimeControlOption("5 min", 5f, 0f, GameMode.BLITZ),
                TimeControlOption("5 + 5", 5f, 5f, GameMode.BLITZ),
                TimeControlOption("5 + 2", 5f, 2f, GameMode.BLITZ)
            ),
            selected = state.selectedControl,
            selectedMode = state.mode
        ) {
            onEvent(GameSetupEvent.ControlSelected(it.label, it.minutes, it.incrementSeconds, it.mode))
        }
        Spacer(Modifier.height(24.dp))
        TimeControlSection(
            title = "RAPID",
            icon = Icons.Default.Schedule,
            color = SetupColors.Rapid,
            options = listOf(
                TimeControlOption("10 min", 10f, 0f, GameMode.RAPID),
                TimeControlOption("15 + 10", 15f, 10f, GameMode.RAPID),
                TimeControlOption("30 min", 30f, 0f, GameMode.RAPID),
                TimeControlOption("10 + 5", 10f, 5f, GameMode.RAPID),
                TimeControlOption("20 min", 20f, 0f, GameMode.RAPID),
                TimeControlOption("60 min", 60f, 0f, GameMode.RAPID)
            ),
            selected = state.selectedControl,
            selectedMode = state.mode
        ) {
            onEvent(GameSetupEvent.ControlSelected(it.label, it.minutes, it.incrementSeconds, it.mode))
        }
        Spacer(Modifier.height(24.dp))
        TimeControlSection(
            title = "DAILY",
            icon = Icons.Default.Schedule,
            color = SetupColors.Daily,
            options = listOf(
                TimeControlOption("1 day", 1440f, 0f, GameMode.DAILY),
                TimeControlOption("2 days", 2880f, 0f, GameMode.DAILY),
                TimeControlOption("3 days", 4320f, 0f, GameMode.DAILY),
                TimeControlOption("5 days", 7200f, 0f, GameMode.DAILY),
                TimeControlOption("7 days", 10080f, 0f, GameMode.DAILY),
                TimeControlOption("14 days", 20160f, 0f, GameMode.DAILY)
            ),
            selected = state.selectedControl,
            selectedMode = state.mode
        ) {
            onEvent(
                GameSetupEvent.ControlSelected(
                    it.label, it.minutes, it.incrementSeconds, it.mode
                )
            )
        }

        if (state.mode != GameMode.DAILY) {
            Spacer(Modifier.height(30.dp))
            CustomClockCard(
                minutes = state.minutes,
                bonus = state.bonusSeconds,
                onMinutesChange = {
                    onEvent(GameSetupEvent.TimeChanged(it))
                },
                onBonusChange = {
                    onEvent(GameSetupEvent.BonusChanged(it))
                }
            )
        }
        Spacer(Modifier.height(28.dp))
        GeneralButton("Confirm Time Control  ✓", onConfirm)
    }
}
