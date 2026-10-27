package com.abhinav.caisarra.presentation.watch

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.components.ClockBar
import com.abhinav.caisarra.presentation.game.components.MoveTicker
import com.caisaara.ui.theme.RedNormal
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsGreen
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.ThemeNeonDeep

@Composable
fun SpectatorGameScreen(
    viewModel: SpectatorGameViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showLeaveDialog by remember { mutableStateOf(false) }

    fun handleLeave() {
        viewModel.leave()
        onBack()
    }

    BackHandler {
        if (state.status == "active" && !state.isWatchEnded) {
            showLeaveDialog = true
        } else {
            handleLeave()
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            containerColor = Color(0xFF111A24),
            title = {
                Text(
                    text = "Leave Spectator Room?",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "You will stop watching this live game.",
                    fontFamily = JetBrainsMono,
                    fontSize = 13.sp,
                    color = StatsTextMuted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    handleLeave()
                }) {
                    Text(
                        text = "LEAVE",
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        color = RedNormal
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text(
                        text = "STAY",
                        fontFamily = JetBrainsMono,
                        color = StatsTextMuted
                    )
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
    ) {
        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = StatsCyan,
                    modifier = Modifier.size(44.dp),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "CONNECTING TO SPECTATOR ROOM...",
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = StatsTextMuted
                )
            }
        } else if (!state.errorMessage.isNullOrBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF281116), CircleShape)
                        .border(1.dp, Color(0xFF5A1E24), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = RedNormal,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "CANNOT WATCH GAME",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.errorMessage.orEmpty(),
                    fontFamily = JetBrainsMono,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = StatsTextMuted
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { handleLeave() },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonDeep)
                ) {
                    Text(
                        text = "BACK TO WATCH HUB",
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        color = StatsCyan
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SpectatorTopBar(
                    spectatorCount = state.spectatorCount,
                    onBack = {
                        if (state.status == "active" && !state.isWatchEnded) {
                            showLeaveDialog = true
                        } else {
                            handleLeave()
                        }
                    },
                    onFlip = viewModel::flipBoard
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (state.isFlipped) {
                    ClockBar(
                        playerName = state.whitePlayerName,
                        timeMillis = state.whiteTimeMs,
                        isActive = state.isWhiteTurn && state.status == "active" && !state.isWatchEnded,
                        rating = state.whiteRating,
                        capturedPieces = state.capturedWhitePieces
                    )
                } else {
                    ClockBar(
                        playerName = state.blackPlayerName,
                        timeMillis = state.blackTimeMs,
                        isActive = !state.isWhiteTurn && state.status == "active" && !state.isWatchEnded,
                        rating = state.blackRating,
                        capturedPieces = state.capturedBlackPieces
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentAlignment = Alignment.Center
                ) {
                    val boardSize = minOf(maxWidth, maxHeight)

                    Box(modifier = Modifier.size(boardSize)) {
                        ChessBoard(
                            board = state.board,
                            selectedSquare = null,
                            legalMoves = emptyList(),
                            lastMoveFrom = state.lastMoveFrom,
                            lastMoveTo = state.lastMoveTo,
                            isWhiteInCheck = state.isWhiteInCheck,
                            isBlackInCheck = state.isBlackInCheck,
                            isFlipped = state.isFlipped,
                            moveAnimationEvent = state.moveAnimationEvent,
                            moveAnimationConsumedId = state.moveAnimationConsumedId,
                            onMoveAnimationStarted = viewModel::onMoveAnimationStarted,
                            onSquareClick = { }
                        )

                        if (state.status == "finished" || state.isWatchEnded) {
                            val resultText = when {
                                state.result == "white_win" -> "${state.whitePlayerName} Won"
                                state.result == "black_win" -> "${state.blackPlayerName} Won"
                                state.result == "draw" -> "Game Drawn"
                                else -> "Game Finished"
                            }
                            val reasonText = state.endReason?.takeIf { it.isNotBlank() } ?: "Session ended"

                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp)
                                    .background(Color(0xE60B1017), RoundedCornerShape(12.dp))
                                    .border(1.dp, StatsCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = resultText.uppercase(),
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = StatsCyan
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = reasonText,
                                        fontFamily = JetBrainsMono,
                                        fontSize = 12.sp,
                                        color = StatsTextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (state.isFlipped) {
                    ClockBar(
                        playerName = state.blackPlayerName,
                        timeMillis = state.blackTimeMs,
                        isActive = !state.isWhiteTurn && state.status == "active" && !state.isWatchEnded,
                        rating = state.blackRating,
                        capturedPieces = state.capturedBlackPieces
                    )
                } else {
                    ClockBar(
                        playerName = state.whitePlayerName,
                        timeMillis = state.whiteTimeMs,
                        isActive = state.isWhiteTurn && state.status == "active" && !state.isWatchEnded,
                        rating = state.whiteRating,
                        capturedPieces = state.capturedWhitePieces
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                MoveTicker(
                    moves = state.moves,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun SpectatorTopBar(
    spectatorCount: Int,
    onBack: () -> Unit,
    onFlip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0xFF0F1722), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF1B2838), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Leave",
                tint = StatsTextWhite,
                modifier = Modifier.size(20.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .background(Color(0xFF1A3326), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(StatsGreen, CircleShape)
                )
                Text(
                    text = "LIVE",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = StatsGreen,
                    letterSpacing = 0.5.sp
                )
            }

            Row(
                modifier = Modifier
                    .background(Color(0xFF142230), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = StatsCyan,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "$spectatorCount",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = StatsCyan
                )
            }
        }

        IconButton(onClick = onFlip, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = "Flip Board",
                tint = StatsTextMuted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
