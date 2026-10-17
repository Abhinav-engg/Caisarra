
package com.abhinav.caisarra.presentation.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceMoveAnimationEvent
import com.abhinav.caisarra.presentation.game.model.PieceType
import com.abhinav.caisarra.presentation.game.model.Square
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

private val LightSquare = Color(0xFFE8EDF3)
private val DarkSquare = Color(0xFF253449)

private val SelectedColor = Color(0xFF10B981)
private val LastMoveColor = Color(0xFFB8D879)

private val LegalMoveDot = Color(0xFF00D6A0)
private val LegalMoveRing = Color(0xFF00E5A0)
private val LegalMoveOverlay = Color(0x3300D6A0)

private val CheckColor = Color(0xFFF04452)

private const val MIN_MOVE_DURATION = 120
private const val MAX_MOVE_DURATION = 320
private const val MOVE_MILLIS_PER_SQUARE = 45f

@Composable
fun ChessBoard(
    board: Map<Square, Piece>,
    selectedSquare: Square?,
    legalMoves: List<Square>,
    lastMoveFrom: Square?,
    lastMoveTo: Square?,
    isWhiteInCheck: Boolean,
    isBlackInCheck: Boolean,
    isFlipped: Boolean,
    moveAnimationEvent: PieceMoveAnimationEvent? = null,
    onSquareClick: (Square) -> Unit
) {
    var movingPiece by remember {
        mutableStateOf<PieceMoveAnimationEvent?>(null)
    }

    var completedMoveId by remember {
        mutableStateOf<Long?>(null)
    }

    val movementProgress = remember(moveAnimationEvent?.id) {
        Animatable(0f)
    }

    LaunchedEffect(moveAnimationEvent?.id) {
        val event = moveAnimationEvent ?: return@LaunchedEffect
        if (completedMoveId == event.id) {
            return@LaunchedEffect
        }

        movingPiece = event
        movementProgress.snapTo(0f)

        val fileDistance = abs(event.to.column - event.from.column)
        val rankDistance = abs(event.to.row - event.from.row)
        val distance = sqrt(
            (fileDistance * fileDistance + rankDistance * rankDistance).toFloat()
        )

        val duration = (distance * MOVE_MILLIS_PER_SQUARE)
            .roundToInt()
            .coerceIn(MIN_MOVE_DURATION, MAX_MOVE_DURATION)

        movementProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = duration,
                easing = LinearEasing
            )
        )

        if (movingPiece?.id == event.id) {
            movingPiece = null
            completedMoveId = event.id
        }
    }

    val activeMove = movingPiece
        ?: moveAnimationEvent?.takeIf { it.id != completedMoveId }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        val density = LocalDensity.current

        val cellWidth = maxWidth / 8
        val cellHeight = maxHeight / 8

        val cellWidthPx = with(density) { cellWidth.toPx() }
        val cellHeightPx = with(density) { cellHeight.toPx() }

        Column(modifier = Modifier.fillMaxSize()) {
            repeat(8) { displayRow ->
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(8) { displayColumn ->
                        val actualRow = if (isFlipped) 7 - displayRow else displayRow
                        val actualColumn = if (isFlipped) 7 - displayColumn else displayColumn
                        val square = Square(
                            row = actualRow,
                            column = actualColumn
                        )
                        val piece = board[square]
                        val isDark = (actualRow + actualColumn) % 2 == 1
                        val isSelected = selectedSquare == square
                        val isLegalMove = square in legalMoves
                        val isLastMove =
                            lastMoveFrom == square || lastMoveTo == square

                        val isCheck = piece != null && piece.type == PieceType.KING && ((piece.color == PieceColor.WHITE && isWhiteInCheck) || (piece.color == PieceColor.BLACK && isBlackInCheck))
                        val baseColor = when {
                            isLastMove -> LastMoveColor
                            isDark -> DarkSquare
                            else -> LightSquare
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(baseColor)
                                .border(
                                    width = 0.5.dp,
                                    color = Color.Black.copy(alpha = 0.15f)
                                )
                                .clickable {
                                    onSquareClick(square)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCheck) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    CheckColor.copy(alpha = 0.85f),
                                                    CheckColor.copy(alpha = 0.35f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }

                            if (isSelected) {
                                AnimatedSelectionCircle(
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            val isMovingDestination = activeMove?.to == square
                            if (piece != null && !isMovingDestination) {
                                PieceView(piece = piece)
                            }

                            if (isLegalMove) {
                                if (piece == null) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .background(
                                                color = LegalMoveDot,
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = 1.5.dp,
                                                color = Color.White.copy(alpha = 0.85f),
                                                shape = CircleShape
                                            )
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize(0.82f)
                                            .background(
                                                color = LegalMoveOverlay,
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = 3.dp,
                                                color = LegalMoveRing,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        activeMove?.let { move ->
            val startRow = if (isFlipped) 7 - move.from.row else move.from.row
            val startColumn = if (isFlipped) 7 - move.from.column else move.from.column

            val endRow = if (isFlipped) 7 - move.to.row else move.to.row
            val endColumn = if (isFlipped) 7 - move.to.column else move.to.column

            val progress = movementProgress.value

            val x = (startColumn + (endColumn - startColumn) * progress)* cellWidthPx

            val y = (startRow + (endRow - startRow) * progress) *cellHeightPx

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = x.roundToInt(),
                            y = y.roundToInt()
                        )
                    }
                    .size(
                        width = cellWidth,
                        height = cellHeight
                    ),
                contentAlignment = Alignment.Center
            ) {
                PieceView(piece = move.piece)
            }
        }
    }
}

@Composable
fun AnimatedSelectionCircle(
    modifier: Modifier = Modifier,
    color: Color = SelectedColor
) {
    val progress = remember {
        Animatable(0f)
    }

    LaunchedEffect(Unit) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 180,
                easing = LinearEasing
            )
        )
    }

    Canvas(modifier = modifier) {
        val halfWidth = size.width / 2f
        val halfHeight = size.height / 2f
        val animatedHalfWidth = halfWidth * progress.value
        val animatedHalfHeight = halfHeight * progress.value

        drawRect(
            color = color,
            topLeft = Offset(
                x = halfWidth - animatedHalfWidth,
                y = halfHeight - animatedHalfHeight
            ),
            size = androidx.compose.ui.geometry.Size(
                width = animatedHalfWidth * 2f,
                height = animatedHalfHeight * 2f
            )
        )
    }
}
