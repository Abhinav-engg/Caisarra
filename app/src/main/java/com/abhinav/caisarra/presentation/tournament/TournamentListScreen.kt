package com.abhinav.caisarra.presentation.tournament

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.data.remote.dto.TournamentDto

@Composable
fun TournamentListScreen(
    viewModel: TournamentListViewModel,
    onCreate: () -> Unit,
    onOpenTournament: (id: String, inviteCode: String?) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showInviteCode by remember { mutableStateOf(false) }
    var isCreateActive by remember { mutableStateOf(false) }
    var selectedTournamentId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
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

            Spacer(Modifier.height(16.dp))

            // Two main action cards: Join a Tournament? & Create a Tournament?
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ActionCard(
                    title = "Join a\nTournament?",
                    isActive = showInviteCode,
                    onClick = {
                        showInviteCode = !showInviteCode
                        if (showInviteCode) {
                            isCreateActive = false
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                ActionCard(
                    title = "Create a\nTournament?",
                    isActive = isCreateActive,
                    onClick = {
                        isCreateActive = true
                        showInviteCode = false
                        onCreate()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Expandable Invite Code card
            AnimatedVisibility(
                visible = showInviteCode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(TournamentColors.Card)
                        .border(1.dp, Color(0xFF1F2D3D), RoundedCornerShape(18.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Invite Code",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(12.dp))

                    TournamentTextField(
                        value = state.code,
                        onValueChange = viewModel::onCodeChange,
                        placeholder = "Enter your Invite code",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                    )

                    state.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = TournamentColors.Danger, fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(16.dp))

                    TournamentActionButton(
                        text = "Join",
                        onClick = {
                            viewModel.lookupCode { tournament, code ->
                                onOpenTournament(tournament.id, code)
                            }
                        },
                        loading = state.lookingUp,
                        enabled = !state.lookingUp && state.code.isNotBlank()
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "OPEN TOURNAMENTS",
                color = TournamentColors.Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Spacer(Modifier.height(12.dp))

            when {
                state.loading && state.tournaments.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TournamentColors.Accent)
                    }
                }

                state.tournaments.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No open tournaments right now.\nCreate one or join with an invite code.",
                            color = TournamentColors.Muted,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.tournaments, key = { it.id }) { tournament ->
                            TournamentCard(
                                tournament = tournament,
                                isSelected = selectedTournamentId == tournament.id,
                                onClick = {
                                    selectedTournamentId = tournament.id
                                    onOpenTournament(tournament.id, null)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(115.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(TournamentColors.Card)
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) TournamentColors.ActiveBorder else TournamentColors.CardBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun TournamentCard(
    tournament: TournamentDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TournamentColors.Card)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) TournamentColors.ActiveBorder else TournamentColors.CardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = tournament.name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Format: ${formatLabel(tournament.format)} | Control: ${tournament.timeControl} | Round: ${tournament.currentRound} of ${tournament.totalRounds}",
            color = TournamentColors.Muted,
            fontSize = 12.5.sp
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${tournament.players ?: 0}/${tournament.maxPlayers} players",
                color = Color(0xFFC4D0DC),
                fontSize = 13.sp
            )

            val (statusText, statusColor) = when {
                tournament.status.equals("registration", ignoreCase = true) ->
                    "Registration Open" to TournamentColors.Success
                tournament.status.equals("completed", ignoreCase = true) ->
                    "Completed" to TournamentColors.Muted
                else ->
                    "Registration Closed" to TournamentColors.Danger
            }

            Text(
                text = statusText,
                color = statusColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
