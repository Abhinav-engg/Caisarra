package com.abhinav.caisarra.presentation.tournament

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.data.remote.dto.PairingDto
import com.abhinav.caisarra.data.remote.dto.StandingDto
import com.abhinav.caisarra.data.remote.dto.TournamentDto
import com.abhinav.caisarra.presentation.game.components.setup.SetupHeader

@Composable
fun TournamentDetailScreen(
    viewModel: TournamentDetailViewModel,
    onOpenGame: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TournamentColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        val tournament = state.tournament
        SetupHeader(tournament?.name ?: "Tournament", onBack)
        Spacer(Modifier.height(12.dp))

        when {
            tournament == null && state.loading -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TournamentColors.Accent)
            }

            tournament == null -> Column(
                Modifier.fillMaxWidth().padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    state.error ?: "Could not load tournament",
                    color = TournamentColors.Danger,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = viewModel::refresh) {
                    Text("RETRY", color = TournamentColors.Accent)
                }
            }

            else -> {
                TabRow(
                    selectedTabIndex = tab,
                    containerColor = Color.Transparent,
                    contentColor = TournamentColors.Accent
                ) {
                    listOf("Overview", "Standings", "Rounds").forEachIndexed { index, title ->
                        Tab(
                            selected = tab == index,
                            onClick = { tab = index },
                            text = { Text(title) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Box(Modifier.weight(1f)) {
                    when (tab) {
                        0 -> OverviewTab(
                            tournament = tournament,
                            state = state,
                            onJoin = viewModel::join,
                            onLeave = viewModel::leave,
                            onStart = viewModel::start,
                            onOpenGame = onOpenGame
                        )

                        1 -> StandingsTab(state.standings, state.myUserId)
                        else -> RoundsTab(state, onOpenGame)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(
    tournament: TournamentDto,
    state: TournamentDetailUiState,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onStart: () -> Unit,
    onOpenGame: (String) -> Unit
) {
    val context = LocalContext.current
    val isCreator = state.myUserId != null && tournament.createdBy == state.myUserId
    val registration = tournament.status == "registration"
    val active = activeGame(state)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (tournament.status == "completed") {
            val winner = state.standings.firstOrNull { it.playerId == state.winnerId }
                ?: state.standings.firstOrNull()
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TournamentColors.Card)
                    .padding(16.dp)
            ) {
                Text(
                    "Winner: ${winner?.username ?: "decided"}",
                    color = TournamentColors.Accent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        if (tournament.description.isNotBlank()) {
            Text(tournament.description, color = TournamentColors.Text, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
        }

        InfoRow("Format", formatLabel(tournament.format))
        InfoRow("Time control", tournament.timeControl)
        InfoRow("Status", statusLabel(tournament.status))
        InfoRow(
            "Players",
            "${tournament.players ?: state.standings.size} (min ${tournament.minPlayers}, max ${tournament.maxPlayers})"
        )
        InfoRow("Round", "${tournament.currentRound} of ${tournament.totalRounds}")
        InfoRow("Visibility", tournament.visibility.replaceFirstChar { it.uppercase() })

        if (isCreator && tournament.inviteCode.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TournamentColors.Card)
                    .padding(16.dp)
            ) {
                Text("INVITE CODE", color = TournamentColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    tournament.inviteCode,
                    color = TournamentColors.Text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Join my chess tournament \"${tournament.name}\" with invite code ${tournament.inviteCode}"
                        )
                    }
                    context.startActivity(Intent.createChooser(intent, "Share invite code"))
                }) {
                    Text("SHARE CODE", color = TournamentColors.Accent)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (active != null) {
            GeneralButton("PLAY ROUND ${active.first}", onClick = { onOpenGame(active.second) })
            Spacer(Modifier.height(12.dp))
        }

        if (registration) {
            if (!state.hasJoined) {
                GeneralButton("JOIN TOURNAMENT", onClick = onJoin)
            } else {
                OutlinedButton(onClick = onLeave, modifier = Modifier.fillMaxWidth()) {
                    Text("LEAVE TOURNAMENT", color = TournamentColors.Danger)
                }
            }
            if (isCreator) {
                Spacer(Modifier.height(12.dp))
                GeneralButton("START TOURNAMENT", onClick = onStart)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Needs at least ${tournament.minPlayers} players to start.",
                    color = TournamentColors.Muted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TournamentColors.Muted, fontSize = 14.sp)
        Text(value, color = TournamentColors.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StandingsTab(standings: List<StandingDto>, myUserId: Long?) {
    if (standings.isEmpty()) {
        Text(
            "Standings will appear once the tournament starts.",
            color = TournamentColors.Muted,
            fontSize = 14.sp
        )
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                HeaderCell("#", Modifier.width(28.dp))
                HeaderCell("PLAYER", Modifier.weight(1f))
                HeaderCell("PTS", Modifier.width(44.dp))
                HeaderCell("BUCH", Modifier.width(44.dp))
                HeaderCell("W-D-L", Modifier.width(64.dp))
            }
        }
        items(standings, key = { it.playerId }) { row ->
            val mine = row.playerId == myUserId
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(TournamentColors.Card)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val color = if (mine) TournamentColors.Accent else TournamentColors.Text
                Text(row.rank.toString(), color = color, fontSize = 14.sp, modifier = Modifier.width(28.dp))
                Text(
                    row.username,
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(scoreLabel(row.score), color = color, fontSize = 14.sp, modifier = Modifier.width(44.dp))
                Text(scoreLabel(row.buchholz), color = TournamentColors.Muted, fontSize = 13.sp, modifier = Modifier.width(44.dp))
                Text(
                    "${row.wins}-${row.draws}-${row.losses}",
                    color = TournamentColors.Muted,
                    fontSize = 13.sp,
                    modifier = Modifier.width(64.dp)
                )
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier) {
    Text(
        text,
        color = TournamentColors.Muted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
private fun RoundsTab(state: TournamentDetailUiState, onOpenGame: (String) -> Unit) {
    if (state.rounds.isEmpty()) {
        Text(
            "Pairings will appear once the tournament starts.",
            color = TournamentColors.Muted,
            fontSize = 14.sp
        )
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(state.rounds.sortedByDescending { it.roundNumber }, key = { it.id }) { round ->
            Column {
                Text(
                    "ROUND ${round.roundNumber} · ${statusLabel(round.status).uppercase()}",
                    color = TournamentColors.Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    round.pairings.forEach { pairing ->
                        PairingRow(pairing, state.myUserId, onOpenGame)
                    }
                }
            }
        }
    }
}

@Composable
private fun PairingRow(pairing: PairingDto, myUserId: Long?, onOpenGame: (String) -> Unit) {
    val gameId = pairing.gameId
    val mine = myUserId != null &&
            (pairing.whitePlayerId == myUserId || pairing.blackPlayerId == myUserId)
    val title = if (pairing.isBye) {
        "${pairing.whiteUsername ?: pairing.blackUsername ?: "Player"} · bye"
    } else {
        "${pairing.whiteUsername ?: "TBD"} vs ${pairing.blackUsername ?: "TBD"}"
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TournamentColors.Card)
            .then(if (gameId != null) Modifier.clickable { onOpenGame(gameId) } else Modifier)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = if (mine) TournamentColors.Accent else TournamentColors.Text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            resultLabel(pairing.result).ifEmpty { if (gameId != null) "Open" else "" },
            color = TournamentColors.Muted,
            fontSize = 13.sp
        )
    }
}

/** The round number and game id of my unfinished game, if any. */
private fun activeGame(state: TournamentDetailUiState): Pair<Int, String>? {
    val me = state.myUserId ?: return null
    state.rounds.sortedByDescending { it.roundNumber }.forEach { round ->
        round.pairings.firstOrNull { p ->
            !p.isBye &&
                    p.gameId != null &&
                    p.result.isNullOrBlank() &&
                    p.status != "completed" &&
                    (p.whitePlayerId == me || p.blackPlayerId == me)
        }?.let { p -> return round.roundNumber to p.gameId.orEmpty() }
    }
    return null
}