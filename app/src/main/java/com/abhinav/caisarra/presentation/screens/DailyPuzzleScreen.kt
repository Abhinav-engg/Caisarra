package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.abhinav.caisarra.presentation.puzzles.DailyPuzzleUiState
import com.abhinav.caisarra.presentation.puzzles.DailyPuzzleViewModel
import com.abhinav.caisarra.presentation.puzzles.components.AttemptsIndicator
import com.abhinav.caisarra.presentation.puzzles.components.PuzzleResultSheet
import com.abhinav.caisarra.presentation.puzzles.components.PuzzleTurnBanner
import com.caisaara.ui.theme.GoldColor
import com.caisaara.ui.theme.MenuFriendsGreen
import com.caisaara.ui.theme.PuzzleDarkCard
import com.caisaara.ui.theme.PuzzleItemBg
import com.caisaara.ui.theme.SilverColor
import com.caisaara.ui.theme.SlateNormal
import com.caisaara.ui.theme.StatsBarSlate
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsPieceCircleDark
import com.caisaara.ui.theme.StatsRed
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeSubhead
import com.caisaara.ui.theme.TextSizeTiny
import com.caisaara.ui.theme.ThemeSlateAccent
import com.caisaara.ui.theme.White
import androidx.compose.ui.platform.LocalConfiguration
@Composable
fun DailyPuzzleScreen(
    viewModel: DailyPuzzleViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    when (val uiState = state) {
        is DailyPuzzleUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StatsBg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = StatsCyan)
            }
        }

        is DailyPuzzleUiState.Error -> {
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
                    if (uiState.canRetry) {
                        GeneralButton(
                            text = "Retry",
                            onClick = { viewModel.loadDailyPuzzle() }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    GeneralButton(
                        text = "Go Back",
                        onClick = onBack
                    )
                }
            }
        }

        is DailyPuzzleUiState.Playing -> {
            DailyPuzzlePlayingContent(
                state = uiState,
                viewModel = viewModel,
                onBack = onBack
            )
        }

        is DailyPuzzleUiState.Solved -> {
            DailyPuzzleSolvedContent(
                state = uiState,
                onBack = onBack
            )
        }

        is DailyPuzzleUiState.Failed -> {
            DailyPuzzleFailedContent(
                state = uiState,
                viewModel = viewModel,
                onBack = onBack
            )
        }
    }
}

@Composable
private fun DailyPuzzlePlayingContent(
    state: DailyPuzzleUiState.Playing,
    viewModel: DailyPuzzleViewModel,
    onBack: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    if (isLandscape) {
        DailyPuzzleLandscapeContent(state, viewModel, onBack)
    } else {
        DailyPuzzlePortraitContent(state, viewModel, onBack)
    }
}

