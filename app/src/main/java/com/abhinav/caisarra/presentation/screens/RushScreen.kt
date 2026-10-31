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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.presentation.common.singleClickable
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.components.ChessMoveSound
import com.abhinav.caisarra.presentation.game.components.PromotionDialog
import com.abhinav.caisarra.presentation.puzzles.RushUiState
import com.abhinav.caisarra.presentation.puzzles.RushViewModel
import com.abhinav.caisarra.presentation.puzzles.components.PuzzleTurnBanner
import com.abhinav.caisarra.presentation.puzzles.components.RushResultSheet
import com.abhinav.caisarra.presentation.puzzles.components.RushStrikesIndicator
import com.caisaara.ui.theme.GoldColor
import com.caisaara.ui.theme.SilverColor
import com.caisaara.ui.theme.SlateNormal
import com.caisaara.ui.theme.StatsBarSlate
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsPieceCircleDark
import com.caisaara.ui.theme.StatsPieceCircleDarkBorder
import com.caisaara.ui.theme.StatsRed
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeSubhead
import com.caisaara.ui.theme.ThemeSlateAccent
import com.caisaara.ui.theme.White
import androidx.compose.ui.platform.LocalConfiguration

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
                    .background(StatsBg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = StatsCyan)
            }
        }

        is RushUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StatsBg)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.message,
                        color = StatsRed,
                        fontSize = TextSizeSubhead
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
                    .background(StatsBg)
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    if (isLandscape) {
        RushLandscapeContent(state, viewModel, onQuit)
    } else {
        RushPortraitContent(state, viewModel, onQuit)
    }
}

@Composable
private fun RushLandscapeContent(
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
        modifier = Modifier.fillMaxSize().background(StatsBg).systemBarsPadding()
    ) {
        PuzzleTurnBanner(
            playerColor = state.playerColor,
            isCheckingMove = state.isAwaitingServer && !state.isSkipping
        )

        Row(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BoxWithConstraints(
                modifier = Modifier.weight(1.05f).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                val boardSize = minOf(maxWidth, maxHeight)
                Box(modifier = Modifier.size(boardSize).aspectRatio(1f)) {
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
                        onSquareClick = { square -> viewModel.onSquareClicked(square) }
                    )
                }
            }

            Column(
                modifier = Modifier.weight(0.95f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(32.dp).background(SilverColor, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            Icon(painter = painterResource(id = R.drawable.black_pawn), contentDescription = null, tint = SlateNormal, modifier = Modifier.size(22.dp))
                        }
                        Text("${state.score}", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMono)
                    }

                    val timerText = if (state.secondsRemaining != null) {
                        val mins = state.secondsRemaining / 60
                        val secs = state.secondsRemaining % 60
                        String.format("%d:%02d", mins, secs)
                    } else {
                        "${state.strikesLeft} LIVES"
                    }
                    val timerColor = if (state.secondsRemaining != null && state.secondsRemaining <= 30) StatsRed else White
                    Text(timerText, color = timerColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMono)
                }

                if (state.isAwaitingServer) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = StatsCyan,
                        trackColor = StatsBarSlate
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RushStrikesIndicator(strikes = state.strikes)
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Difficulty", color = ThemeSlateAccent, fontSize = TextSizeCaption, fontFamily = JetBrainsMono)
                        Text("${state.currentPuzzle.rating}", color = White, fontSize = TextSizeSubhead, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMono)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.singleClickable {
                            viewModel.quitRush()
                            onQuit()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Back",
                            tint = White,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Back",
                            color = White,
                            fontSize = 12.sp,
                            fontFamily = JetBrainsMono
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(StatsBarSlate, RoundedCornerShape(12.dp))
                            .border(1.dp, StatsPieceCircleDark, RoundedCornerShape(12.dp))
                            .singleClickable(
                                enabled = !state.isAwaitingServer && !state.isSkipping
                            ) {
                                viewModel.requestHint()
                            }
                            .padding(horizontal = 22.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Hint",
                                tint = GoldColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Hint",
                                color = White,
                                fontSize = TextSizeBadge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.singleClickable(
                            enabled = !state.isAwaitingServer && !state.isSkipping
                        ) {
                            viewModel.skipPuzzle()
                        }
                    ) {
                        if (state.isSkipping) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = StatsCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Skipping...",
                                color = StatsCyan,
                                fontSize = 12.sp,
                                fontFamily = JetBrainsMono
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Skip",
                                tint = if (state.isAwaitingServer) {
                                    StatsPieceCircleDarkBorder
                                } else {
                                    White
                                },
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = if (state.secondsRemaining != null) "Skip (-45s)" else "Skip",
                                color = if (state.isAwaitingServer) {
                                    StatsPieceCircleDarkBorder
                                } else {
                                    White
                                },
                                fontSize = 12.sp,
                                fontFamily = JetBrainsMono
                            )
                        }
                    }
                }
            }
        }
    }

    state.promotionPending?.let {
        PromotionDialog(onPieceSelected = { pieceType -> viewModel.onPromotionPieceSelected(pieceType) })
    }
}

