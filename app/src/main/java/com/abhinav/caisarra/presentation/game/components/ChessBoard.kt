package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceType
import com.abhinav.caisarra.presentation.game.model.Square

private val LightSquare = Color(0xFFE2E8F0)
private val DarkSquare = Color(0xFF1E293B)

private val SelectedColor = Color(0xFF10B981)
private val LastMoveColor = Color(0xFFB9D65C)
private val LegalMoveColor = Color(0x8010B981)
private val CheckColor = Color(0xFFEF4444)

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
    onSquareClick: (Square) -> Unit
) {
    // Fills whatever square the caller gives it (GameScreen wraps it in a fixed-size Box).
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
                    val isLegalMove = legalMoves.contains(square)
                    val isLastMove = lastMoveFrom == square || lastMoveTo == square

                    // Only the KING of the side that is in check gets marked.
                    // (Before, every piece of that colour matched, so the whole side turned red.)
                    val isCheck = piece != null &&
                            piece.type == PieceType.KING &&
                            (
                                    (piece.color == PieceColor.WHITE && isWhiteInCheck) ||
                                            (piece.color == PieceColor.BLACK && isBlackInCheck)
                                    )

                    // Check is no longer part of this chain: it is drawn as a glow on top.
                    val baseColor = when {
                        isSelected -> SelectedColor
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
                            .clickable { onSquareClick(square) },
                        contentAlignment = Alignment.Center
                    ) {

                        if (isCheck) {
                            // Soft red radial glow behind the king; the piece itself stays untinted.
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                CheckColor.copy(alpha = 0.9f),
                                                CheckColor.copy(alpha = 0f)
                                            )
                                        )
                                    )
                            )
                        }

                        if (isLegalMove) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        LegalMoveColor,
                                        shape = CircleShape
                                    )
                            )
                        }

                        if (piece != null) {
                            PieceView(piece = piece)
                        }
                    }
                }
            }
        }
    }
}