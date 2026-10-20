package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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

private val MovesBackground = Color(0xFF151F2B)
private val MoveText = Color(0xFFE5E7EB)
private val MoveNumberText = Color(0xFF92928B)
private val LatestMoveBackground = Color(0xFF151F2B)

@Composable
fun MoveList(
    moves: List<MoveUi>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(moves.size) {
        if (moves.isNotEmpty()) {
            listState.animateScrollToItem(moves.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MovesBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#",
                modifier = Modifier.weight(0.16f),
                color = MoveNumberText,
                fontSize = 12.sp
            )

            Text(
                text = "WHITE",
                modifier = Modifier.weight(0.42f),
                color = MoveNumberText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "BLACK",
                modifier = Modifier.weight(0.42f),
                color = MoveNumberText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (moves.isEmpty()) {
            Text(
                text = "Moves will appear here",
                color = MoveNumberText,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(
                    items = moves,
                    key = { it.moveNumber }
                ) { move ->
                    val isLatestMove = move == moves.last()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isLatestMove) {
                                    LatestMoveBackground
                                } else {
                                    Color.Transparent
                                }
                            )
                            .padding(
                                horizontal = 4.dp,
                                vertical = 8.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${move.moveNumber}.",
                            modifier = Modifier.weight(0.16f),
                            color = MoveNumberText,
                            fontSize = 14.sp
                        )

                        Text(
                            text = move.whiteMove.orEmpty(),
                            modifier = Modifier.weight(0.42f),
                            color = MoveText,
                            fontSize = 15.sp,
                            fontWeight = if (
                                isLatestMove && move.blackMove == null
                            ) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            }
                        )

                        Text(
                            text = move.blackMove.orEmpty(),
                            modifier = Modifier.weight(0.42f),
                            color = MoveText,
                            fontSize = 15.sp,
                            fontWeight = if (
                                isLatestMove && move.blackMove != null
                            ) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            }
                        )
                    }
                }
            }
        }
    }
}