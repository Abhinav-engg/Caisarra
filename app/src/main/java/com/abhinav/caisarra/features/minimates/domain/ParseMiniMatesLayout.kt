package com.abhinav.caisarra.features.minimates.domain

import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceType
import com.abhinav.caisarra.presentation.game.model.Square

private val pieceTypes = mapOf(
    'k' to PieceType.KING, 'q' to PieceType.QUEEN, 'r' to PieceType.ROOK,
    'b' to PieceType.BISHOP, 'n' to PieceType.KNIGHT, 'p' to PieceType.PAWN
)

fun parseMiniMatesLayout(layout: String): MiniMatesPosition {
    val rows = layout.split("/")
    val pieces = buildMap {
        rows.forEachIndexed { row, line ->
            line.forEachIndexed { column, char ->
                pieceTypes[char.lowercaseChar()]?.let {
                    val color = if (char.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK
                    put(Square(row, column), Piece(it, color))
                }
            }
        }
    }
    return MiniMatesPosition(rows.size, pieces)
}
