package com.abhinav.caisarra.features.themecustomization.domain

import com.abhinav.caisarra.features.minimateswidget.domain.fenPieceType
import com.abhinav.caisarra.features.minimateswidget.domain.fenRows
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.Square

object PreviewPosition {
    val board: Map<Square, Piece> = fenRows("r5k1/pppq1ppp/8/3N4/8/8/PPP2PPP/5K2").flatMapIndexed { row, rank ->
        rank.mapIndexedNotNull { column, char ->
            char?.let {
                Square(row, column) to Piece(
                    type = fenPieceType(it),
                    color = if (it.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK
                )
            }
        }
    }.toMap()
    val selected = Square(3, 3)
    val legalMoves = listOf(Square(1, 2), Square(1, 4), Square(2, 1), Square(2, 5), Square(4, 1), Square(4, 5), Square(5, 2), Square(5, 4))
    val lastMoveFrom = Square(0, 3)
    val lastMoveTo = Square(1, 3)
}
