package com.abhinav.caisarra.presentation.invite

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private object JoinColors {
    val Background = Color(0xFF080D13)
    val Card = Color(0xFF111A24)
    val Border = Color(0xFF1F2B38)
    val Accent = Color(0xFF10B981)
    val TextSecondary = Color(0xFF9CA3AF)
    val Error = Color(0xFFEF4444)
}

@Composable
fun JoinInviteScreen(
    viewModel: JoinInviteViewModel,
    onGameReady: (String) -> Unit,
    onLogin: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.gameReady.collect { onGameReady(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JoinColors.Background)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val current = state) {
                JoinInviteState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = JoinColors.Accent)
                    }
                }

                is JoinInviteState.Error -> {
                    Text(
                        text = current.message.ifBlank { "Something went wrong." },
                        color = JoinColors.Error,
                        fontSize = 15.sp
                    )
                    PrimaryButton(text = "TRY AGAIN", onClick = viewModel::load)
                    SecondaryButton(text = "BACK", onClick = onBack)
                }

                is JoinInviteState.Preview -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "GAME INVITE",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${current.inviterName} invited you to play",
                            color = JoinColors.TextSecondary,
                            fontSize = 14.sp
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = JoinColors.Card,
                        border = BorderStroke(1.dp, JoinColors.Border)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailRow(
                                label = "Time control",
                                value = if (current.incrementSeconds > 0) {
                                    "${current.timeControlMinutes} min + ${current.incrementSeconds}s"
                                } else {
                                    "${current.timeControlMinutes} min"
                                }
                            )
                            DetailRow(label = "You play", value = current.yourColor)
                        }
                    }

                    if (current.needsLogin) {
                        Text(
                            text = "Log in to join this game.",
                            color = JoinColors.TextSecondary,
                            fontSize = 13.sp
                        )
                        PrimaryButton(text = "LOG IN", onClick = onLogin)
                    } else {
                        PrimaryButton(
                            text = if (current.joining) "JOINING..." else "JOIN GAME",
                            enabled = !current.joining,
                            onClick = viewModel::join
                        )
                    }
                    SecondaryButton(text = "BACK", onClick = onBack)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = JoinColors.TextSecondary, fontSize = 14.sp)
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = JoinColors.Accent)
    ) {
        Text(text = text, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SecondaryButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, JoinColors.Border)
    ) {
        Text(text = text, color = Color.White)
    }
}