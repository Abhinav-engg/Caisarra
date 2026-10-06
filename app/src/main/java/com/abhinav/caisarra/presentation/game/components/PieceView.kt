package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceType

@Composable
fun PieceView(
    piece: Piece,
    modifier: Modifier = Modifier
) {
    val symbol = when (piece.type) {
        PieceType.KING -> if (piece.color == PieceColor.WHITE) "♔" else "♚"

        PieceType.QUEEN -> if (piece.color == PieceColor.WHITE) "♕" else "♛"

        PieceType.ROOK -> if (piece.color == PieceColor.WHITE) "♖" else "♜"

        PieceType.BISHOP -> if (piece.color == PieceColor.WHITE) "♗" else "♝"

        PieceType.KNIGHT -> if (piece.color == PieceColor.WHITE) "♘" else "♞"

        PieceType.PAWN -> if (piece.color == PieceColor.WHITE) "♙" else "♟"
    }

    val textColor = if (piece.color == PieceColor.WHITE) {
            Color.White
        } else {
            Color(0xFF151515)
        }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = textColor,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )
    }
}