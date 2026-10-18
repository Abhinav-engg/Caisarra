package com.abhinav.caisarra.presentation.tournament

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.data.remote.dto.TournamentDto
import com.abhinav.caisarra.presentation.game.components.setup.SetupHeader

@Composable
fun TournamentListScreen(
    viewModel: TournamentListViewModel,
    onCreate: () -> Unit,
    onOpenTournament: (id: String, inviteCode: String?) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TournamentColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        SetupHeader("Tournaments", onBack)
        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.code,
                onValueChange = viewModel::onCodeChange,
                modifier = Modifier.weight(1f),
                label = { Text("Invite code") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                colors = tournamentFieldColors()
            )
            Spacer(Modifier.width(12.dp))
            OutlinedButton(
                onClick = {
                    viewModel.lookupCode { tournament, code ->
                        onOpenTournament(tournament.id, code)
                    }
                },
                enabled = !state.lookingUp
            ) {
                Text("JOIN", color = TournamentColors.Accent)
            }
        }

        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = TournamentColors.Danger, fontSize = 13.sp)
        }

        Spacer(Modifier.height(16.dp))
        GeneralButton("CREATE TOURNAMENT", onClick = onCreate)
        Spacer(Modifier.height(24.dp))

        Text(
            "OPEN TOURNAMENTS",
            color = TournamentColors.Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))

        when {
            state.loading && state.tournaments.isEmpty() -> Box(
                Modifier.fillMaxWidth().padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TournamentColors.Accent)
            }

            state.tournaments.isEmpty() -> Text(
                "No open tournaments right now. Create one or join with a code.",
                color = TournamentColors.Muted,
                fontSize = 14.sp
            )

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.tournaments, key = { it.id }) { tournament ->
                    TournamentCard(tournament) { onOpenTournament(tournament.id, null) }
                }
            }
        }
    }
}

@Composable
private fun TournamentCard(tournament: TournamentDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TournamentColors.Card)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            tournament.name,
            color = TournamentColors.Text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "${formatLabel(tournament.format)} · ${tournament.timeControl}",
            color = TournamentColors.Muted,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${tournament.players ?: 0}/${tournament.maxPlayers} players",
                color = TournamentColors.Text,
                fontSize = 13.sp
            )
            Text(
                statusLabel(tournament.status),
                color = TournamentColors.Accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}