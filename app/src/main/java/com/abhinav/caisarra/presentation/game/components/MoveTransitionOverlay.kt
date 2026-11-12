package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import com.abhinav.caisarra.presentation.game.model.PieceTheme
import com.abhinav.caisarra.presentation.game.model.Square
import kotlin.math.roundToInt

@Composable
fun MoveTransitionOverlay(
    transition: MoveTransition,
    progress: () -> Float,
    cellWidth: Dp,
    cellHeight: Dp,
    squareOffset: (Square) -> Offset,
    pieceTheme: PieceTheme?
) {
    val captured = transition.captured
    val capturedSquare = transition.capturedSquare
    if (captured != null && capturedSquare != null) {
        val origin = squareOffset(capturedSquare)
        Box(
            modifier = Modifier
                .offset { IntOffset(origin.x.roundToInt(), origin.y.roundToInt()) }
                .size(width = cellWidth, height = cellHeight)
                .graphicsLayer {
                    val p = progress()
                    val eased = p * p
                    alpha = 1f - eased
                    scaleX = 1f - 0.35f * eased
                    scaleY = scaleX
                },
            contentAlignment = Alignment.Center
        ) {
            PieceView(piece = captured, pieceTheme = pieceTheme)
        }
    }

    transition.flights.forEach { flight ->
        val start = squareOffset(flight.from)
        val end = squareOffset(flight.to)
        Box(
            modifier = Modifier
                .offset {
                    val p = progress()
                    IntOffset(
                        x = (start.x + (end.x - start.x) * p).roundToInt(),
                        y = (start.y + (end.y - start.y) * p).roundToInt()
                    )
                }
                .size(width = cellWidth, height = cellHeight),
            contentAlignment = Alignment.Center
        ) {
            PieceView(piece = flight.piece, pieceTheme = pieceTheme)
        }
    }
}
