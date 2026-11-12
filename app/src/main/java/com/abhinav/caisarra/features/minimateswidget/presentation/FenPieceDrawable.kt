package com.abhinav.caisarra.features.minimateswidget.presentation

import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceThemes
import com.abhinav.caisarra.presentation.game.model.PieceType

fun fenPieceDrawable(piece: Char): Int = PieceThemes.Classic.getPieceDrawable(
    if (piece.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK,
    when (piece.lowercaseChar()) {
        'k' -> PieceType.KING
        'q' -> PieceType.QUEEN
        'r' -> PieceType.ROOK
        'b' -> PieceType.BISHOP
        'n' -> PieceType.KNIGHT
        else -> PieceType.PAWN
    }
)
