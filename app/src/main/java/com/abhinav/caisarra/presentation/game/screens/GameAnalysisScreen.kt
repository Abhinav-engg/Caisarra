package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.features.mascot.presentation.AnalysisMoveMascot
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.viewmodel.AnalysisPhase
import com.abhinav.caisarra.presentation.game.viewmodel.GameAnalysisUiState
import com.abhinav.caisarra.presentation.game.viewmodel.GameAnalysisViewModel
import com.abhinav.caisarra.presentation.game.screens.components.analysis.AnalysisNavigationControls
import com.abhinav.caisarra.presentation.game.screens.components.analysis.AnalysisTabButton
import com.abhinav.caisarra.presentation.game.screens.components.analysis.CenterColumn
import com.abhinav.caisarra.presentation.game.screens.components.analysis.CurrentMoveCard
import com.abhinav.caisarra.presentation.game.screens.components.analysis.EvaluationBar
import com.abhinav.caisarra.presentation.game.screens.components.analysis.MovesListTab
import com.abhinav.caisarra.presentation.game.screens.components.analysis.SummaryReportTab

private val ScreenBg = Color(0xFF0B1218)
private val Card = Color(0xFF121A22)
private val Border = Color(0xFF2A3848)
private val TextMain = Color(0xFFF4F7FB)
private val Muted = Color(0xFF8FA0B3)
private val Cyan = Color(0xFF3AD6F0)
private val Win = Color(0xFF45E5AF)
private val Brilliant = Color(0xFF26D0CE)
private val Great = Color(0xFF5C9DFF)
private val Warn = Color(0xFFF2C94C)
private val Orange = Color(0xFFF2994A)
private val Miss = Color(0xFFE08A6B)
private val Loss = Color(0xFFF08D96)
private val Book = Color(0xFFD4A373)

@Composable
fun GameAnalysisScreen(
    viewModel: GameAnalysisViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.start() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, Border, CircleShape)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextMain,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.size(12.dp))
            Text(
                "Game Analysis",
                color = TextMain,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            if (state.phase == AnalysisPhase.READY) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, Border, CircleShape)
                        .clip(CircleShape)
                        .clickable(onClick = viewModel::flipBoard),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Flip Board",
                        tint = Cyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        when (state.phase) {
            AnalysisPhase.ANALYSING -> CenterColumn {
                CircularProgressIndicator(color = Cyan)
                Spacer(Modifier.height(16.dp))
                Text("Analysing your game...", color = Muted, fontSize = 14.sp)
            }

            AnalysisPhase.FAILED -> CenterColumn {
                Text(
                    state.errorMessage.orEmpty(),
                    color = Loss,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Cyan)
                        .clickable(onClick = viewModel::retry)
                        .padding(horizontal = 28.dp, vertical = 14.dp)
                ) {
                    Text("Retry", color = Color(0xFF082028), fontWeight = FontWeight.Bold)
                }
            }

            AnalysisPhase.READY -> AnalysisContent(state, viewModel)
        }
    }
}

@Composable
internal fun AnalysisContent(
    state: GameAnalysisUiState,
    viewModel: GameAnalysisViewModel
) {
    val summary = state.summary ?: return
    val stats = summary.summaryStats

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val totalAvailableWidth = maxWidth - 24.dp
        val barWidth = 18.dp
        val gap = 8.dp
        val isWide = maxWidth > maxHeight && maxWidth >= 600.dp
        val boardSize = if (isWide) {
            minOf(maxHeight - 24.dp, maxWidth * 0.55f - barWidth - gap)
        } else {
            minOf(totalAvailableWidth - barWidth - gap, 360.dp)
        }
        val currentEval = if (state.currentPly == 0) {
            0
        } else {
            state.moves.getOrNull(state.currentPly - 1)?.evalAfter ?: 0
        }

        val boardPanel: @Composable () -> Unit = {
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EvaluationBar(
                    evalCentipawns = currentEval,
                    isFlipped = state.isFlipped,
                    modifier = Modifier
                        .width(barWidth)
                        .height(boardSize)
                )

                Spacer(Modifier.width(gap))

                Box(
                    modifier = Modifier.size(boardSize),
                    contentAlignment = Alignment.Center
                ) {
                    ChessBoard(
                        board = state.board,
                        selectedSquare = null,
                        legalMoves = emptyList(),
                        lastMoveFrom = state.lastMoveFrom,
                        lastMoveTo = state.lastMoveTo,
                        isWhiteInCheck = state.isWhiteInCheck,
                        isBlackInCheck = state.isBlackInCheck,
                        isFlipped = state.isFlipped,
                        onSquareClick = {}
                    )
                }
            }
        }

        val detailsPanel: @Composable ColumnScope.() -> Unit = {
            CurrentMoveCard(
                currentPly = state.currentPly,
                moves = state.moves,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            )

            AnalysisMoveMascot(
                currentPly = state.currentPly,
                moves = state.moves,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            )

            AnalysisNavigationControls(
                currentPly = state.currentPly,
                totalPlies = state.totalPlies,
                isPlayingToImportantMove = state.isPlayingToImportantMove,
                onFirst = viewModel::firstMove,
                onPrev = viewModel::previousMove,
                onPlayImportant = viewModel::playToNextImportantMove,
                onNext = viewModel::nextMove,
                onLast = viewModel::lastMove,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .background(Card, RoundedCornerShape(12.dp))
                    .border(1.dp, Border, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AnalysisTabButton(
                    text = "MOVES (${state.moves.size})",
                    selected = state.activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    modifier = Modifier.weight(1f)
                )
                AnalysisTabButton(
                    text = "REPORT",
                    selected = state.activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (state.activeTab == 0) {
                MovesListTab(
                    moves = state.moves,
                    currentPly = state.currentPly,
                    onMoveClick = viewModel::jumpToPly,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                SummaryReportTab(
                    summary = summary,
                    stats = stats,
                    selectedMove = state.selectedMove,
                    onEvalPointClick = viewModel::jumpToPly,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }

        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                boardPanel()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    content = detailsPanel
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                boardPanel()
                detailsPanel()
            }
        }
    }
}

fun formatEval(centipawns: Int) = "%+.2f".format(centipawns / 100.0)

fun classificationColor(classification: String) = when (classification.lowercase()) {
    "brilliant" -> Brilliant
    "great" -> Great
    "best" -> Win
    "good" -> Cyan
    "book" -> Book
    "inaccuracy" -> Warn
    "mistake" -> Orange
    "miss" -> Miss
    "blunder" -> Loss
    else -> Muted
}

fun classificationBadge(classification: String) = when (classification.lowercase()) {
    "brilliant" -> "!! Brilliant"
    "great" -> "! Great Move"
    "best" -> "★ Best Move"
    "good" -> "✓ Good"
    "book" -> "📖 Book"
    "inaccuracy" -> "?! Inaccuracy"
    "mistake" -> "? Mistake"
    "miss" -> "✕ Miss"
    "blunder" -> "?? Blunder"
    else -> classification.replaceFirstChar { it.uppercase() }
}

fun classificationSymbol(classification: String) = when (classification.lowercase()) {
    "brilliant" -> "!!"
    "great" -> "!"
    "best" -> "★"
    "good" -> "✓"
    "book" -> "📖"
    "inaccuracy" -> "?!"
    "mistake" -> "?"
    "miss" -> "✕"
    "blunder" -> "??"
    else -> ""
}
