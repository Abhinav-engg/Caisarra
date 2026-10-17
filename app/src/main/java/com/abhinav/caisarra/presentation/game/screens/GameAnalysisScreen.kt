package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.abhinav.caisarra.data.remote.dto.AnalysisSummaryStatsDto
import com.abhinav.caisarra.data.remote.dto.EvalPointDto
import com.abhinav.caisarra.data.remote.dto.MoveAnalysisDto
import com.abhinav.caisarra.presentation.game.viewmodel.AnalysisPhase
import com.abhinav.caisarra.presentation.game.viewmodel.GameAnalysisUiState
import com.abhinav.caisarra.presentation.game.viewmodel.GameAnalysisViewModel

private val ScreenBg = Color(0xFF0B1218)
private val Card = Color(0xFF121A22)
private val Border = Color(0xFF2A3848)
private val TextMain = Color(0xFFF4F7FB)
private val Muted = Color(0xFF8FA0B3)
private val Cyan = Color(0xFF3AD6F0)
private val Win = Color(0xFF45E5AF)
private val Warn = Color(0xFFF2C94C)
private val Orange = Color(0xFFF2994A)
private val Loss = Color(0xFFF08D96)

@Composable
fun GameAnalysisScreen(
    viewModel: GameAnalysisViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
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
            Text("Game Analysis", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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

            AnalysisPhase.READY -> AnalysisContent(state, viewModel::selectMove)
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
private fun AnalysisContent(state: GameAnalysisUiState, onMoveClick: (Int) -> Unit) {
    val summary = state.summary ?: return
    val stats = summary.summaryStats

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AccuracyCard("WHITE", stats.accuracyWhite, Modifier.weight(1f))
                AccuracyCard("BLACK", stats.accuracyBlack, Modifier.weight(1f))
            }
        }
        item { CountsCard(stats) }
        item { EvalGraph(summary.evalHistory, state.selectedMove) }
        items(state.moves, key = { it.moveNumber }) { move ->
            MoveRow(move, move.moveNumber == state.selectedMove) { onMoveClick(move.moveNumber) }
        }
    }
}

@Composable
private fun AccuracyCard(label: String, value: Double, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(Card, RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("$label ACCURACY", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        Spacer(Modifier.height(6.dp))
        Text("%.1f%%".format(value), color = Win, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CountsCard(stats: AnalysisSummaryStatsDto) {
    val rows = listOf(
        Triple("Best", stats.bestWhite to stats.bestBlack, Win),
        Triple("Good", stats.goodWhite to stats.goodBlack, Cyan),
        Triple("Inaccuracies", stats.inaccuraciesWhite to stats.inaccuraciesBlack, Warn),
        Triple("Mistakes", stats.mistakesWhite to stats.mistakesBlack, Orange),
        Triple("Blunders", stats.blundersWhite to stats.blundersBlack, Loss)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Card, RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { (label, counts, color) ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(counts.first.toString(), color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(label, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(2f))
                Text(counts.second.toString(), color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun EvalGraph(points: List<EvalPointDto>, selected: Int?) {
    if (points.size < 2) return
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(Card, RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(14.dp)
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

@Composable
private fun MoveRow(move: MoveAnalysisDto, expanded: Boolean, onClick: () -> Unit) {
    val color = classificationColor(move.classification)
    val label = "${(move.moveNumber + 1) / 2}${if (move.playerColor == "white") "." else "..."} ${move.playedMove}"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Card, RoundedCornerShape(14.dp))
            .border(1.dp, if (expanded) color.copy(alpha = 0.7f) else Border, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(
                move.classification.replaceFirstChar { it.uppercase() },
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Eval ${formatEval(move.evalBefore)} → ${formatEval(move.evalAfter)}  •  Loss ${move.centipawnLoss} cp",
                color = Muted,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(4.dp))
            Text("Best move: ${move.bestMove}", color = Muted, fontSize = 12.sp)
            if (move.pv.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("Line: ${move.pv.joinToString(" ")}", color = Muted, fontSize = 12.sp)
            }
        }
    }
}

private fun formatEval(centipawns: Int) = "%+.2f".format(centipawns / 100.0)

private fun classificationColor(classification: String) = when (classification.lowercase()) {
    "best", "brilliant" -> Win
    "good" -> Cyan
    "inaccuracy" -> Warn
    "mistake" -> Orange
    "blunder" -> Loss
    else -> Muted
}