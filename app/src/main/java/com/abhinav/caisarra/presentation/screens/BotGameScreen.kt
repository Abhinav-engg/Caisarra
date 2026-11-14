package com.abhinav.caisarra.presentation.screens

import com.abhinav.caisarra.presentation.game.components.GameOverReveal
import com.abhinav.caisarra.core.ui.components.rememberToaster
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.abhinav.caisarra.core.ui.components.FullScreenError
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.features.gameplay.presentation.GameActions
import com.abhinav.caisarra.features.gameplay.presentation.MoveNavigationBar
import com.abhinav.caisarra.features.gameplay.presentation.StatePlayerCard
import com.abhinav.caisarra.features.gameplay.presentation.ThinkingIndicator
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.presentation.common.singleClick
import com.abhinav.caisarra.presentation.game.BotGameViewModel
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.components.ChessMoveSound
import com.abhinav.caisarra.presentation.game.components.GameDecision
import com.abhinav.caisarra.presentation.game.components.GameDecisionDialog
import com.abhinav.caisarra.presentation.game.components.MoveList
import com.abhinav.caisarra.presentation.game.components.MoveTicker
import com.abhinav.caisarra.presentation.game.components.PromotionDialog
import com.abhinav.caisarra.presentation.game.model.GameResult
import com.abhinav.caisarra.presentation.game.model.GameUiState
import androidx.compose.runtime.LaunchedEffect
import com.abhinav.caisarra.presentation.game.components.rememberAutoQueenEnabled
private val ScreenBg = Color(0xFF080D13)
private val Accent = Color(0xFF10B981)

@Composable
fun BotGameScreen(
    viewModel: BotGameViewModel,
    onHome: () -> Unit,
    onNewGame: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val thinking by viewModel.botThinking.collectAsState()
    val nextBot by viewModel.nextBot.collectAsState()
    val endReason by viewModel.endReason.collectAsState()
    val isStartingNextBot by viewModel.isStartingNextBot.collectAsState()

    val toaster = rememberToaster()
    var showHomeResignDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            toaster.show(message)
        }
    }

    ChessMoveSound(
        board = state.board,
        moveEventId = state.moveAnimationEvent?.id,
        moveTo = state.moveAnimationEvent?.to
    )

    var showResultDialog by remember(state.gameResult != null) { mutableStateOf(state.gameResult != null) }

    val requestHome = {
        if (state.gameResult == null) {
            showHomeResignDialog = true
        } else {
            onHome()
        }
    }
    val autoQueen by rememberAutoQueenEnabled()

    LaunchedEffect(state.promotionPending, autoQueen) {
        if (state.promotionPending != null && autoQueen) {
            viewModel.onPromote(
                com.abhinav.caisarra.presentation.game.model.PieceType.QUEEN
            )
        }
    }

    BackHandler(enabled = true) {
        if (showResultDialog) {
            showResultDialog = false
        } else if (state.gameResult == null) {
            showHomeResignDialog = true
        } else {
            onHome()
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Accent)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Loading game...", color = Color.White, fontFamily = JetBrainsMono)
            }
        }
        return
    }

    if (state.errorMessage != null && state.board.isEmpty()) {
        FullScreenError(state.errorMessage, viewModel::loadGameFallback, onHome)
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBg)
                .systemBarsPadding()
        ) {
            val isWide = maxWidth > maxHeight && maxWidth >= 600.dp

            if (isWide) {
                val boardSize = minOf(maxHeight - 24.dp, maxWidth * 0.55f)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(boardSize)) {
                        BotBoard(
                            state = state,
                            onMoveAnimationStarted = viewModel::onMoveAnimationStarted,
                            onSquareClick = viewModel::onSquareClick
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        CaisaaraTopBar(title = "VS BOT", onBack = requestHome)
                        if (state.gameResult != null && !showResultDialog) {
                            GameOverPill(onClick = { showResultDialog = true })
                        }
                        BotPlayer(state, state.isBoardFlipped, thinking)
                        MoveList(state.moves, Modifier.weight(1f).padding(vertical = 8.dp))
                        BotPlayer(state, !state.isBoardFlipped, thinking)
                        Spacer(Modifier.height(8.dp))
                        GameActions(state.gameResult == null, state.isBoardFlipped, viewModel::resign, viewModel::flipBoard)
                        MoveNavigationBar(state.canUndo, state.canRedo, viewModel::undo, viewModel::redo, Modifier.padding(top = 8.dp))
                    }
                }
            } else {
                val boardSize = maxWidth - 24.dp
                Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CaisaaraTopBar(title = "VS BOT", onBack = requestHome)
                        if (state.gameResult != null && !showResultDialog) {
                            GameOverPill(onClick = { showResultDialog = true })
                        }
                    }

                    BotPlayer(state, state.isBoardFlipped, thinking, Modifier.width(boardSize))

                    Box(modifier = Modifier.size(boardSize)) {
                        BotBoard(
                            state = state,
                            onMoveAnimationStarted = viewModel::onMoveAnimationStarted,
                            onSquareClick = viewModel::onSquareClick
                        )
                    }

                    BotPlayer(state, !state.isBoardFlipped, thinking, Modifier.width(boardSize))
                    MoveTicker(moves = state.moves, modifier = Modifier.width(boardSize))
                    GameActions(state.gameResult == null, state.isBoardFlipped, viewModel::resign, viewModel::flipBoard, Modifier.padding(bottom = 8.dp))
                }
                MoveNavigationBar(state.canUndo, state.canRedo, viewModel::undo, viewModel::redo)
                }
            }
        }
    }

    if (!autoQueen) {
        state.promotionPending?.let {
            PromotionDialog(
                onPieceSelected = viewModel::onPromote
            )
        }
    }

    if (showHomeResignDialog) {
        GameDecisionDialog(
            decision = GameDecision.Resign,
            onConfirm = {
                showHomeResignDialog = false
                viewModel.resign()
            },
            onDismiss = { showHomeResignDialog = false }
        )
    }

    if (showResultDialog) {
        state.gameResult?.let { result ->
            BotResultDialog(
                result = result,
                endReason = endReason,
                nextBot = nextBot,
                isStartingNextBot = isStartingNextBot,
                onPlayNextBot = {
                    viewModel.playNextBot { newGameId ->
                        onNewGame()
                    }
                },
                onNewGame = onNewGame,
                onHome = singleClick(onHome),
                onDismiss = { showResultDialog = false }
            )
        }
    }
}

