@file:OptIn(ExperimentalMaterial3Api::class)

package com.abhinav.caisarra.presentation.tournament

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreateTournamentScreen(
    viewModel: CreateTournamentViewModel,
    onCreated: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.createdId) {
        state.createdId?.let(onCreated)
    }

    TournamentBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            TournamentHeader(
                title = "CREATE TOURNAMENT",
                onBack = onBack
            )

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                FieldLabel("Tournament name")
                TournamentTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    placeholder = "Enter tournament name"
                )

                Spacer(Modifier.height(16.dp))

                FieldLabel("Description(Optional)")
                TournamentTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = "Write description here...",
                    singleLine = false,
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(Modifier.height(16.dp))

                FieldLabel("Format")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val formats = listOf(
                        "swiss" to "Swiss",
                        "round_robin" to "Round Robin",
                        "knockout" to "Knockout"
                    )
                    formats.forEach { (key, label) ->
                        PillButton(
                            text = label,
                            isSelected = state.format == key,
                            onClick = { viewModel.onFormatChange(key) },
                            modifier = Modifier.weight(
                                when (key) {
                                    "round_robin" -> 1.25f
                                    "knockout" -> 1.05f
                                    else -> 0.95f
                                }
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                FieldLabel("Time Control")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val timeControls = listOf("1+0", "3+0", "3+2", "5+0", "10+0", "15+10")
                    timeControls.forEach { tc ->
                        PillButton(
                            text = tc,
                            isSelected = state.timeControl == tc,
                            onClick = { viewModel.onTimeControlChange(tc) },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                FieldLabel("Visibility")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val visibilities = listOf(
                        "public" to "Public",
                        "private" to "Private (Invite mode)"
                    )
                    visibilities.forEach { (key, label) ->
                        PillButton(
                            text = label,
                            isSelected = state.visibility == key,
                            onClick = { viewModel.onVisibilityChange(key) },
                            modifier = Modifier.weight(if (key == "private") 1.35f else 0.95f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                FieldLabel("Players")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Minimum",
                            color = Color(0xFFC4D0DC),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        TournamentTextField(
                            value = state.minPlayers,
                            onValueChange = viewModel::onMinPlayersChange,
                            placeholder = "Enter value here",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textAlignment = TextAlign.Center
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Maximum",
                            color = Color(0xFFC4D0DC),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(6.dp))
                        TournamentTextField(
                            value = state.maxPlayers,
                            onValueChange = viewModel::onMaxPlayersChange,
                            placeholder = "Enter value here",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textAlignment = TextAlign.Center
                        )
                    }
                }

                state.error?.let {
                    Spacer(Modifier.height(14.dp))
                    Text(it, color = TournamentColors.Danger, fontSize = 13.sp)
                }

                Spacer(Modifier.height(24.dp))
            }

            TournamentActionButton(
                text = "Create Tournament",
                onClick = viewModel::submit,
                loading = state.submitting
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 14.5.sp,
        fontWeight = FontWeight.Medium
    )
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun PillButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color(0xFF14202D) else Color(0xFF0E151F)
            )
            .border(
                width = if (isSelected) 1.2.dp else 1.dp,
                color = if (isSelected) TournamentColors.ActiveBorder else Color(0xFF223040),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF8B98A5),
            fontSize = 13.5.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}
