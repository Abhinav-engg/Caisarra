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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.game.OnlineGameViewModel
import com.abhinav.caisarra.presentation.game.components.ChatPanel
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.components.ClockBar
import com.abhinav.caisarra.presentation.game.components.GameBottomBar
import com.abhinav.caisarra.presentation.game.components.GameResultDialog
import com.abhinav.caisarra.presentation.game.components.MoveList
import com.abhinav.caisarra.presentation.game.components.PromotionDialog
import com.abhinav.caisarra.presentation.game.model.GameUiState

private val ScreenBg = Color(0xFF080D13)
private val SheetBg = Color(0xFF111A24)
private val Accent = Color(0xFF10B981)
private val BannerBg = Color(0xFF3B1F24)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineGameScreen(
    viewModel: OnlineGameViewModel,
    onHome: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val waiting by viewModel.waitingForOpponent.collectAsState()
    val chat by viewModel.chat.collectAsState()
    val myUserId by viewModel.myUserId.collectAsState()

    var showChat by remember { mutableStateOf(false) }
    var seenCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(showChat, chat.size) {
        if (showChat) seenCount = chat.size
    }
    val unread = if (showChat) 0 else chat.drop(seenCount).count { it.senderId != myUserId }

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

    if (waiting) {
        CenteredColumn {
            CircularProgressIndicator(color = Accent)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Waiting for opponent to join...", color = Color.White)
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
        val boardSize = minOf(maxWidth - 24.dp, maxHeight * 0.5f)

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnlineTopBar(
                    unread = unread,
                    onHome = onHome,
                    onFlip = viewModel::flipBoard,
                    onChat = { showChat = true }
                )

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

                TopClock(state = state, modifier = Modifier.width(boardSize))
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.size(boardSize)) {
                    ChessBoard(
                        board = state.board,
                        selectedSquare = state.selectedSquare,
                        legalMoves = state.legalMoves,
                        lastMoveFrom = state.lastMoveFrom,
                        lastMoveTo = state.lastMoveTo,
                        isWhiteInCheck = state.isWhiteInCheck,
                        isBlackInCheck = state.isBlackInCheck,
                        isFlipped = state.isBoardFlipped,
                        onSquareClick = viewModel::onSquareClick
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                BottomClock(state = state, modifier = Modifier.width(boardSize))

                MoveList(
                    moves = state.moves,
                    modifier = Modifier
                        .weight(1f)
                        .width(boardSize)
                        .padding(vertical = 8.dp)
                )
            }

            GameBottomBar(
                canUndo = false,
                canRedo = false,
                optionsEnabled = state.gameResult == null,
                onUndo = {},
                onRedo = {},
                onOfferDraw = {},
                onResign = viewModel::resign,
                showDraw = false,
                showUndoRedo = false
            )
        }
    }

    if (showChat) {
        ModalBottomSheet(
            onDismissRequest = { showChat = false },
            containerColor = SheetBg
        ) {
            ChatPanel(
                messages = chat,
                myUserId = myUserId,
                onSend = viewModel::sendChat,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .imePadding()
            )
        }
    }

    state.promotionPending?.let {
        PromotionDialog(onPieceSelected = viewModel::onPromote)
    }

    state.gameResult?.let { result ->
        GameResultDialog(
            result = result,
            onNewGame = onHome,
            onHome = onHome
        )
    }
}

@Composable
private fun OnlineTopBar(
    unread: Int,
    onHome: () -> Unit,
    onFlip: () -> Unit,
    onChat: () -> Unit
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
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Row {
            TextButton(onClick = onFlip) {
                Text(text = "Flip", color = Color.White)
            }
            TextButton(onClick = onChat) {
                Text(
                    text = if (unread > 0) "Chat ($unread)" else "Chat",
                    color = if (unread > 0) Accent else Color.White
                )
            }
        }
    }
}

@Composable
private fun TopClock(state: GameUiState, modifier: Modifier = Modifier) {
    if (state.isBoardFlipped) {
        ClockBar(
            playerName = state.whitePlayerName,
            timeMillis = state.whiteTimeMillis,
            isActive = state.isWhiteTurn,
            modifier = modifier
        )
    } else {
        ClockBar(
            playerName = state.blackPlayerName,
            timeMillis = state.blackTimeMillis,
            isActive = !state.isWhiteTurn,
            modifier = modifier
        )
    }
}

@Composable
private fun BottomClock(state: GameUiState, modifier: Modifier = Modifier) {
    if (state.isBoardFlipped) {
        ClockBar(
            playerName = state.blackPlayerName,
            timeMillis = state.blackTimeMillis,
            isActive = !state.isWhiteTurn,
            modifier = modifier
        )
    } else {
        ClockBar(
            playerName = state.whitePlayerName,
            timeMillis = state.whiteTimeMillis,
            isActive = state.isWhiteTurn,
            modifier = modifier
        )
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