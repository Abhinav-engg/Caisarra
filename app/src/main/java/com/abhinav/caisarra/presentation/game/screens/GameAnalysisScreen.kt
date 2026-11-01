package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.data.remote.dto.AnalysisPhaseDto
import com.abhinav.caisarra.data.remote.dto.AnalysisSummaryStatsDto
import com.abhinav.caisarra.data.remote.dto.EvalPointDto
import com.abhinav.caisarra.data.remote.dto.MoveAnalysisDto
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.viewmodel.AnalysisPhase
import com.abhinav.caisarra.presentation.game.viewmodel.GameAnalysisUiState
import com.abhinav.caisarra.presentation.game.viewmodel.GameAnalysisViewModel
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono

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
private fun CenterColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        content()
    }
}

@Composable
private fun AnalysisContent(
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

@Composable
private fun AnalysisTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Cyan.copy(alpha = 0.2f) else Color.Transparent)
            .border(
                1.dp,
                if (selected) Cyan.copy(alpha = 0.6f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Cyan else Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = JetBrainsMono
        )
    }
}

@Composable
private fun CurrentMoveCard(
    currentPly: Int,
    moves: List<MoveAnalysisDto>,
    modifier: Modifier = Modifier
) {
    if (currentPly == 0 || moves.isEmpty()) {
        Box(
            modifier = modifier
                .background(Card, RoundedCornerShape(14.dp))
                .border(1.dp, Border, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Game Start", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Starting position • White to move", color = Muted, fontSize = 12.sp)
                }
                Text("0.00", color = Cyan, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMono)
            }
        }
        return
    }

    val move = moves.getOrNull(currentPly - 1) ?: return
    val color = classificationColor(move.classification)
    val label = "${(move.moveNumber + 1) / 2}${if (move.playerColor == "white") "." else "..."} ${move.playedMove}"

    Column(
        modifier = modifier
            .background(Card, RoundedCornerShape(14.dp))
            .border(1.dp, color.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = TextMain,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMono
            )
            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.16f))
                    .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = classificationBadge(move.classification),
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                formatEval(move.evalAfter),
                color = if (move.evalAfter >= 0) Win else Loss,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMono
            )
        }

        if (move.bestMove.isNotBlank() && !move.bestMove.equals(move.playedMove, ignoreCase = true)) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Best: ${move.bestMove}",
                color = Win,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = JetBrainsMono
            )
        }
    }
}

