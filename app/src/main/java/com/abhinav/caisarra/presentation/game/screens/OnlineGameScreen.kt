package com.abhinav.caisarra.presentation.game.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.data.remote.dto.ChatMessageDto
import com.abhinav.caisarra.presentation.game.OnlineGameViewModel
import com.abhinav.caisarra.presentation.game.components.ChatPanel
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.components.ClockBar
import com.abhinav.caisarra.presentation.game.components.GameBottomBar
import com.abhinav.caisarra.presentation.game.components.GameDecision
import com.abhinav.caisarra.presentation.game.components.GameDecisionDialog
import com.abhinav.caisarra.presentation.game.components.GameResultDialog
import com.abhinav.caisarra.presentation.game.components.MoveList
import com.abhinav.caisarra.presentation.game.components.PromotionDialog
import com.abhinav.caisarra.presentation.game.model.GameResult
import com.abhinav.caisarra.presentation.game.model.GameUiState
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.viewmodel.GameAnalysisViewModel
import kotlinx.coroutines.launch
import com.abhinav.caisarra.presentation.game.components.ChessMoveSound
private val ScreenBg = Color(0xFF080D13)
private val SheetBg = Color(0xFF111A24)
private val Accent = Color(0xFF10B981)
private val BannerBg = Color(0xFF3B1F24)
private val InfoBannerBg = Color(0xFF1F2B38)
private val Scrim = Color(0x99000000)
private val HandleColor = Color(0xFF4A5560)

