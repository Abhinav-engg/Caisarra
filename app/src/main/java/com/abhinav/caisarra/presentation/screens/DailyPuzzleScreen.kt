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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.presentation.common.singleClick
import com.abhinav.caisarra.presentation.common.singleClickable
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.components.ChessMoveSound
import com.abhinav.caisarra.presentation.game.components.PromotionDialog
import com.abhinav.caisarra.presentation.puzzles.DailyPuzzleUiState
import com.abhinav.caisarra.presentation.puzzles.DailyPuzzleViewModel
import com.abhinav.caisarra.presentation.puzzles.components.AttemptsIndicator
import com.abhinav.caisarra.presentation.puzzles.components.PuzzleResultSheet

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
                    .background(Color(0xFF080D13)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF23CDE8))
            }
        }

        is DailyPuzzleUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF080D13))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.message,
                        color = Color(0xFFF87171),
                        fontSize = 15.sp
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
    ChessMoveSound(
        board = state.board,
        moveEventId = state.moveAnimationEvent?.id,
        moveTo = state.moveAnimationEvent?.to
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080D13))
            .systemBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = singleClick { onBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "DAILY PUZZLE",
                color = Color.White,
                fontFamily = JetBrainsMono,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "#${state.puzzle.rating}",
                        color = Color(0xFF23CDE8),
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ATTEMPTS: ",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                AttemptsIndicator(attemptsLeft = state.attemptsLeft)
            }

            Text(
                text = "STREAK: ${state.streak} DAYS",
                color = Color(0xFFFBBF24),
                fontSize = 11.sp,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            val boardSize = maxWidth
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

        state.message?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
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
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = theme,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (state.hintAvailable) Color(0xFF1E293B) else Color(0xFF0F172A),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (state.hintAvailable) Color(0xFF334155) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .singleClickable(enabled = state.hintAvailable && !state.isAwaitingServer) {
                        viewModel.requestHint()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Hint",
                        tint = if (state.hintAvailable) Color(0xFFFBBF24) else Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Hint",
                        color = if (state.hintAvailable) Color.White else Color(0xFF475569),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (state.canViewSolution) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF334155),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .singleClickable(enabled = !state.isAwaitingServer) {
                            viewModel.requestSolution()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Solution",
                            tint = Color(0xFF23CDE8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Solution",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
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
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
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
                            color = Color(0xFF23CDE8),
                            fontSize = 12.sp,
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        if (state.isPlayingSolution) {
                            Text(
                                text = "Playing on board...",
                                color = Color(0xFF34D399),
                                fontSize = 11.sp,
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
                                        color = if (isPlayerMove) Color(0xFF1E293B) else Color(0xFF131D2A),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isPlayerMove) Color(0xFF334155) else Color(0xFF1E293B),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${index + 1}. $moveUci",
                                    color = if (isPlayerMove) Color.White else Color(0xFF94A3B8),
                                    fontSize = 12.sp,
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
                                .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
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
                                    tint = Color(0xFF23CDE8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Replay",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
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
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Try Moves",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
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
            .background(Color(0xFF080D13))
            .systemBarsPadding()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            val boardSize = maxWidth
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
            .background(Color(0xFF080D13))
            .systemBarsPadding()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            val boardSize = maxWidth
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