@Composable
private fun BotBoard(
    state: GameUiState,
    onMoveAnimationStarted: (Long) -> Unit,
    onSquareClick: (com.abhinav.caisarra.presentation.game.model.Square) -> Unit
) {
    ChessBoard(
        board = state.board,
        selectedSquare = state.selectedSquare,
        legalMoves = state.legalMoves,
        lastMoveFrom = state.lastMoveFrom,
        lastMoveTo = state.lastMoveTo,
        isWhiteInCheck = state.isWhiteInCheck,
        isBlackInCheck = state.isBlackInCheck,
        isFlipped = state.isBoardFlipped,
        moveAnimationEvent = state.moveAnimationEvent,
        moveAnimationConsumedId = state.moveAnimationConsumedId,
        onMoveAnimationStarted = onMoveAnimationStarted,
        onSquareClick = onSquareClick
    )
}

@Composable
private fun BotPlayer(state: GameUiState, isWhite: Boolean, thinking: Boolean, modifier: Modifier = Modifier) {
    StatePlayerCard(state, isWhite, modifier) {
        if (thinking && state.isWhiteTurn == isWhite) ThinkingIndicator()
    }
}

@Composable
private fun BotResultDialog(
    result: GameResult,
    endReason: String?,
    nextBot: com.abhinav.caisarra.data.remote.dto.NextBot?,
    isStartingNextBot: Boolean,
    onPlayNextBot: () -> Unit,
    onNewGame: () -> Unit,
    onHome: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        GameOverReveal(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111820), RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF33414E), RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFFAAB7C6)
                    )
                }
            }
            val title = when (result) {
                GameResult.WHITE_WINS -> "White Wins"
                GameResult.BLACK_WINS -> "Black Wins"
                GameResult.DRAW -> "Draw"
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMono
            )

            endReason?.let { reason ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = reason.replace('_', ' ').replaceFirstChar { it.uppercase() },
                    color = Color(0xFFAAB7C6),
                    fontSize = 14.sp,
                    fontFamily = JetBrainsMono
                )
            }

            Spacer(Modifier.height(24.dp))

            if (nextBot != null) {
                GeneralButton(
                    text = if (isStartingNextBot) "STARTING..." else "PLAY NEXT BOT (${nextBot.name})",
                    onClick = onPlayNextBot,
                    enabled = !isStartingNextBot
                )
                Spacer(Modifier.height(12.dp))
            }

            GeneralButton(
                text = "NEW GAME",
                onClick = onNewGame
            )

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onHome) {
                Text(text = "BACK TO HOME", color = Color(0xFFAAB7C6), fontFamily = JetBrainsMono)
            }
        }
        }
    }
}

@Composable
private fun GameOverPill(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFF60A5FA),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Game Over • Tap to view result",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
