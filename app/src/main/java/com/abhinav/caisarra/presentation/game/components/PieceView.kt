package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.abhinav.caisarra.R
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceType

@Composable
fun PieceView(
    piece: Piece,
    modifier: Modifier = Modifier
) {

    val pieceResource = when (piece.color) {

        PieceColor.WHITE -> {
            when (piece.type) {

                PieceType.KING ->
                    R.drawable.white_king

                PieceType.QUEEN ->
                    R.drawable.white_queen

                PieceType.ROOK ->
                    R.drawable.white_castle

                PieceType.BISHOP ->
                    R.drawable.white_bishop

                PieceType.KNIGHT ->
                    R.drawable.white_knight

                PieceType.PAWN ->
                    R.drawable.white_pawn
            }
        }

        PieceColor.BLACK -> {
            when (piece.type) {

                PieceType.KING ->
                    R.drawable.black_king

                PieceType.QUEEN ->
                    R.drawable.black_queen

                PieceType.ROOK ->
                    R.drawable.black_castle

                PieceType.BISHOP ->
                    R.drawable.black_bishop

                PieceType.KNIGHT ->
                    R.drawable.black_knight

                PieceType.PAWN ->
                    R.drawable.black_pawn
            }
        }
    }

    Image(
        painter = painterResource(
            id = pieceResource
        ),
        contentDescription = "${piece.color} ${piece.type}",
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}