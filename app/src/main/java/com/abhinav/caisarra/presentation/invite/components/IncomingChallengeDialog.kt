package com.abhinav.caisarra.presentation.invite.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.data.remote.ChallengeNotificationEvent
import com.abhinav.caisarra.data.repository.ChallengeManager
import kotlinx.coroutines.launch

private val DialogBg = Color(0xFF0F1923)
private val DialogCard = Color(0xFF162534)
private val DialogBorder = Color(0xFF26384D)
private val AccentCyan = Color(0xFF00E5FF)
private val TextWhite = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF8A9AA8)
private val DangerRed = Color(0xFFEF4444)

@Composable
fun IncomingChallengeDialog(
    challenge: ChallengeNotificationEvent.Received,
    onAccept: (gameId: String) -> Unit,
    onDecline: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val challengeManager = remember { ChallengeManager.get(context) }

    var isAccepting by remember { mutableStateOf(false) }
    var isDeclining by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = {
            scope.launch { challengeManager.declineChallenge(challenge.code) }
            onDecline()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val shape = RoundedCornerShape(24.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(shape)
                .background(DialogBg)
                .border(1.dp, DialogBorder, shape)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = challenge.title.ifBlank { "Game Challenge" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    IconButton(
                        onClick = {
                            scope.launch { challengeManager.declineChallenge(challenge.code) }
                            onDecline()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Decline", tint = TextMuted)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Challenger info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DialogCard)
                        .border(1.dp, DialogBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FriendAvatar(name = challenge.challengerUsername, avatarUrl = null, size = 48.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = challenge.challengerUsername,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = challenge.body,
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }

                errorMessage?.let { err ->
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = err,
                        color = DangerRed,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DialogCard)
                            .border(1.dp, DialogBorder, RoundedCornerShape(12.dp))
                            .clickable(enabled = !isDeclining && !isAccepting) {
                                isDeclining = true
                                scope.launch {
                                    challengeManager.declineChallenge(challenge.code)
                                    isDeclining = false
                                    onDecline()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isDeclining) "DECLINING..." else "DECLINE",
                            color = DangerRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentCyan)
                            .clickable(enabled = !isAccepting && !isDeclining) {
                                isAccepting = true
                                errorMessage = null
                                scope.launch {
                                    challengeManager.acceptChallenge(challenge.code)
                                        .onSuccess { res ->
                                            isAccepting = false
                                            onAccept(res.gameId.ifBlank { challenge.gameId })
                                        }
                                        .onFailure { err ->
                                            isAccepting = false
                                            errorMessage = err.message ?: "Failed to accept challenge"
                                        }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAccepting) {
                            CircularProgressIndicator(
                                color = Color(0xFF082028),
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "ACCEPT",
                                color = Color(0xFF082028),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
