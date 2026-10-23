package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.presentation.common.singleClickable
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.components.ChessMoveSound
import com.abhinav.caisarra.presentation.game.components.PromotionDialog
import com.abhinav.caisarra.presentation.puzzles.RushUiState
import com.abhinav.caisarra.presentation.puzzles.RushViewModel
import com.abhinav.caisarra.presentation.puzzles.components.RushResultSheet
import com.abhinav.caisarra.presentation.puzzles.components.RushStrikesIndicator

@Composable
fun RushScreen(
    viewModel: RushViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    when (val uiState = state) {
        is RushUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF080D13)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF23CDE8))
            }
        }

        is RushUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF080D13))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.message,
                        color = Color(0xFFF87171),
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GeneralButton(
                        text = "Retry",
                        onClick = { viewModel.startRush() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GeneralButton(
                        text = "Go Back",
                        onClick = onBack
                    )
                }
            }
        }

        is RushUiState.Playing -> {
            RushPlayingContent(
                state = uiState,
                viewModel = viewModel,
                onQuit = onBack
            )
        }

        is RushUiState.GameOver -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF080D13))
                    .systemBarsPadding(),
                contentAlignment = Alignment.BottomCenter
            ) {
                RushResultSheet(
                    summary = uiState.summary,
                    onPlayAgain = { viewModel.startRush() },
                    onClose = onBack
                )
            }
        }
    }
}

@Composable
private fun RushPlayingContent(
    state: RushUiState.Playing,
    viewModel: RushViewModel,
    onQuit: () -> Unit
) {
    ChessMoveSound(
        board = state.board,
        moveEventId = state.moveAnimationEvent?.id,
        moveTo = state.moveAnimationEvent?.to
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080D13))
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.quitRush() }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Quit",
                    tint = Color.White
                )
            }

            Text(
                text = if (state.mode == "survival") "SURVIVAL RUSH" else "PUZZLE RUSH",
                color = Color.White,
                fontFamily = JetBrainsMono,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            if (state.secondsRemaining != null) {
                val mins = state.secondsRemaining / 60
                val secs = state.secondsRemaining % 60
                val timerColor = if (state.secondsRemaining <= 30) Color(0xFFF87171) else Color(0xFF23CDE8)
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = String.format("%02d:%02d", mins, secs),
                        color = timerColor,
                        fontFamily = JetBrainsMono,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "3 LIVES",
                        color = Color(0xFF23CDE8),
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SCORE: ",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.score}",
                    color = Color(0xFF34D399),
                    fontSize = 20.sp,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold
                )
            }

            RushStrikesIndicator(strikes = state.strikes)

            Box(
                modifier = Modifier
                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "#${state.currentPuzzle.rating}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            val boardSize = maxWidth
            Box(
                modifier = Modifier
                    .size(boardSize)
                    .aspectRatio(1f)
            ) {
                ChessBoard(
                    board = state.board,
                    selectedSquare = state.selectedSquare ?: state.highlightedSquare,
                    legalMoves = state.legalMoves,
                    lastMoveFrom = state.lastMoveFrom,
                    lastMoveTo = state.lastMoveTo,
                    isWhiteInCheck = state.isWhiteInCheck,
                    isBlackInCheck = state.isBlackInCheck,
                    isFlipped = state.isBoardFlipped,
                    moveAnimationEvent = state.moveAnimationEvent,
                    moveAnimationConsumedId = state.moveAnimationConsumedId,
                    onMoveAnimationStarted = viewModel::onMoveAnimationStarted,
                    onSquareClick = { square ->
                        viewModel.onSquareClicked(square)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                    .singleClickable(enabled = !state.isAwaitingServer) {
                        viewModel.requestHint()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Hint",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Hint",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                    .singleClickable(enabled = !state.isAwaitingServer) {
                        viewModel.skipPuzzle()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Skip",
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (state.secondsRemaining != null) "Skip (-45s)" else "Skip",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    state.promotionPending?.let {
        PromotionDialog(
            onPieceSelected = { pieceType ->
                viewModel.onPromotionPieceSelected(pieceType)
            }
        )
    }
}
