package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.abhinav.caisarra.presentation.game.BotGameViewModel
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.components.GameBottomBar
import com.abhinav.caisarra.presentation.game.components.GameResultDialog
import com.abhinav.caisarra.presentation.game.components.MoveList
import com.abhinav.caisarra.presentation.game.components.PromotionDialog
import com.abhinav.caisarra.presentation.game.model.GameUiState

private val ScreenBg = Color(0xFF080D13)
private val Accent = Color(0xFF10B981)
private val BannerBg = Color(0xFF3B1F24)

@Composable
fun BotGameScreen(
    viewModel: BotGameViewModel,
    onHome: () -> Unit,
    onNewGame: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val thinking by viewModel.botThinking.collectAsState()

    if (!state.isLoading && state.board.isEmpty() && state.errorMessage != null) {
        CenteredColumn {
            Text(text = state.errorMessage.orEmpty(), color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onHome) {
                Text(text = "HOME", color = Accent)
            }
        }
        return
    }

    if (state.isLoading) {
        CenteredColumn {
            CircularProgressIndicator(color = Accent)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Loading game...", color = Color.White)
        }
        return
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .systemBarsPadding()
    ) {
        val isWide = maxWidth > maxHeight && maxWidth >= 600.dp
        val boardSize = if (isWide) {
            minOf(maxHeight - 24.dp, maxWidth * 0.55f)
        } else {
            minOf(maxWidth - 24.dp, maxHeight * 0.5f)
        }

        if (isWide) {
            Row(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(boardSize)) {
                    BotBoard(state, viewModel::onSquareClick)
                }
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    BotTopBar(onHome, viewModel::flipBoard)
                    state.errorMessage?.let { message ->
                        Text(message, color = Color.White, fontSize = 12.sp, modifier = Modifier.background(BannerBg, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp))
                        Spacer(Modifier.height(6.dp))
                    }
                    TopPlayer(state, thinking)
                    MoveList(state.moves, Modifier.weight(1f).padding(vertical = 8.dp))
                    BottomPlayer(state, thinking)
                    Spacer(Modifier.height(8.dp))
                    BotActions(state.gameResult == null, viewModel::resign)
                }
            }
        } else Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BotTopBar(onHome = onHome, onFlip = viewModel::flipBoard)

                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .width(boardSize)
                            .background(BannerBg, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                TopPlayer(state = state, thinking = thinking, modifier = Modifier.width(boardSize))
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.size(boardSize)) {
                    BotBoard(state, viewModel::onSquareClick)
                }
                Spacer(modifier = Modifier.height(6.dp))
                BottomPlayer(state = state, thinking = thinking, modifier = Modifier.width(boardSize))

                MoveList(
                    moves = state.moves,
                    modifier = Modifier
                        .weight(1f)
                        .width(boardSize)
                        .padding(vertical = 8.dp)
                )
            }

            BotActions(state.gameResult == null, viewModel::resign)
        }
    }

    state.promotionPending?.let {
        PromotionDialog(onPieceSelected = viewModel::onPromote)
    }

    state.gameResult?.let { result ->
        GameResultDialog(
            result = result,
            onNewGame = onNewGame,
            onHome = onHome,
            whitePlayerName = state.whitePlayerName,
            blackPlayerName = state.blackPlayerName
        )
    }
}

@Composable
private fun BotBoard(state: GameUiState, onSquareClick: (com.abhinav.caisarra.presentation.game.model.Square) -> Unit) {
    ChessBoard(
        board = state.board,
        selectedSquare = state.selectedSquare,
        legalMoves = state.legalMoves,
        lastMoveFrom = state.lastMoveFrom,
        lastMoveTo = state.lastMoveTo,
        isWhiteInCheck = state.isWhiteInCheck,
        isBlackInCheck = state.isBlackInCheck,
        isFlipped = state.isBoardFlipped,
        onSquareClick = onSquareClick
    )
}

@Composable
private fun BotActions(gameOngoing: Boolean, onResign: () -> Unit) {
    GameBottomBar(
        canUndo = false,
        canRedo = false,
        optionsEnabled = gameOngoing,
        onUndo = {},
        onRedo = {},
        onOfferDraw = {},
        onResign = onResign,
        showDraw = false,
        showUndoRedo = false
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
            Text(text = "Home", color = Color.White)
        }
        Text(
            text = "MOSAIC",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        TextButton(onClick = onFlip) {
            Text(text = "Flip", color = Color.White)
        }
    }
}

@Composable
private fun TopPlayer(state: GameUiState, thinking: Boolean, modifier: Modifier = Modifier) {
    if (state.isBoardFlipped) {
        PlayerBar(state.whitePlayerName, state.isWhiteTurn, thinking, modifier)
    } else {
        PlayerBar(state.blackPlayerName, !state.isWhiteTurn, thinking, modifier)
    }
}

@Composable
private fun BottomPlayer(state: GameUiState, thinking: Boolean, modifier: Modifier = Modifier) {
    if (state.isBoardFlipped) {
        PlayerBar(state.blackPlayerName, !state.isWhiteTurn, thinking, modifier)
    } else {
        PlayerBar(state.whitePlayerName, state.isWhiteTurn, thinking, modifier)
    }
}

@Composable
private fun PlayerBar(name: String, isActive: Boolean, thinking: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isActive) Color(0xFF263B30) else Color(0xFF151F2B), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF304154), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
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
