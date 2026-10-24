package com.abhinav.caisarra.presentation.tournament

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.data.remote.dto.PairingDto
import com.abhinav.caisarra.data.remote.dto.StandingDto
import com.abhinav.caisarra.data.remote.dto.TournamentDto

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

    TournamentBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            TournamentHeader(
                title = "TOURNAMENTS",
                onBack = onBack
            )

            val tournament = state.tournament

            when {
                tournament == null && state.loading -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TournamentColors.Accent)
                    }
                }

                tournament == null -> {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            state.error ?: "Could not load tournament",
                            color = TournamentColors.Danger,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        TournamentActionButton(
                            text = "Retry",
                            onClick = viewModel::refresh,
                            modifier = Modifier.width(160.dp)
                        )
                    }
                }

                else -> {
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = tournament.name,
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(18.dp))

                    SecondaryTabRow(
                        selectedTabIndex = tab,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        indicator = {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tab, matchContentSize = false),
                                height = 2.5.dp,
                                color = Color.White
                            )
                        },
                        divider = {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = Color(0xFF1E2838)
                            )
                        }
                    ) {
                        val tabs = listOf("Overview", "Standings", "Rounds")
                        tabs.forEachIndexed { index, title ->
                            val selected = tab == index
                            Tab(
                                selected = selected,
                                onClick = { tab = index },
                                text = {
                                    Text(
                                        text = title,
                                        color = if (selected) Color.White else TournamentColors.Muted,
                                        fontSize = 15.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

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
                    .border(1.dp, TournamentColors.ActiveBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "Winner: ${winner?.username ?: "decided"}",
                    color = TournamentColors.Accent,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        InfoRow("Format", formatLabel(tournament.format))
        InfoRow("Time Control", timeControlDisplay(tournament.timeControl))

        val (statusText, statusColor) = when {
            tournament.status.equals("registration", ignoreCase = true) ->
                "Registration Open" to TournamentColors.Success
            tournament.status.equals("completed", ignoreCase = true) ->
                "Completed" to TournamentColors.Muted
            else ->
                statusLabel(tournament.status) to TournamentColors.Danger
        }
        InfoRow("Status", statusText, valueColor = statusColor)

        val totalMax = tournament.maxPlayers
        val currentPlayers = tournament.players ?: state.standings.size
        InfoRow("Players", "$currentPlayers / $totalMax Players")

        InfoRow("Round", "${tournament.currentRound} of ${tournament.totalRounds}")
        InfoRow("Visibility", tournament.visibility.replaceFirstChar { it.uppercase() })

        if (tournament.description.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(
                tournament.description,
                color = TournamentColors.Muted,
                fontSize = 13.5.sp,
                lineHeight = 20.sp
            )
        }

        if (isCreator && tournament.inviteCode.isNotBlank()) {
            Spacer(Modifier.height(20.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TournamentColors.Card)
                    .border(1.dp, TournamentColors.CardBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "INVITE CODE",
                    color = TournamentColors.Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    tournament.inviteCode,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Spacer(Modifier.height(12.dp))
                TournamentActionButton(
                    text = "Share Invite Code",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Join my chess tournament \"${tournament.name}\" with invite code ${tournament.inviteCode}"
                            )
                        }
                        context.startActivity(Intent.createChooser(intent, "Share invite code"))
                    }
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        if (active != null) {
            TournamentActionButton(
                "Play Round ${active.first}",
                onClick = { onOpenGame(active.second) }
            )
            Spacer(Modifier.height(14.dp))
        }

        if (registration) {
            if (!state.hasJoined) {
                TournamentActionButton("Join Tournament", onClick = onJoin)
            } else {
                OutlinedButton(
                    onClick = onLeave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "Leave Tournament",
                        color = TournamentColors.Danger,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (isCreator) {
                Spacer(Modifier.height(14.dp))
                TournamentActionButton("Start Tournament", onClick = onStart)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Needs at least ${tournament.minPlayers} players to start.",
                    color = TournamentColors.Muted,
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TournamentColors.Muted,
            fontSize = 14.5.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StandingsTab(standings: List<StandingDto>, myUserId: Long?) {
    if (standings.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Standings will appear once the tournament starts.",
                color = TournamentColors.Muted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                HeaderCell("#", Modifier.width(30.dp))
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(TournamentColors.Card)
                    .border(
                        1.dp,
                        if (mine) TournamentColors.ActiveBorder else TournamentColors.CardBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val color = if (mine) TournamentColors.Accent else Color.White
                Text(
                    row.rank.toString(),
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(30.dp)
                )
                Text(
                    row.username,
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    scoreLabel(row.score),
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(44.dp)
                )
                Text(
                    scoreLabel(row.buchholz),
                    color = TournamentColors.Muted,
                    fontSize = 13.sp,
                    modifier = Modifier.width(44.dp)
                )
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
        text = text,
        color = TournamentColors.Muted,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = modifier
    )
}

@Composable
private fun RoundsTab(state: TournamentDetailUiState, onOpenGame: (String) -> Unit) {
    if (state.rounds.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Pairings will appear once the tournament starts.",
                color = TournamentColors.Muted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            .clip(RoundedCornerShape(12.dp))
            .background(TournamentColors.Card)
            .border(
                1.dp,
                if (mine) TournamentColors.ActiveBorder else TournamentColors.CardBorder,
                RoundedCornerShape(12.dp)
            )
            .then(if (gameId != null) Modifier.clickable { onOpenGame(gameId) } else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = if (mine) TournamentColors.Accent else Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            resultLabel(pairing.result).ifEmpty { if (gameId != null) "Open" else "" },
            color = if (mine) TournamentColors.Accent else TournamentColors.Muted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

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
