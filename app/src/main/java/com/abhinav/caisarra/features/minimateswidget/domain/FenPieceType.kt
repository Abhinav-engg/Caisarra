package com.abhinav.caisarra.features.minimateswidget.domain

import com.abhinav.caisarra.presentation.game.model.PieceType

fun fenPieceType(piece: Char): PieceType = when (piece.lowercaseChar()) {
    'k' -> PieceType.KING
    'q' -> PieceType.QUEEN
    'r' -> PieceType.ROOK
    'b' -> PieceType.BISHOP
    'n' -> PieceType.KNIGHT
    else -> PieceType.PAWN
}
