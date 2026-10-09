package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
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
import com.abhinav.caisarra.presentation.game.components.CapturedPieces
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.components.ClockBar
import com.abhinav.caisarra.presentation.game.components.GameBottomBar
import com.abhinav.caisarra.presentation.game.components.GameResultDialog
import com.abhinav.caisarra.presentation.game.components.MoveList
import com.abhinav.caisarra.presentation.game.components.PromotionDialog
import com.abhinav.caisarra.presentation.game.model.GameIntent
import com.abhinav.caisarra.presentation.game.model.GameUiState
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.viewmodel.GameViewModel

private val ScreenBg = Color(0xFF080D13)
private val Accent = Color(0xFF10B981)

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onHome: () -> Unit,
    onNewGame: () -> Unit
) {
    val state by viewModel.state.collectAsState()

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
                    Board(state = state, onIntent = onIntent)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    TopBar(
                        onHome = onHome,
                        onFlip = { onIntent(GameIntent.FlipBoard) }
                    )
                    TopPlayerStrip(state = state)
                    MoveList(
                        moves = state.moves,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    )
                    BottomPlayerStrip(state = state)
                    Spacer(modifier = Modifier.height(8.dp))
                    GameActions(state = state, onIntent = onIntent)
                }
            }
        } else {

            val boardSize = minOf(maxWidth - 24.dp, maxHeight * 0.5f)

            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TopBar(
                        onHome = onHome,
                        onFlip = { onIntent(GameIntent.FlipBoard) }
                    )
                    TopPlayerStrip(
                        state = state,
                        modifier = Modifier.width(boardSize)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.size(boardSize)) {
                        Board(state = state, onIntent = onIntent)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    BottomPlayerStrip(
                        state = state,
                        modifier = Modifier.width(boardSize)
                    )
                    MoveList(
                        moves = state.moves,
                        modifier = Modifier
                            .weight(1f)
                            .width(boardSize)
                            .padding(vertical = 8.dp)
                    )
                }
                GameActions(state = state, onIntent = onIntent)
            }
        }
    }

    state.promotionPending?.let {
        PromotionDialog(
            onPieceSelected = { pieceType ->
                onIntent(GameIntent.Promote(pieceType))
            }
        )
    }

    state.gameResult?.let { result ->
        GameResultDialog(
            result = result,
            onNewGame = onNewGame,
            onHome = onHome
        )
    }
}

@Composable
private fun Board(
    state: GameUiState,
    onIntent: (GameIntent) -> Unit
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
        onSquareClick = { square ->
            onIntent(GameIntent.SquareClicked(square))
        }
    )
}

@Composable
private fun TopBar(
    onHome: () -> Unit,
    onFlip: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onHome) {
            Text(text = "Home", color = Color.White)
        }
        Text(
            text = "CAISARRA",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        TextButton(onClick = onFlip) {
            Text(text = "Flip", color = Color.White)
        }
    }
}

@Composable
private fun TopPlayerStrip(
    state: GameUiState,
    modifier: Modifier = Modifier
) {
    if (state.isBoardFlipped) {
        PlayerStrip(
            name = state.whitePlayerName,
            timeMillis = state.whiteTimeMillis,
            isActive = state.isWhiteTurn,
            captured = state.capturedWhitePieces,
            capturedOnTop = false,
            modifier = modifier
        )
    } else {
        PlayerStrip(
            name = state.blackPlayerName,
            timeMillis = state.blackTimeMillis,
            isActive = !state.isWhiteTurn,
            captured = state.capturedBlackPieces,
            capturedOnTop = false,
            modifier = modifier
        )
    }
}

@Composable
private fun BottomPlayerStrip(
    state: GameUiState,
    modifier: Modifier = Modifier
) {
    if (state.isBoardFlipped) {
        PlayerStrip(
            name = state.blackPlayerName,
            timeMillis = state.blackTimeMillis,
            isActive = !state.isWhiteTurn,
            captured = state.capturedBlackPieces,
            capturedOnTop = true,
            modifier = modifier
        )
    } else {
        PlayerStrip(
            name = state.whitePlayerName,
            timeMillis = state.whiteTimeMillis,
            isActive = state.isWhiteTurn,
            captured = state.capturedWhitePieces,
            capturedOnTop = true,
            modifier = modifier
        )
    }
}

@Composable
private fun PlayerStrip(
    name: String,
    timeMillis: Long,
    isActive: Boolean,
    captured: List<Piece>,
    capturedOnTop: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {

        val capturedModifier = Modifier
            .heightIn(min = 20.dp)
            .padding(vertical = 2.dp)

        if (capturedOnTop) {
            CapturedPieces(pieces = captured, modifier = capturedModifier)
        }
        ClockBar(
            playerName = name,
            timeMillis = timeMillis,
            isActive = isActive
        )
        if (!capturedOnTop) {
            CapturedPieces(pieces = captured, modifier = capturedModifier)
        }
    }
}

@Composable
private fun GameActions(
    state: GameUiState,
    onIntent: (GameIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    GameBottomBar(
        canUndo = state.canUndo,
        canRedo = state.canRedo,
        optionsEnabled = state.gameResult == null,
        onUndo = { onIntent(GameIntent.Undo) },
        onRedo = { onIntent(GameIntent.Redo) },
        onOfferDraw = { onIntent(GameIntent.OfferDraw) },
        onResign = { onIntent(GameIntent.Resign) },
        modifier = modifier
    )
}

@Composable
private fun CenteredColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        content()
    }
}