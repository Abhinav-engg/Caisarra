package com.abhinav.caisarra.presentation.screens

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.presentation.common.singleClick
import com.abhinav.caisarra.presentation.game.BotGameViewModel
import com.abhinav.caisarra.presentation.game.components.CapturedPieces
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.components.ChessMoveSound
import com.abhinav.caisarra.presentation.game.components.GameBottomBar
import com.abhinav.caisarra.presentation.game.components.GameDecision
import com.abhinav.caisarra.presentation.game.components.GameDecisionDialog
import com.abhinav.caisarra.presentation.game.components.MoveList
import com.abhinav.caisarra.presentation.game.components.MoveTicker
import com.abhinav.caisarra.presentation.game.components.PromotionDialog
import com.abhinav.caisarra.presentation.game.model.GameResult
import com.abhinav.caisarra.presentation.game.model.GameUiState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.abhinav.caisarra.presentation.game.components.isAutoQueenEnabled
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

    val snackbarHostState = remember { SnackbarHostState() }
    var showHomeResignDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
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
    val context = LocalContext.current
    val autoQueen = isAutoQueenEnabled(context)

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
                        BotTopBar(
                            onHome = singleClick(requestHome),
                            onFlip = singleClick(viewModel::flipBoard)
                        )
                        if (state.gameResult != null && !showResultDialog) {
                            GameOverPill(onClick = { showResultDialog = true })
                        }
                        TopPlayer(state, thinking)
                        MoveList(state.moves, Modifier.weight(1f).padding(vertical = 8.dp))
                        BottomPlayer(state, thinking)
                        Spacer(Modifier.height(8.dp))
                        BotActions(
                            canUndo = state.canUndo,
                            canRedo = state.canRedo,
                            onUndo = viewModel::undo,
                            onRedo = viewModel::redo,
                            gameOngoing = state.gameResult == null,
                            onResign = viewModel::resign
                        )
                    }
                }
            } else {
                val boardSize = maxWidth - 24.dp
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BotTopBar(
                            onHome = singleClick(requestHome),
                            onFlip = singleClick(viewModel::flipBoard)
                        )
                        if (state.gameResult != null && !showResultDialog) {
                            GameOverPill(onClick = { showResultDialog = true })
                        }
                        Spacer(Modifier.height(2.dp))
                        MoveTicker(
                            moves = state.moves,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        )
                    }

                    TopPlayer(
                        state = state,
                        thinking = thinking,
                        modifier = Modifier.width(boardSize)
                    )

                    Box(modifier = Modifier.size(boardSize)) {
                        BotBoard(
                            state = state,
                            onMoveAnimationStarted = viewModel::onMoveAnimationStarted,
                            onSquareClick = viewModel::onSquareClick
                        )
                    }

                    BottomPlayer(
                        state = state,
                        thinking = thinking,
                        modifier = Modifier.width(boardSize)
                    )

                    BotActions(
                        canUndo = state.canUndo,
                        canRedo = state.canRedo,
                        onUndo = viewModel::undo,
                        onRedo = viewModel::redo,
                        gameOngoing = state.gameResult == null,
                        onResign = viewModel::resign
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
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
private fun BotActions(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    gameOngoing: Boolean,
    onResign: () -> Unit
) {
    GameBottomBar(
        canUndo = canUndo,
        canRedo = canRedo,
        optionsEnabled = gameOngoing,
        onUndo = onUndo,
        onRedo = onRedo,
        onOfferDraw = {},
        onResign = onResign,
        showDraw = false,
        showUndoRedo = true
    )
}

@Composable
private fun BotTopBar(onHome: () -> Unit, onFlip: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onHome) {
            Text(text = "Home", color = Color.White, fontFamily = JetBrainsMono)
        }
        Text(
            text = "MOSAIC",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = JetBrainsMono
        )
        TextButton(onClick = onFlip) {
            Text(text = "Flip", color = Color.White, fontFamily = JetBrainsMono)
        }
    }
}

@Composable
private fun TopPlayer(
    state: GameUiState,
    thinking: Boolean,
    modifier: Modifier = Modifier
) {
    if (state.isBoardFlipped) {
        PlayerBar(
            name = state.whitePlayerName,
            isActive = state.isWhiteTurn,
            thinking = thinking,
            captured = state.capturedBlackPieces,
            advantage = state.whiteAdvantage,
            modifier = modifier
        )
    } else {
        PlayerBar(
            name = state.blackPlayerName,
            isActive = !state.isWhiteTurn,
            thinking = thinking,
            captured = state.capturedWhitePieces,
            advantage = state.blackAdvantage,
            modifier = modifier
        )
    }
}

@Composable
private fun BottomPlayer(
    state: GameUiState,
    thinking: Boolean,
    modifier: Modifier = Modifier
) {
    if (state.isBoardFlipped) {
        PlayerBar(
            name = state.blackPlayerName,
            isActive = !state.isWhiteTurn,
            thinking = thinking,
            captured = state.capturedWhitePieces,
            advantage = state.blackAdvantage,
            modifier = modifier
        )
    } else {
        PlayerBar(
            name = state.whitePlayerName,
            isActive = state.isWhiteTurn,
            thinking = thinking,
            captured = state.capturedBlackPieces,
            advantage = state.whiteAdvantage,
            modifier = modifier
        )
    }
}

@Composable
private fun PlayerBar(
    name: String,
    isActive: Boolean,
    thinking: Boolean,
    captured: List<com.abhinav.caisarra.presentation.game.model.Piece>,
    advantage: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isActive) Color(0xFF263B30) else Color(0xFF151F2B),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                Color(0xFF304154),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (captured.isNotEmpty() || advantage > 0) {
                CapturedPieces(
                    pieces = captured,
                    advantage = advantage
                )
            }
        }

        if (isActive && thinking) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    color = Accent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Thinking...",
                    color = Accent,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
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
