package com.abhinav.caisarra.presentation.game.screens.components.analysis

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.data.remote.dto.MoveAnalysisDto
import com.abhinav.caisarra.presentation.game.screens.classificationBadge
import com.abhinav.caisarra.presentation.game.screens.classificationColor
import com.abhinav.caisarra.presentation.game.screens.formatEval

private val Card = Color(0xFF121A22)

private val Border = Color(0xFF2A3848)

private val TextMain = Color(0xFFF4F7FB)

private val Muted = Color(0xFF8FA0B3)

private val Cyan = Color(0xFF3AD6F0)

private val Win = Color(0xFF45E5AF)

private val Loss = Color(0xFFF08D96)

@Composable
fun CenterColumn(content: @Composable () -> Unit) {
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
internal fun AnalysisTabButton(
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
fun CurrentMoveCard(
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