@Composable
fun OnlineGameScreen(
    viewModel: OnlineGameViewModel,
    analysisViewModel: GameAnalysisViewModel,
    onHome: () -> Unit,
    onNewOpponent: (minutes: Int, incrementSeconds: Int) -> Unit = { _, _ -> onHome() },
    onRematchStarted: (gameId: String, shareLink: String, opponentName: String) -> Unit = { _, _, _ -> },
    onStartAnalysis: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    ChessMoveSound(
        board = state.board,
        moveEventId = state.moveAnimationEvent?.id,
        moveTo = state.moveAnimationEvent?.to
    )
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var rematchBusy by remember { mutableStateOf(false) }
    val waiting by viewModel.waitingForOpponent.collectAsState()
    val chat by viewModel.chat.collectAsState()
    val chatMuted by viewModel.chatMuted.collectAsState()
    val myUserId by viewModel.myUserId.collectAsState()
    val drawOfferedBy by viewModel.drawOfferedBy.collectAsState()
    val opponentName by viewModel.opponentName.collectAsState()

    var showChat by remember { mutableStateOf(false) }
    var showHomeResignDialog by remember { mutableStateOf(false) }
    val requestHome = {
        if (state.gameResult == null && !waiting) {
            showHomeResignDialog = true
        } else {
            onHome()
        }
    }
    var seenCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(showChat, chatMuted, chat.size) {
        if (showChat || chatMuted) seenCount = chat.size
    }

    BackHandler(enabled = showChat || state.gameResult != null) {
        if (showChat) showChat = false else onHome()
    }

    val unread = if (showChat || chatMuted) 0 else chat.drop(seenCount).count { it.senderId != myUserId }
    val gameOngoing = state.gameResult == null
    val incomingDrawOffer = gameOngoing && drawOfferedBy != null && drawOfferedBy != myUserId
    val drawOfferSent = gameOngoing && drawOfferedBy != null && drawOfferedBy == myUserId

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

    Box(modifier = Modifier.fillMaxSize()) {
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
                        OnlineBoard(state, viewModel::onSquareClick)
                    }
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        OnlineTopBar(state.isRated, unread, chatMuted, requestHome, viewModel::flipBoard) { showChat = true }
                        state.errorMessage?.let { Banner(it, BannerBg); Spacer(Modifier.height(6.dp)) }
                        if (drawOfferSent) {
                            Banner("Draw offer sent. Waiting for your opponent...", InfoBannerBg)
                            Spacer(Modifier.height(6.dp))
                        }
                        TopClock(state)
                        MoveList(
                            moves = state.moves,
                            modifier = Modifier.weight(1f).padding(vertical = 8.dp)
                        )
                        BottomClock(state)
                        Spacer(Modifier.height(8.dp))
                        OnlineActions(gameOngoing, viewModel::offerDraw, viewModel::resign)
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
                    OnlineTopBar(
                        isRated = state.isRated,
                        unread = unread,
                        muted = chatMuted,
                        onHome = requestHome,
                        onFlip = viewModel::flipBoard,
                        onChat = { showChat = true }
                    )

                    state.errorMessage?.let { message ->
                        Banner(text = message, background = BannerBg, modifier = Modifier.width(boardSize))
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (drawOfferSent) {
                        Banner(
                            text = "Draw offer sent. Waiting for your opponent...",
                            background = InfoBannerBg,
                            modifier = Modifier.width(boardSize)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    TopClock(state = state, modifier = Modifier.width(boardSize))
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.size(boardSize)) {
                        OnlineBoard(state, viewModel::onSquareClick)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    BottomClock(state = state, modifier = Modifier.width(boardSize))
                    if (state.isDailyMode) {
                        state.lastMoveAccepted?.let { accepted ->
                            Text(
                                text = if (accepted) {
                                    "✓ Move accepted"
                                } else {
                                    "✕ Move not accepted"
                                },
                                color = if (accepted) {
                                    Color(0xFF10B981)
                                } else {
                                    Color(0xFFEF4444)
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    MoveList(
                        moves = state.moves,
                        modifier = Modifier
                            .weight(1f)
                            .width(boardSize)
                            .padding(vertical = 8.dp)
                    )
                }

                OnlineActions(gameOngoing, viewModel::offerDraw, viewModel::resign)
            }
        }

        if (showChat) {
            ChatOverlay(
                chat = chat,
                myUserId = myUserId,
                muted = chatMuted,
                onToggleMute = viewModel::toggleChatMute,
                onSend = viewModel::sendChat,
                onDismiss = { showChat = false }
            )
        }
    }
    if (showHomeResignDialog) {
        GameDecisionDialog(
            decision = GameDecision.Resign,
            opponentName = opponentName,
            onConfirm = {
                showHomeResignDialog = false
                viewModel.resign()
            },
            onDismiss = { showHomeResignDialog = false }
        )
    }

    if (incomingDrawOffer) {
        GameDecisionDialog(
            decision = GameDecision.IncomingDraw,
            opponentName = opponentName,
            onConfirm = viewModel::acceptDraw,
            onDismiss = viewModel::declineDraw
        )
    }

    state.promotionPending?.let {
        PromotionDialog(onPieceSelected = viewModel::onPromote)
    }
    state.gameResult?.let { result ->
        if (state.isRated) {
            RatedGameResultScreen(
                state = state,
                result = result,
                rematchBusy = rematchBusy,
                onClose = onHome,
                onShare = { shareGame(context, viewModel.pgn(), resultShareText(state, result)) },
                onStartAnalysis = onStartAnalysis,
                onRematch = {
                    if (rematchBusy) return@RatedGameResultScreen
                    rematchBusy = true
                    scope.launch {
                        viewModel.createRematch()
                            .onSuccess { invite ->
                                copyText(context, invite.link, "Rematch link copied — share it with $opponentName")
                                shareGame(
                                    context,
                                    invite.link,
                                    "Rematch on Caisaara with $opponentName: ${invite.link}"
                                )
                                onRematchStarted(invite.gameId, invite.link, opponentName)
                            }
                            .onFailure { error ->
                                rematchBusy = false
                                Toast.makeText(
                                    context,
                                    error.message ?: "Could not start a rematch with this opponent.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                },
                onNewOpponent = {
                    onNewOpponent(state.timeControlMinutes, state.incrementSeconds)
                },
                onSavePgn = { savePgn(context, viewModel.pgn()) },
                analysisViewModel = analysisViewModel
            )
        } else {
            GameResultDialog(
                result = result,
                onNewGame = { onNewOpponent(state.timeControlMinutes, state.incrementSeconds) },
                onHome = onHome,
                playerColor = state.myColor,
                playerRating = state.playerRating,
                ratingChange = state.ratingChange,
                isRated = state.isRated,
                whitePlayerName = state.whitePlayerName,
                blackPlayerName = state.blackPlayerName
            )
        }
    }
}

private fun resultShareText(state: GameUiState, result: GameResult): String {
    val outcome = when (result) {
        GameResult.DRAW -> "drew"
        GameResult.WHITE_WINS -> if (state.myColor == PieceColor.BLACK) "lost to" else "beat"
        GameResult.BLACK_WINS -> if (state.myColor == PieceColor.BLACK) "beat" else "lost to"
    }
    val opponent = when (state.myColor) {
        PieceColor.WHITE -> state.blackPlayerName
        PieceColor.BLACK -> state.whitePlayerName
        null -> "my opponent"
    }
    val rating = state.playerRating?.let { rating ->
        val change = state.ratingChange?.let { change ->
            if (change > 0) " (+$change)" else " ($change)"
        }.orEmpty()
        " Rating $rating$change."
    }.orEmpty()
    return "I just $outcome $opponent on Caisaara.$rating"
}

private fun savePgn(context: Context, pgn: String) {
    copyText(context, pgn, "PGN copied")
}

private fun copyText(context: Context, text: String, toast: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("caisaara", text))
    Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
}

private fun shareGame(context: Context, pgn: String, summary: String) {
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "$summary\n\n$pgn")
            },
            null
        )
    )
}

@Composable
private fun OnlineBoard(state: GameUiState, onSquareClick: (com.abhinav.caisarra.presentation.game.model.Square) -> Unit) {
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
        onSquareClick = onSquareClick
    )
}

@Composable
private fun OnlineActions(gameOngoing: Boolean, onOfferDraw: () -> Unit, onResign: () -> Unit) {
    GameBottomBar(
        canUndo = false,
        canRedo = false,
        optionsEnabled = gameOngoing,
        onUndo = {},
        onRedo = {},
        onOfferDraw = onOfferDraw,
        onResign = onResign,
        showDraw = true,
        showUndoRedo = false
    )
}

@Composable
private fun ChatOverlay(
    chat: List<ChatMessageDto>,
    myUserId: Long?,
    muted: Boolean,
    onToggleMute: () -> Unit,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    val heightFraction by animateFloatAsState(targetValue = if (imeVisible) 1f else 0.7f, label = "chatHeight")
    val noRipple = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Scrim)
            .clickable(interactionSource = noRipple, indication = null, onClick = onDismiss)
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.systemBars))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(heightFraction)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(SheetBg)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        var total = 0f
                        detectVerticalDragGestures(
                            onDragStart = { total = 0f },
                            onVerticalDrag = { _, delta -> total += delta },
                            onDragEnd = { if (total > 120f) onDismiss() }
                        )
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .background(HandleColor, RoundedCornerShape(2.dp))
                )
            }
            ChatPanel(
                messages = chat,
                myUserId = myUserId,
                muted = muted,
                onToggleMute = onToggleMute,
                onSend = onSend,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Banner(text: String, background: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 12.sp,
        modifier = modifier
            .background(background, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun OnlineTopBar(
    isRated: Boolean,
    unread: Int,
    muted: Boolean,
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "MOSAIC",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = if (isRated) "RATED" else "UNRATED",
                color = if (isRated) Accent else Color(0xFFA6C1D4),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        Row {
            TextButton(onClick = onFlip) {
                Text(text = "Flip", color = Color.White)
            }
            TextButton(onClick = onChat) {
                Text(
                    text = when {
                        muted -> "Chat (muted)"
                        unread > 0 -> "Chat ($unread)"
                        else -> "Chat"
                    },
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
            isDailyMode = state.isDailyMode,
            modifier = modifier
        )
    } else {
        ClockBar(
            playerName = state.blackPlayerName,
            timeMillis = state.blackTimeMillis,
            isActive = !state.isWhiteTurn,
            isDailyMode = state.isDailyMode,
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
            rating = state.playerRating,
            isDailyMode = state.isDailyMode,
            modifier = modifier
        )
    } else {
        ClockBar(
            playerName = state.whitePlayerName,
            timeMillis = state.whiteTimeMillis,
            isActive = state.isWhiteTurn,
            isDailyMode = state.isDailyMode,
            rating = state.playerRating,
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