@Composable
private fun RushPortraitContent(
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
            .background(StatsBg)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PuzzleTurnBanner(
            playerColor = state.playerColor,
            isCheckingMove = state.isAwaitingServer && !state.isSkipping
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(SilverColor, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.black_pawn),
                        contentDescription = null,
                        tint = SlateNormal,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "${state.score}",
                    color = White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_puzzle_piece),
                    contentDescription = null,
                    tint = ThemeSlateAccent,
                    modifier = Modifier.size(24.dp)
                )
                val timerText = if (state.secondsRemaining != null) {
                    val mins = state.secondsRemaining / 60
                    val secs = state.secondsRemaining % 60
                    String.format("%d:%02d", mins, secs)
                } else {
                    "${state.strikesLeft} LIVES"
                }
                val timerColor = if (state.secondsRemaining != null && state.secondsRemaining <= 30) StatsRed else White
                Text(
                    text = timerText,
                    color = timerColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .height(4.dp)
        ) {
            if (state.isAwaitingServer) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = StatsCyan,
                    trackColor = StatsBarSlate
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            val configuration = LocalConfiguration.current
            val isLandscape =
                configuration.screenWidthDp > configuration.screenHeightDp

            val landscapeBoardLimit =
                (configuration.screenHeightDp.dp - 180.dp)
                    .coerceAtLeast(180.dp)

            val boardSize = if (isLandscape) {
                minOf(maxWidth, landscapeBoardLimit)
            } else {
                maxWidth
            }
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RushStrikesIndicator(strikes = state.strikes)

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Difficulty",
                    color = ThemeSlateAccent,
                    fontSize = TextSizeCaption,
                    fontFamily = JetBrainsMono
                )
                Text(
                    text = "${state.currentPuzzle.rating}",
                    color = White,
                    fontSize = TextSizeSubhead,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.singleClickable {
                    viewModel.quitRush()
                    onQuit()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = White,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Back",
                    color = White,
                    fontSize = 12.sp,
                    fontFamily = JetBrainsMono
                )
            }

            Box(
                modifier = Modifier
                    .background(StatsBarSlate, RoundedCornerShape(12.dp))
                    .border(1.dp, StatsPieceCircleDark, RoundedCornerShape(12.dp))
                    .singleClickable(enabled = !state.isAwaitingServer && !state.isSkipping) {
                        viewModel.requestHint()
                    }
                    .padding(horizontal = 22.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Hint",
                        tint = GoldColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Hint",
                        color = White,
                        fontSize = TextSizeBadge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.singleClickable(enabled = !state.isAwaitingServer && !state.isSkipping) {
                    viewModel.skipPuzzle()
                }
            ) {
                if (state.isSkipping) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = StatsCyan,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Skipping...",
                        color = StatsCyan,
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMono
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Forward",
                        tint = if (state.isAwaitingServer) StatsPieceCircleDarkBorder else White,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = if (state.secondsRemaining != null) "Skip (-45s)" else "Skip",
                        color = if (state.isAwaitingServer) StatsPieceCircleDarkBorder else White,
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMono
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
