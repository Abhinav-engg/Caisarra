@file:OptIn(ExperimentalMaterial3Api::class)

package com.abhinav.caisarra.presentation.tournament

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.presentation.game.components.setup.SetupHeader

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TournamentColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        SetupHeader("Create Tournament", onBack)
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
                singleLine = true,
                colors = tournamentFieldColors()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Description (optional)") },
                minLines = 2,
                maxLines = 4,
                colors = tournamentFieldColors()
            )

            SectionLabel("FORMAT")
            OptionChips(
                options = listOf(
                    "swiss" to "Swiss",
                    "round_robin" to "Round Robin",
                    "knockout" to "Knockout"
                ),
                selected = state.format,
                onSelect = viewModel::onFormatChange
            )

            SectionLabel("TIME CONTROL")
            OptionChips(
                options = listOf("1+0", "3+0", "3+2", "5+0", "10+0", "15+10").map { it to it },
                selected = state.timeControl,
                onSelect = viewModel::onTimeControlChange
            )

            SectionLabel("VISIBILITY")
            OptionChips(
                options = listOf("public" to "Public", "private" to "Private (invite code)"),
                selected = state.visibility,
                onSelect = viewModel::onVisibilityChange
            )

            SectionLabel("PLAYERS")
            Row {
                OutlinedTextField(
                    value = state.minPlayers,
                    onValueChange = viewModel::onMinPlayersChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Minimum") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = tournamentFieldColors()
                )
                Spacer(Modifier.width(12.dp))
                OutlinedTextField(
                    value = state.maxPlayers,
                    onValueChange = viewModel::onMaxPlayersChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Maximum") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = tournamentFieldColors()
                )
            }

            state.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = TournamentColors.Danger, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(12.dp))
        GeneralButton(
            if (state.submitting) "CREATING..." else "CREATE TOURNAMENT",
            onClick = viewModel::submit
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Spacer(Modifier.height(20.dp))
    Text(
        text,
        color = TournamentColors.Muted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun OptionChips(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = TournamentColors.Card,
                    labelColor = TournamentColors.Text,
                    selectedContainerColor = TournamentColors.Accent,
                    selectedLabelColor = TournamentColors.Background
                )
            )
        }
    }
}