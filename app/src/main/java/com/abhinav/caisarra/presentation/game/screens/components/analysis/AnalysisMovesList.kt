package com.abhinav.caisarra.presentation.game.screens.components.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.data.remote.dto.MoveAnalysisDto
import com.abhinav.caisarra.presentation.game.screens.classificationColor
import com.abhinav.caisarra.presentation.game.screens.classificationSymbol

private val Card = Color(0xFF121A22)

private val Border = Color(0xFF2A3848)

private val TextMain = Color(0xFFF4F7FB)

private val Muted = Color(0xFF8FA0B3)

@Composable
fun MovesListTab(
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
fun MoveChip(
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
