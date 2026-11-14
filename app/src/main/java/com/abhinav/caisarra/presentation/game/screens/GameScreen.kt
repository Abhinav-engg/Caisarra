package com.abhinav.caisarra.presentation.game.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.features.gameplay.presentation.GameActions
import com.abhinav.caisarra.features.gameplay.presentation.MoveNavigationBar
import com.abhinav.caisarra.features.gameplay.presentation.StatePlayerCard
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.components.GameResultDialog
import com.abhinav.caisarra.presentation.game.components.MoveList
import com.abhinav.caisarra.presentation.game.components.PromotionDialog
import com.abhinav.caisarra.presentation.game.model.GameIntent
import com.abhinav.caisarra.presentation.game.model.GameUiState
import com.abhinav.caisarra.presentation.game.viewmodel.GameViewModel
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.abhinav.caisarra.presentation.game.components.GameDecision
import com.abhinav.caisarra.presentation.game.components.GameDecisionDialog
import com.abhinav.caisarra.presentation.game.components.ChessMoveSound
import com.abhinav.caisarra.presentation.game.components.MoveTicker
import androidx.compose.runtime.LaunchedEffect
import com.abhinav.caisarra.presentation.game.components.rememberAutoQueenEnabled
private val ScreenBg = Color(0xFF080D13)
private val Accent = Color(0xFF10B981)

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onHome: () -> Unit,
    onNewGame: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    val autoQueen by rememberAutoQueenEnabled()

    LaunchedEffect(state.promotionPending, autoQueen) {
        if (state.promotionPending != null && autoQueen) {
            viewModel.onIntent(GameIntent.Promote(
                com.abhinav.caisarra.presentation.game.model.PieceType.QUEEN
            ))
        }
    }
    ChessMoveSound(
        board = state.board,
        moveEventId = state.moveAnimationEvent?.id,
        moveTo = state.moveAnimationEvent?.to
    )
    var showHomeResignDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember(state.gameResult != null) { mutableStateOf(state.gameResult != null) }
    val requestHome = {
        if (state.gameResult == null) showHomeResignDialog = true else onHome()
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

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    viewModel.pauseAndSaveGame()
                }

                Lifecycle.Event.ON_START -> {
                    viewModel.resumeClock()
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    if (state.isLoading) {
        CenteredColumn {
            CircularProgressIndicator(color = Accent)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Loading game...", color = Color.White)
        }
        return
    }

    state.errorMessage?.let { error ->
        CenteredColumn {
            Text(text = error, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onHome) {
                Text(text = "HOME", color = Accent)
            }
        }
        return
    }

    val onIntent = viewModel::onIntent

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
                    Board(state = state, onIntent = onIntent,moveAnimationConsumedId = state.moveAnimationConsumedId,onMoveAnimationStarted = viewModel::onMoveAnimationStarted)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    CaisaaraTopBar(title = "PASS & PLAY", onBack = requestHome)
                    Spacer(modifier = Modifier.height(4.dp))

                    StatePlayerCard(state, state.isBoardFlipped)
                    MoveList(
                        moves = state.moves,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    )
                    StatePlayerCard(state, !state.isBoardFlipped)
                    Spacer(modifier = Modifier.height(8.dp))
                    PassAndPlayActions(state = state, onIntent = onIntent)
                    MoveNavigationBar(state.canUndo, state.canRedo, { onIntent(GameIntent.Undo) }, { onIntent(GameIntent.Redo) }, Modifier.padding(top = 8.dp))
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
                    CaisaaraTopBar(title = "PASS & PLAY", onBack = requestHome)

                    if (state.gameResult != null && !showResultDialog) {
                        GameOverPill(onClick = { showResultDialog = true })
                    }
                }
                StatePlayerCard(state, state.isBoardFlipped, Modifier.width(boardSize))
                Box(
                    modifier = Modifier.size(boardSize)
                ) {
                    Board(
                        state = state,
                        onIntent = onIntent,
                        moveAnimationConsumedId = state.moveAnimationConsumedId,
                        onMoveAnimationStarted = viewModel::onMoveAnimationStarted
                    )
                }
                StatePlayerCard(state, !state.isBoardFlipped, Modifier.width(boardSize))
                MoveTicker(moves = state.moves, modifier = Modifier.width(boardSize))
                PassAndPlayActions(state = state, onIntent = onIntent, modifier = Modifier.padding(bottom = 8.dp))
            }
            MoveNavigationBar(state.canUndo, state.canRedo, { onIntent(GameIntent.Undo) }, { onIntent(GameIntent.Redo) })
            }
        }

    }

    if (!autoQueen) {
        state.promotionPending?.let {
            PromotionDialog(
                onPieceSelected = { pieceType ->
                    onIntent(GameIntent.Promote(pieceType))
                }
            )
        }
    }
    if (showHomeResignDialog) {
        GameDecisionDialog(
            decision = GameDecision.Resign,
            onConfirm = {
                showHomeResignDialog = false
                onIntent(GameIntent.Resign)
            },
            onDismiss = { showHomeResignDialog = false }
        )
    }

    if (showResultDialog) {
        state.gameResult?.let { result ->
            GameResultDialog(
                result = result,
                onNewGame = onNewGame,
                onHome = onHome,
                whitePlayerName = state.whitePlayerName,
                blackPlayerName = state.blackPlayerName,
                onDismiss = { showResultDialog = false }
            )
        }
    }
}

@Composable
private fun Board(
    state: GameUiState,
    onIntent: (GameIntent) -> Unit,
    moveAnimationConsumedId: Long?,
    onMoveAnimationStarted: (Long) -> Unit

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
        moveAnimationConsumedId = moveAnimationConsumedId,
        onMoveAnimationStarted = onMoveAnimationStarted,
        onSquareClick = { square ->
            onIntent(GameIntent.SquareClicked(square))
        }
    )
}

@Composable
private fun PassAndPlayActions(
    state: GameUiState,
    onIntent: (GameIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    GameActions(
        gameOngoing = state.gameResult == null,
        isBoardFlipped = state.isBoardFlipped,
        onResign = { onIntent(GameIntent.Resign) },
        onFlip = { onIntent(GameIntent.FlipBoard) },
        onOfferDraw = { onIntent(GameIntent.OfferDraw) },
        autoFlip = state.autoFlip,
        onAutoFlipChange = { onIntent(GameIntent.SetAutoFlip(it)) },
        modifier = modifier
    )
}

@Composable
private fun CenteredColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        content()
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
