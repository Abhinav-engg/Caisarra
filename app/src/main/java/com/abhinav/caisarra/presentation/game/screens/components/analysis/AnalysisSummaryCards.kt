package com.abhinav.caisarra.presentation.game.screens.components.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.data.remote.dto.AnalysisPhaseDto
import com.abhinav.caisarra.data.remote.dto.AnalysisSummaryStatsDto
import com.abhinav.caisarra.presentation.game.screens.classificationColor

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

@Composable
fun SummaryReportTab(
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
fun AccuracyCard(label: String, accuracy: Double, rating: Int, modifier: Modifier) {
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
fun PhasesCard(phases: List<AnalysisPhaseDto>) {
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
fun PhaseScore(accuracy: Double, symbol: String, color: Color, modifier: Modifier, textAlign: TextAlign) {
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
fun CountsCard(stats: AnalysisSummaryStatsDto) {
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