@Composable
private fun AnalysisNavigationControls(
    currentPly: Int,
    totalPlies: Int,
    isPlayingToImportantMove: Boolean,
    onFirst: () -> Unit,
    onPrev: () -> Unit,
    onPlayImportant: () -> Unit,
    onNext: () -> Unit,
    onLast: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onFirst,
            enabled = currentPly > 0,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = "First Move",
                tint = if (currentPly > 0) TextMain else Muted.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = onPrev,
            enabled = currentPly > 0,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Move",
                tint = if (currentPly > 0) TextMain else Muted.copy(alpha = 0.4f),
                modifier = Modifier.size(22.dp)
            )
        }

        Button(
            onClick = onPlayImportant,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlayingToImportantMove) Loss else Cyan
            ),
            shape = RoundedCornerShape(18.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            if (isPlayingToImportantMove) {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = "Stop",
                    tint = ScreenBg,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "STOP",
                    color = ScreenBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = JetBrainsMono
                )
            } else {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = "Important Move",
                    tint = ScreenBg,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "KEY MOVE",
                    color = ScreenBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = JetBrainsMono
                )
            }
        }

        IconButton(
            onClick = onNext,
            enabled = currentPly < totalPlies,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Move",
                tint = if (currentPly < totalPlies) TextMain else Muted.copy(alpha = 0.4f),
                modifier = Modifier.size(22.dp)
            )
        }

        IconButton(
            onClick = onLast,
            enabled = currentPly < totalPlies,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = "Last Move",
                tint = if (currentPly < totalPlies) TextMain else Muted.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun MovesListTab(
    moves: List<MoveAnalysisDto>,
    currentPly: Int,
    onMoveClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentPly) {
        if (currentPly > 0) {
            val rowIndex = (currentPly - 1) / 2
            listState.animateScrollToItem(rowIndex.coerceAtLeast(0))
        }
    }

    val movePairs: List<List<MoveAnalysisDto>> = remember(moves) {
        moves.distinctBy { it.moveNumber }.chunked(2)
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(items = movePairs, key = { it.first().moveNumber }) { pair: List<MoveAnalysisDto> ->
            val moveNumber = (pair.first().moveNumber + 1) / 2
            val whiteMove = pair.firstOrNull()
            val blackMove = pair.getOrNull(1)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Card, RoundedCornerShape(10.dp))
                    .border(1.dp, Border, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$moveNumber.",
                    color = Muted,
                    fontSize = 12.sp,
                    fontFamily = JetBrainsMono,
                    modifier = Modifier.width(36.dp)
                )

                if (whiteMove != null) {
                    MoveChip(
                        move = whiteMove,
                        isSelected = currentPly == whiteMove.moveNumber,
                        onClick = { onMoveClick(whiteMove.moveNumber) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }

                Spacer(Modifier.width(8.dp))

                if (blackMove != null) {
                    MoveChip(
                        move = blackMove,
                        isSelected = currentPly == blackMove.moveNumber,
                        onClick = { onMoveClick(blackMove.moveNumber) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MoveChip(
    move: MoveAnalysisDto,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = classificationColor(move.classification)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color.copy(alpha = 0.22f) else Color(0xFF18222C))
            .border(
                1.dp,
                if (isSelected) color else Border.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                move.playedMove,
                color = if (isSelected) TextMain else Color(0xFFD2D9DF),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontFamily = JetBrainsMono
            )

            Text(
                classificationSymbol(move.classification),
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMono
            )
        }
    }
}

@Composable
private fun SummaryReportTab(
    summary: com.abhinav.caisarra.data.remote.dto.AnalysisSummaryResponse,
    stats: AnalysisSummaryStatsDto,
    selectedMove: Int?,
    onEvalPointClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AccuracyCard("WHITE", stats.accuracyWhite, stats.ratingWhite, Modifier.weight(1f))
                AccuracyCard("BLACK", stats.accuracyBlack, stats.ratingBlack, Modifier.weight(1f))
            }
        }
        if (summary.phases.isNotEmpty()) {
            item { PhasesCard(summary.phases) }
        }
        item { CountsCard(stats) }
        item {
            EvalGraph(
                points = summary.evalHistory,
                selected = selectedMove,
                onPointClick = onEvalPointClick
            )
        }
    }
}

@Composable
private fun AccuracyCard(label: String, accuracy: Double, rating: Int, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(Card, RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("$label ACCURACY", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        Spacer(Modifier.height(4.dp))
        Text("%.1f%%".format(accuracy), color = Win, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text("Rating $rating", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun PhasesCard(phases: List<AnalysisPhaseDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Card, RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        phases.forEach { phase ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                PhaseScore(
                    accuracy = phase.accuracyWhite,
                    symbol = phase.symbolWhite,
                    color = classificationColor(phase.classificationWhite),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                Text(
                    phase.phase.replaceFirstChar { it.uppercase() },
                    color = Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                PhaseScore(
                    accuracy = phase.accuracyBlack,
                    symbol = phase.symbolBlack,
                    color = classificationColor(phase.classificationBlack),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun PhaseScore(accuracy: Double, symbol: String, color: Color, modifier: Modifier, textAlign: TextAlign) {
    Text(
        "%.1f%% %s".format(accuracy, symbol),
        color = color,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        textAlign = textAlign,
        modifier = modifier
    )
}

@Composable
private fun CountsCard(stats: AnalysisSummaryStatsDto) {
    val rows = listOf(
        Triple("Brilliant", stats.brilliantWhite to stats.brilliantBlack, Brilliant),
        Triple("Great", stats.greatWhite to stats.greatBlack, Great),
        Triple("Best", stats.bestWhite to stats.bestBlack, Win),
        Triple("Good", stats.goodWhite to stats.goodBlack, Cyan),
        Triple("Inaccuracies", stats.inaccuraciesWhite to stats.inaccuraciesBlack, Warn),
        Triple("Mistakes", stats.mistakesWhite to stats.mistakesBlack, Orange),
        Triple("Misses", stats.missesWhite to stats.missesBlack, Miss),
        Triple("Blunders", stats.blundersWhite to stats.blundersBlack, Loss)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Card, RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        rows.forEach { (label, counts, color) ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(counts.first.toString(), color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(2f))
                Text(counts.second.toString(), color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun EvalGraph(
    points: List<EvalPointDto>,
    selected: Int?,
    onPointClick: (Int) -> Unit = {}
) {
    if (points.size < 2) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Card, RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text("EVALUATION GRAPH", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val limit = 500
            val half = size.height / 2f
            val stepX = size.width / (points.size - 1)
            fun yFor(eval: Int) = half - (eval.coerceIn(-limit, limit).toFloat() / limit) * half

            drawLine(Border, Offset(0f, half), Offset(size.width, half), 1.dp.toPx())

            val path = Path()
            points.forEachIndexed { index, point ->
                val x = index * stepX
                if (index == 0) path.moveTo(x, yFor(point.evalWhite)) else path.lineTo(x, yFor(point.evalWhite))
            }
            drawPath(path, Cyan, style = Stroke(width = 2.dp.toPx()))

            val selectedIndex = points.indexOfFirst { it.moveNumber == selected }
            if (selectedIndex >= 0) {
                drawCircle(Color.White, 5.dp.toPx(), Offset(selectedIndex * stepX, yFor(points[selectedIndex].evalWhite)))
            }
        }
    }
}

private fun formatEval(centipawns: Int) = "%+.2f".format(centipawns / 100.0)

@Composable
private fun EvaluationBar(
    evalCentipawns: Int,
    isFlipped: Boolean,
    modifier: Modifier = Modifier
) {
    val targetWhiteFraction = remember(evalCentipawns) {
        val cp = evalCentipawns.toDouble()
        val prob = 1.0 / (1.0 + Math.pow(10.0, -cp / 400.0))
        prob.coerceIn(0.04, 0.96).toFloat()
    }

    val animatedWhiteFraction by animateFloatAsState(
        targetValue = targetWhiteFraction,
        animationSpec = tween(durationMillis = 350),
        label = "evalFraction"
    )

    val topFraction = if (isFlipped) animatedWhiteFraction else (1f - animatedWhiteFraction)
    val topColor = if (isFlipped) Color(0xFFF3F6F9) else Color(0xFF1E242B)
    val bottomColor = if (isFlipped) Color(0xFF1E242B) else Color(0xFFF3F6F9)

    val evalScoreText = remember(evalCentipawns) {
        val pawns = Math.abs(evalCentipawns) / 100.0
        if (pawns >= 100) {
            "M"
        } else if (pawns >= 10) {
            pawns.toInt().toString()
        } else {
            "%.1f".format(pawns)
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, Border, RoundedCornerShape(6.dp))
            .background(Color(0xFF121A22))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(topFraction.coerceAtLeast(0.01f))
                .background(topColor),
            contentAlignment = Alignment.TopCenter
        ) {
            val showOnTop = if (isFlipped) evalCentipawns >= 100 else evalCentipawns <= -100
            if (showOnTop) {
                Text(
                    text = evalScoreText,
                    color = if (isFlipped) Color(0xFF111820) else Color(0xFFE2E8F0),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight((1f - topFraction).coerceAtLeast(0.01f))
                .background(bottomColor),
            contentAlignment = Alignment.BottomCenter
        ) {
            val showOnBottom = if (isFlipped) evalCentipawns < 100 else evalCentipawns > -100
            if (showOnBottom) {
                Text(
                    text = evalScoreText,
                    color = if (isFlipped) Color(0xFFE2E8F0) else Color(0xFF111820),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

private fun classificationColor(classification: String) = when (classification.lowercase()) {
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

private fun classificationBadge(classification: String) = when (classification.lowercase()) {
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

private fun classificationSymbol(classification: String) = when (classification.lowercase()) {
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
