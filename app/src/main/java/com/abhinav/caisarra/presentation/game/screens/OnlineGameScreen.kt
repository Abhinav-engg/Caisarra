package com.abhinav.caisarra.presentation.game.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.network.userMessage
import com.abhinav.caisarra.core.ui.security.SecureScreen
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.data.remote.dto.ChatMessageDto
import com.abhinav.caisarra.features.gameplay.presentation.GameActions
import com.abhinav.caisarra.features.gameplay.presentation.MoveNavigationBar
import com.abhinav.caisarra.features.gameplay.presentation.OnlineGameHeader
import com.abhinav.caisarra.features.gameplay.presentation.StatePlayerCard
import com.abhinav.caisarra.presentation.game.OnlineGameViewModel
import com.abhinav.caisarra.presentation.game.components.CapturedPieces
import com.abhinav.caisarra.presentation.game.components.ChatPanel
import com.abhinav.caisarra.presentation.game.components.GameOverReveal
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.components.ChessMoveSound
import com.abhinav.caisarra.presentation.game.components.GameDecision
import com.abhinav.caisarra.presentation.game.components.GameDecisionDialog
import com.abhinav.caisarra.presentation.game.components.GameResultDialog
import com.abhinav.caisarra.presentation.game.components.MoveList
import com.abhinav.caisarra.presentation.game.components.MoveTicker
import com.abhinav.caisarra.presentation.game.components.PromotionDialog
import com.abhinav.caisarra.presentation.game.components.rememberAutoQueenEnabled
import com.abhinav.caisarra.presentation.game.model.GameIntent
import com.abhinav.caisarra.presentation.game.model.GameResult
import com.abhinav.caisarra.presentation.game.model.GameUiState
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.viewmodel.GameAnalysisViewModel
import kotlinx.coroutines.launch
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

    SecureScreen()
    val autoQueen by rememberAutoQueenEnabled()

    LaunchedEffect(state.promotionPending, autoQueen) {
        if (state.promotionPending != null && autoQueen) {
            viewModel.onPromote(
                com.abhinav.caisarra.presentation.game.model.PieceType.QUEEN
            )
        }
    }
    val scope = rememberCoroutineScope()
    var rematchBusy by remember { mutableStateOf(false) }
    val waiting by viewModel.waitingForOpponent.collectAsState()
    val rematchDeclined by viewModel.rematchDeclined.collectAsState()
    val chat by viewModel.chat.collectAsState()
    val chatMuted by viewModel.chatMuted.collectAsState()
    val myUserId by viewModel.myUserId.collectAsState()
    val drawOfferedBy by viewModel.drawOfferedBy.collectAsState()
    val opponentName by viewModel.opponentName.collectAsState()
    val opponentAvatarUrl by viewModel.opponentAvatarUrl.collectAsState()
    val myAvatarUrl by viewModel.myAvatarUrl.collectAsState()
    val pingMs by viewModel.pingMs.collectAsState()
    val connected by viewModel.connected.collectAsState()

    var showChat by remember { mutableStateOf(false) }
    var showHomeResignDialog by remember { mutableStateOf(false) }
    var showWatchShareDialog by remember { mutableStateOf(false) }
    val requestHome = {
        if (state.gameResult == null && !waiting) {
            showHomeResignDialog = true
        } else {
            onHome()
        }
    }
    var seenCount by remember { mutableIntStateOf(0) }
    var showResultDialog by remember(state.gameResult != null) { mutableStateOf(state.gameResult != null) }

    LaunchedEffect(showChat, chatMuted, chat.size) {
        if (showChat || chatMuted) seenCount = chat.size
    }

    BackHandler(enabled = showChat || showResultDialog || state.gameResult != null) {
        if (showChat) {
            showChat = false
        } else if (showResultDialog) {
            showResultDialog = false
        } else {
            onHome()
        }
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
    if (rematchDeclined != null) {
        CenteredColumn {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFEF5350).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = rematchDeclined.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your opponent declined the rematch challenge.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onHome,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Text("HOME")
                }
                Button(
                    onClick = {
                        onNewOpponent(state.timeControlMinutes, state.incrementSeconds)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.Black)
                ) {
                    Text("NEW OPPONENT", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    if (waiting) {
        CenteredColumn {
            CircularProgressIndicator(color = Accent)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Waiting for opponent to join...",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.cancelWaitingChallenge(
                            onDone = onHome,
                            onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                        )
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Text("CANCEL")
                }
                TextButton(onClick = onHome) {
                    Text(text = "HOME", color = Accent)
                }
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
                maxWidth - 24.dp
            }

            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(boardSize)) {
                        OnlineBoard(state, viewModel::onSquareClick, moveAnimationConsumedId = state.moveAnimationConsumedId,onMoveAnimationStarted = viewModel::onMoveAnimationStarted)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        OnlineGameHeader(state, pingMs, connected, onBack = requestHome)

                        state.errorMessage?.let {
                            Spacer(Modifier.height(4.dp))
                            Banner(it, BannerBg)
                        }

                        if (drawOfferSent) {
                            Spacer(Modifier.height(4.dp))
                            Banner(
                                "Draw offer sent. Waiting for your opponent...",
                                InfoBannerBg
                            )
                        }
                        StatePlayerCard(state, state.isBoardFlipped, myAvatarUrl = myAvatarUrl, opponentAvatarUrl = opponentAvatarUrl)
                        MoveList(
                            moves = state.moves,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                        StatePlayerCard(state, !state.isBoardFlipped, myAvatarUrl = myAvatarUrl, opponentAvatarUrl = opponentAvatarUrl)

                        Spacer(Modifier.height(8.dp))
                        GameActions(
                            gameOngoing = gameOngoing,
                            opponentName = opponentName,
                            chatUnread = unread,
                            chatMuted = chatMuted,
                            isBoardFlipped = state.isBoardFlipped,
                            spectatorCount = state.spectatorCount,
                            onResign = viewModel::resign,
                            onOfferDraw = viewModel::offerDraw,
                            onChat = { showChat = true },
                            onFlip = viewModel::flipBoard,
                            onSpectate = { showWatchShareDialog = true }
                        )
                        MoveNavigationBar(state.canUndo, state.canRedo, viewModel::undo, viewModel::redo, Modifier.padding(top = 8.dp))
                    }
                }
            }
            else {
                val portraitBoardSize = maxWidth - 24.dp

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
                        OnlineGameHeader(state, pingMs, connected, onBack = requestHome)

                        if (state.gameResult != null && !showResultDialog) {
                            GameOverPill(onClick = { showResultDialog = true })
                        }

                        state.errorMessage?.let { message ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Banner(
                                text = message,
                                background = BannerBg,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (drawOfferSent) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Banner(
                                text = "Draw offer sent. Waiting for your opponent...",
                                background = InfoBannerBg,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    StatePlayerCard(state, state.isBoardFlipped, Modifier.width(portraitBoardSize), myAvatarUrl, opponentAvatarUrl)

                    Box(modifier = Modifier.size(portraitBoardSize)) {
                        OnlineBoard(state, viewModel::onSquareClick, moveAnimationConsumedId = state.moveAnimationConsumedId,onMoveAnimationStarted = viewModel::onMoveAnimationStarted)
                    }

                    StatePlayerCard(state, !state.isBoardFlipped, Modifier.width(portraitBoardSize), myAvatarUrl, opponentAvatarUrl)
                    MoveTicker(moves = state.moves, modifier = Modifier.width(portraitBoardSize))
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
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    GameActions(
                        gameOngoing = gameOngoing,
                        opponentName = opponentName,
                        chatUnread = unread,
                        chatMuted = chatMuted,
                        isBoardFlipped = state.isBoardFlipped,
                        spectatorCount = state.spectatorCount,
                        onResign = viewModel::resign,
                        onOfferDraw = viewModel::offerDraw,
                        onChat = { showChat = true },
                        onFlip = viewModel::flipBoard,
                        onSpectate = { showWatchShareDialog = true },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                MoveNavigationBar(state.canUndo, state.canRedo, viewModel::undo, viewModel::redo)
                }
            }
        }
        if (showChat) {
            ChatOverlay(
                chat = chat,
                myUserId = myUserId,
                opponentUsername = opponentName,
                opponentAvatarUrl = opponentAvatarUrl,
                myAvatarUrl = myAvatarUrl,
                muted = chatMuted,
                onToggleMute = viewModel::toggleChatMute,
                onSend = viewModel::sendChat,
                onDismiss = { showChat = false }
            )
        }
    }
    if (showWatchShareDialog) {
        val watchUrl = state.watchLink ?: state.watchCode?.let { "https://caisaara.app/watch/$it" } ?: ""
        AlertDialog(
            onDismissRequest = { showWatchShareDialog = false },
            containerColor = Color(0xFF111A24),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color(0xFF22D3EE),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Spectators (${state.spectatorCount})",
                        fontFamily = com.abhinav.caisarra.core.ui.theme.JetBrainsMono,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Matchmade games allow public spectators by default. You can also share your watch link directly:",
                        fontFamily = com.abhinav.caisarra.core.ui.theme.JetBrainsMono,
                        fontSize = 12.sp,
                        color = Color(0xFF82909E)
                    )
                    if (watchUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF16212D), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF243345), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = watchUrl,
                                fontFamily = com.abhinav.caisarra.core.ui.theme.JetBrainsMono,
                                fontSize = 12.sp,
                                color = Color(0xFF22D3EE),
                                maxLines = 2
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (watchUrl.isNotBlank()) {
                    TextButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Mosaic Watch Link", watchUrl))
                        Toast.makeText(context, "Watch link copied!", Toast.LENGTH_SHORT).show()
                    }) {
                        Text(
                            text = "COPY LINK",
                            fontFamily = com.abhinav.caisarra.core.ui.theme.JetBrainsMono,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Color(0xFF22D3EE)
                        )
                    }
                }
            },
            dismissButton = {
                Row {
                    if (watchUrl.isNotBlank()) {
                        TextButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Watch my live chess game on Mosaic: $watchUrl"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Watch Link"))
                        }) {
                            Text(
                                text = "SHARE",
                                fontFamily = com.abhinav.caisarra.core.ui.theme.JetBrainsMono,
                                color = Color.White
                            )
                        }
                    }
                    TextButton(onClick = { showWatchShareDialog = false }) {
                        Text(
                            text = "CLOSE",
                            fontFamily = com.abhinav.caisarra.core.ui.theme.JetBrainsMono,
                            color = Color(0xFF82909E)
                        )
                    }
                }
            }
        )
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

    if (!autoQueen) {
        state.promotionPending?.let {
            PromotionDialog(
                onPieceSelected = viewModel::onPromote
            )
        }
    }
    if (showResultDialog) {
        state.gameResult?.let { result ->
            if (state.isRated) {
                GameOverReveal {                 RatedGameResultScreen(
                    state = state,
                    result = result,
                    rematchBusy = rematchBusy,
                    onClose = { showResultDialog = false },
                    onShare = { shareGame(context, viewModel.pgn(), resultShareText(state, result)) },
                    onStartAnalysis = onStartAnalysis,
                    onRematch = {
                        if (rematchBusy) return@RatedGameResultScreen
                        rematchBusy = true
                        scope.launch {
                            viewModel.createRematch()
                                .onSuccess { invite ->
                                    val oppId = viewModel.getOpponentId()
                                    if (oppId != null && oppId > 0) {
                                        Toast.makeText(context, "Rematch challenge sent to $opponentName!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        copyText(context, invite.link, "Rematch link copied — share it with $opponentName")
                                        shareGame(
                                            context,
                                            invite.link,
                                            "Rematch on Mosaic with $opponentName: ${invite.link}"
                                        )
                                    }
                                    onRematchStarted(invite.gameId, invite.link, opponentName)
                                }
                                .onFailure { error ->
                                    rematchBusy = false
                                    Toast.makeText(
                                        context,
                                        error.userMessage("Could not start a rematch with this opponent."),
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
                ) }
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
                    blackPlayerName = state.blackPlayerName,
                    onDismiss = { showResultDialog = false }
                )
            }
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
    return "I just $outcome $opponent on Mosaic.$rating"
}

private fun savePgn(context: Context, pgn: String) {
    copyText(context, pgn, "PGN copied")
}

private fun copyText(context: Context, text: String, toast: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("mosaic", text))
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
private fun OnlineBoard(state: GameUiState, onSquareClick: (com.abhinav.caisarra.presentation.game.model.Square) -> Unit, moveAnimationConsumedId: Long?, onMoveAnimationStarted: (Long) -> Unit) {
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
        onSquareClick = onSquareClick
    )
}

@Composable
private fun ChatOverlay(
    chat: List<ChatMessageDto>,
    myUserId: Long?,
    opponentUsername: String? = null,
    opponentAvatarUrl: String? = null,
    myAvatarUrl: String? = null,
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
                opponentUsername = opponentUsername,
                opponentAvatarUrl = opponentAvatarUrl,
                myAvatarUrl = myAvatarUrl,
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
