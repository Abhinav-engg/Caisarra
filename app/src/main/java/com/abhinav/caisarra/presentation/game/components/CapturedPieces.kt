package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.R
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceType

@Composable
fun CapturedPieces(
    pieces: List<Piece>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {

        pieces.forEach { piece ->

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
                painter = painterResource(
                    id = resource
                ),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }
    }
}