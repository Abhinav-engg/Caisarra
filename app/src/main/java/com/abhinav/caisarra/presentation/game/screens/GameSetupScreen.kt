package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.game.viewmodel.GameSetupViewModel
import com.abhinav.caisarra.presentation.game.viewmodel.MAX_CUSTOM_MINUTES
import com.abhinav.caisarra.presentation.game.viewmodel.MIN_CUSTOM_MINUTES
import com.abhinav.caisarra.presentation.game.viewmodel.PRESET_TIMES

private object SetupColors {
    val Background = Color(0xFF080D13)
    val Card = Color(0xFF111A24)
    val Border = Color(0xFF1F2B38)
    val Accent = Color(0xFF10B981)
    val TextSecondary = Color(0xFF9CA3AF)
    val Error = Color(0xFFEF4444)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameSetupScreen(
    viewModel: GameSetupViewModel,
    onStartGame: (
        whitePlayer: String,
        blackPlayer: String,
        minutes: Int,
        flipBoard: Boolean,
        undoEnabled: Boolean
    ) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SetupColors.Background)
            .systemBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {

        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "NEW GAME",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pass & play on one device",
                    color = SetupColors.TextSecondary,
                    fontSize = 14.sp
                )
            }

            SetupCard(title = "Players") {
                SetupTextField(
                    value = state.whitePlayerName,
                    onValueChange = viewModel::setWhitePlayerName,
                    label = "White Player"
                )
                SetupTextField(
                    value = state.blackPlayerName,
                    onValueChange = viewModel::setBlackPlayerName,
                    label = "Black Player"
                )
            }

            SetupCard(title = "Game Time") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PRESET_TIMES.forEach { minutes ->
                        TimeChip(
                            label = "${minutes}m",
                            selected = !state.isCustomTime && state.timeMinutes == minutes,
                            onClick = { viewModel.setTime(minutes) }
                        )
                    }
                    TimeChip(
                        label = "Custom",
                        selected = state.isCustomTime,
                        onClick = viewModel::selectCustomTime
                    )
                }

                if (state.isCustomTime) {
                    OutlinedTextField(
                        value = state.customTimeText,
                        onValueChange = viewModel::setCustomTimeText,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Minutes per player") },
                        supportingText = {
                            Text("$MIN_CUSTOM_MINUTES – $MAX_CUSTOM_MINUTES minutes")
                        },
                        suffix = { Text("min") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = setupFieldColors()
                    )
                }
            }

            SetupCard(title = "Options") {
                SettingRow(
                    title = "Flip Board",
                    subtitle = "Rotate the board after every move",
                    checked = state.flipBoard,
                    onCheckedChange = viewModel::setFlipBoard
                )
                SettingRow(
                    title = "Enable Undo",
                    subtitle = "Allow players to take back a move",
                    checked = state.undoEnabled,
                    onCheckedChange = viewModel::setUndoEnabled
                )
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = SetupColors.Error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    if (viewModel.validate()) {
                        onStartGame(
                            state.whitePlayerName,
                            state.blackPlayerName,
                            state.timeMinutes,
                            state.flipBoard,
                            state.undoEnabled
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SetupColors.Accent)
            ) {
                Text(
                    text = "START GAME",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SetupColors.Border)
            ) {
                Text(text = "BACK", color = Color.White)
            }
        }
    }
}

@Composable
private fun SetupCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SetupColors.Card,
        border = BorderStroke(1.dp, SetupColors.Border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title.uppercase(),
                color = SetupColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            content()
        }
    }
}

@Composable
private fun SetupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = setupFieldColors()
    )
}

@Composable
private fun setupFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = SetupColors.Accent,
    unfocusedBorderColor = SetupColors.Border,
    focusedLabelColor = SetupColors.Accent,
    unfocusedLabelColor = SetupColors.TextSecondary,
    cursorColor = SetupColors.Accent,
    focusedSuffixColor = SetupColors.TextSecondary,
    unfocusedSuffixColor = SetupColors.TextSecondary,
    focusedSupportingTextColor = SetupColors.TextSecondary,
    unfocusedSupportingTextColor = SetupColors.TextSecondary
)

@Composable
private fun TimeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .defaultMinSize(minWidth = 64.dp)
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) SetupColors.Accent else Color.Transparent,
        border = if (selected) null else BorderStroke(1.dp, SetupColors.Border)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) Color.Black else Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 16.sp)
            Text(text = subtitle, color = SetupColors.TextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = SetupColors.Accent,
                checkedThumbColor = Color.Black,
                uncheckedTrackColor = SetupColors.Border,
                uncheckedThumbColor = SetupColors.TextSecondary,
                uncheckedBorderColor = SetupColors.Border
            )
        )
    }
}