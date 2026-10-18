package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.game.model.MoveUi

@Composable
fun MoveTicker(
    moves: List<MoveUi>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(moves) {
        if (moves.isNotEmpty()) {
            listState.animateScrollToItem(moves.lastIndex)
        }
    }
    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(
                color = Color(0xFF151F2B),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (moves.isEmpty()) {
            item {
                Text(
                    text = "Moves",
                    color = Color(0xFF8C98A5),
                    fontSize = 12.sp
                )
            }
        }

        items(
            items = moves,
            key = { it.moveNumber }
        ) { move ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${move.moveNumber}.",
                    color = Color(0xFF8C98A5),
                    fontSize = 12.sp
                )
                Text(
                    text = move.whiteMove ?: "—",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                if (move.blackMove != null) {
                    Text(
                        text = move.blackMove,
                        color = Color(0xFFB8C5D4),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
