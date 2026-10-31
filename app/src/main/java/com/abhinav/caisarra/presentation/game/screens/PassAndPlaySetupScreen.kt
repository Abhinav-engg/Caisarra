package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.presentation.game.components.setup.CustomClockCard
import com.abhinav.caisarra.presentation.game.components.setup.SetupColors
import com.abhinav.caisarra.presentation.game.components.setup.SetupHeader
import com.abhinav.caisarra.presentation.game.components.setup.SetupPage
import com.abhinav.caisarra.presentation.game.components.setup.TimeControlOption
import com.abhinav.caisarra.presentation.game.components.setup.TimeControlSection
import com.abhinav.caisarra.presentation.game.viewmodel.PassAndPlaySetupEvent
import com.abhinav.caisarra.presentation.game.viewmodel.PassAndPlaySetupUiState
@Composable
fun PassAndPlaySetupScreen(
    state: PassAndPlaySetupUiState,
    onEvent: (PassAndPlaySetupEvent) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    val presets = listOf(1, 5, 10, 15, 30, 60)

    SetupPage {
        SetupHeader("Pass and Play", onBack)
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Column {
                Text(
                    text = "Use a clock",
                    color = SetupColors.Text,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Give each player a time limit",
                    color = SetupColors.Secondary,
                    fontSize = 13.sp
                )
            }
            Switch(
                checked = state.clockEnabled,
                onCheckedChange = {
                    onEvent(PassAndPlaySetupEvent.ClockChanged(it))
                }
            )
        }

        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Column {
                Text(
                    text = "Auto-flip board",
                    color = SetupColors.Text,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Rotate the board after each move",
                    color = SetupColors.Secondary,
                    fontSize = 13.sp
                )
            }
            Switch(
                checked = state.autoFlip,
                onCheckedChange = {
                    onEvent(PassAndPlaySetupEvent.AutoFlipChanged(it))
                }
            )
        }

        if (state.clockEnabled) {
            Spacer(Modifier.height(28.dp))
            TimeControlSection(
                title = "TIME PER PLAYER",
                icon = Icons.Default.AccessTime,
                color = SetupColors.Accent,
                options = presets.map { minutes ->
                    TimeControlOption(
                        label = "$minutes min",
                        minutes = minutes.toFloat(),
                        incrementSeconds = 0f,
                        mode = com.abhinav.caisarra.presentation.game.viewmodel.GameMode.RAPID
                    )
                },
                selected = "${state.minutes.toInt()} min"
            ) {
                onEvent(PassAndPlaySetupEvent.PresetSelected(it.minutes.toInt()))
            }
            Spacer(Modifier.height(24.dp))
            CustomClockCard(
                minutes = state.minutes.coerceAtMost(60f),
                bonus = 0f,
                onMinutesChange = {
                    onEvent(PassAndPlaySetupEvent.TimeChanged(it))
                },
                onBonusChange = {},
                showBonus = false
            )
        }

        Spacer(Modifier.height(30.dp))
        GeneralButton(
            text = "START GAME",
            onClick = onStart,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
