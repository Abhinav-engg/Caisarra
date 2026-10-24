package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.R
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceType
import com.abhinav.caisarra.presentation.game.model.pointValue

@Composable
fun CapturedPieces(
    pieces: List<Piece>,
    modifier: Modifier = Modifier,
    advantage: Int = 0
) {
    val sortedPieces = remember(pieces) {
        pieces.sortedWith(
            compareBy<Piece> { it.type.pointValue }
                .thenBy { it.type.ordinal }
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        sortedPieces.forEach { piece ->
            val resource = when (piece.color) {
                PieceColor.WHITE -> {
                    when (piece.type) {
                        PieceType.KING -> R.drawable.white_king
                        PieceType.QUEEN -> R.drawable.white_queen
                        PieceType.ROOK -> R.drawable.white_castle
                        PieceType.BISHOP -> R.drawable.white_bishop
                        PieceType.KNIGHT -> R.drawable.white_knight
                        PieceType.PAWN -> R.drawable.white_pawn
                    }
                }

                PieceColor.BLACK -> {
                    when (piece.type) {
                        PieceType.KING -> R.drawable.black_king
                        PieceType.QUEEN -> R.drawable.black_queen
                        PieceType.ROOK -> R.drawable.black_castle
                        PieceType.BISHOP -> R.drawable.black_bishop
                        PieceType.KNIGHT -> R.drawable.black_knight
                        PieceType.PAWN -> R.drawable.black_pawn
                    }
                }
            }

            Image(
                painter = painterResource(id = resource),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                contentScale = ContentScale.Fit
            )
        }

        if (advantage > 0) {
            Text(
                text = "+$advantage",
                color = Color(0xFF9FB0C3),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}