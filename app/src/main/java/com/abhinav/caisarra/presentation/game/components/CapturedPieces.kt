package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.game.model.Piece

@Composable
fun CapturedPieces(
    pieces: List<Piece>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        pieces.forEach { piece ->
            Text(
                text = when (piece.type.name) {
                    "KING" -> if (piece.color.name == "WHITE") "♔" else "♚"
                    "QUEEN" -> if (piece.color.name == "WHITE") "♕" else "♛"
                    "ROOK" -> if (piece.color.name == "WHITE") "♖" else "♜"
                    "BISHOP" -> if (piece.color.name == "WHITE") "♗" else "♝"
                    "KNIGHT" -> if (piece.color.name == "WHITE") "♘" else "♞"
                    else -> if (piece.color.name == "WHITE") "♙" else "♟"
                },
                color = Color.LightGray,
                fontSize = 18.sp
            )
        }
    }
}