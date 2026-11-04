package com.abhinav.caisarra.presentation.game.screens.components.analysis

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.data.remote.dto.EvalPointDto

private val Card = Color(0xFF121A22)
private val Border = Color(0xFF2A3848)
private val Muted = Color(0xFF8FA0B3)
private val Cyan = Color(0xFF3AD6F0)

@Composable
fun EvalGraph(
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
@Composable
fun EvaluationBar(
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