@Composable
private fun DailyPuzzleLandscapeContent(
    state: DailyPuzzleUiState.Playing,
    viewModel: DailyPuzzleViewModel,
    onBack: () -> Unit
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
            .systemBarsPadding()
    ) {
        PuzzleTurnBanner(
            playerColor = state.playerColor,
            isCheckingMove = state.isAwaitingServer
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                modifier = Modifier
                    .weight(0.95f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.size(32.dp).background(SilverColor, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(painter = painterResource(id = R.drawable.black_pawn), contentDescription = null, tint = SlateNormal, modifier = Modifier.size(22.dp))
                        }
                        Text("${state.streak}", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMono)
                    }
                    Text("DAILY", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMono)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AttemptsIndicator(attemptsLeft = state.attemptsLeft)
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Difficulty", color = ThemeSlateAccent, fontSize = TextSizeCaption, fontFamily = JetBrainsMono)
                        Text("${state.puzzle.rating}", color = White, fontSize = TextSizeSubhead, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMono)
                    }
                }

                state.message?.let {
                    Text(it, color = ThemeSlateAccent, fontSize = TextSizeBadge)
                }

                if (state.puzzle.themes.isNotEmpty()) {
                    Text("THEMES", color = StatsCyan, fontSize = TextSizeCaption, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.puzzle.themes.take(4).forEach { theme ->
                            Box(
                                modifier = Modifier.background(StatsBarSlate, RoundedCornerShape(8.dp)).padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Text(theme, color = ThemeSlateAccent, fontSize = TextSizeTiny)
                            }
                        }
                    }
                }

                if (state.solutionRevealed && state.solutionMoves.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PuzzleDarkCard),
                        border = BorderStroke(1.dp, StatsBarSlate)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                            Text("SOLUTION", color = StatsCyan, fontSize = TextSizeCaption, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMono)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = state.solutionMoves.mapIndexed { index, move -> "${if (index % 2 == 0) (index / 2 + 1).toString() + "." else ""}$move" }.joinToString(" "),
                                color = White,
                                fontSize = TextSizeTiny
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                GeneralButton(text = "Replay", onClick = { viewModel.replaySolution() }, modifier = Modifier.weight(1f))
                                GeneralButton(text = "Try Moves", onClick = { viewModel.resetPuzzleToStart() }, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(0.75f)
                            .singleClickable {
                                onBack()
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Back",
                            tint = White,
                            modifier = Modifier.size(28.dp)
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
                            .weight(1.15f)
                            .padding(horizontal = 4.dp)
                            .background(
                                color = if (state.hintAvailable) StatsBarSlate else PuzzleDarkCard,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (state.hintAvailable) StatsPieceCircleDark else StatsBarSlate,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .singleClickable(
                                enabled = state.hintAvailable && !state.isAwaitingServer
                            ) {
                                viewModel.requestHint()
                            }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Hint",
                                tint = if (state.hintAvailable) GoldColor else SlateNormal,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Hint",
                                color = if (state.hintAvailable) White else SlateNormal,
                                fontSize = TextSizeBadge,
                                fontWeight = FontWeight.Medium,
                                fontFamily = JetBrainsMono
                            )
                        }
                    }

                    if (state.canViewSolution) {
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .padding(start = 4.dp)
                                .background(StatsBarSlate, RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = StatsPieceCircleDark,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .singleClickable(enabled = !state.isAwaitingServer) {
                                    viewModel.requestSolution()
                                }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Solution",
                                    tint = White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Solution",
                                    color = White,
                                    fontSize = TextSizeBadge,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = JetBrainsMono
                                )
                            }
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
private fun DailyPuzzlePortraitContent(
    state: DailyPuzzleUiState.Playing,
    viewModel: DailyPuzzleViewModel,
    onBack: () -> Unit
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
            .systemBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PuzzleTurnBanner(
            playerColor = state.playerColor,
            isCheckingMove = state.isAwaitingServer
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
                    text = "${state.streak}",
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
                Text(
                    text = "DAILY",
                    color = White,
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
            AttemptsIndicator(attemptsLeft = state.attemptsLeft)

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Difficulty",
                    color = ThemeSlateAccent,
                    fontSize = TextSizeCaption,
                    fontFamily = JetBrainsMono
                )
                Text(
                    text = "${state.puzzle.rating}",
                    color = White,
                    fontSize = TextSizeSubhead,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono
                )
            }
        }

        state.message?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                color = ThemeSlateAccent,
                fontSize = TextSizeBadge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (state.puzzle.themes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.puzzle.themes.take(4).forEach { theme ->
                    Box(
                        modifier = Modifier
                            .background(StatsBarSlate, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = theme,
                            color = ThemeSlateAccent,
                            fontSize = TextSizeTiny
                        )
                    }
                }
            }
        }

        if (state.solutionRevealed && state.solutionMoves.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PuzzleDarkCard),
                border = BorderStroke(1.dp, StatsBarSlate)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOLUTION",
                            color = StatsCyan,
                            fontSize = TextSizeCaption,
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        if (state.isPlayingSolution) {
                            Text(
                                text = "Playing on board...",
                                color = MenuFriendsGreen,
                                fontSize = TextSizeTiny,
                                fontFamily = JetBrainsMono
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.solutionMoves.forEachIndexed { index, moveUci ->
                            val isPlayerMove = index % 2 == 0
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isPlayerMove) StatsBarSlate else PuzzleItemBg,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isPlayerMove) StatsPieceCircleDark else StatsBarSlate,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${index + 1}. $moveUci",
                                    color = if (isPlayerMove) White else ThemeSlateAccent,
                                    fontSize = TextSizeCaption,
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(StatsBarSlate, RoundedCornerShape(10.dp))
                                .border(1.dp, StatsPieceCircleDark, RoundedCornerShape(10.dp))
                                .singleClickable(enabled = !state.isPlayingSolution) {
                                    viewModel.replaySolution()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Replay",
                                    tint = StatsCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Replay",
                                    color = White,
                                    fontSize = TextSizeCaption,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(StatsBarSlate, RoundedCornerShape(10.dp))
                                .border(1.dp, StatsPieceCircleDark, RoundedCornerShape(10.dp))
                                .singleClickable(enabled = !state.isPlayingSolution) {
                                    viewModel.resetPuzzleToStart()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Try it",
                                    tint = GoldColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Try Moves",
                                    color = White,
                                    fontSize = TextSizeCaption,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.heightIn(min = 20.dp))

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
                    onBack()
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
                    .background(
                        color = if (state.hintAvailable) StatsBarSlate else PuzzleDarkCard,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (state.hintAvailable) StatsPieceCircleDark else StatsBarSlate,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .singleClickable(enabled = state.hintAvailable && !state.isAwaitingServer) {
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
                        tint = if (state.hintAvailable) GoldColor else SlateNormal,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Hint",
                        color = if (state.hintAvailable) White else SlateNormal,
                        fontSize = TextSizeBadge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (state.canViewSolution) {
                Box(
                    modifier = Modifier
                        .background(StatsBarSlate, RoundedCornerShape(12.dp))
                        .border(1.dp, StatsPieceCircleDark, RoundedCornerShape(12.dp))
                        .singleClickable(enabled = !state.isAwaitingServer) {
                            viewModel.requestSolution()
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Solution",
                            tint = StatsCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Solution",
                            color = White,
                            fontSize = TextSizeBadge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.size(32.dp))
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

@Composable
private fun DailyPuzzleSolvedContent(
    state: DailyPuzzleUiState.Solved,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .systemBarsPadding()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            val boardSize = minOf(maxWidth, maxHeight)
            Box(
                modifier = Modifier
                    .size(boardSize)
                    .aspectRatio(1f)
                    .align(Alignment.Center)
            ) {
                ChessBoard(
                    board = state.board,
                    selectedSquare = null,
                    legalMoves = emptyList(),
                    lastMoveFrom = null,
                    lastMoveTo = null,
                    isWhiteInCheck = false,
                    isBlackInCheck = false,
                    isFlipped = state.isBoardFlipped,
                    onSquareClick = {}
                )
            }
        }

        PuzzleResultSheet(
            isSolved = true,
            ratingChange = state.ratingChange,
            newRating = state.userRatingAfter,
            streak = state.streak,
            message = state.message,
            onClose = onBack
        )
    }
}

@Composable
private fun DailyPuzzleFailedContent(
    state: DailyPuzzleUiState.Failed,
    viewModel: DailyPuzzleViewModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .systemBarsPadding()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            val boardSize = minOf(maxWidth, maxHeight)
            Box(
                modifier = Modifier
                    .size(boardSize)
                    .aspectRatio(1f)
                    .align(Alignment.Center)
            ) {
                ChessBoard(
                    board = state.board,
                    selectedSquare = null,
                    legalMoves = emptyList(),
                    lastMoveFrom = null,
                    lastMoveTo = null,
                    isWhiteInCheck = false,
                    isBlackInCheck = false,
                    isFlipped = state.isBoardFlipped,
                    onSquareClick = {}
                )
            }
        }

        PuzzleResultSheet(
            isSolved = false,
            ratingChange = state.ratingChange,
            newRating = state.userRatingAfter,
            streak = state.streak,
            message = state.message,
            onViewSolution = { viewModel.requestSolution() },
            onClose = onBack
        )
    }
}
