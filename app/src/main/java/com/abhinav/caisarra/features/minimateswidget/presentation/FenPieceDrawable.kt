package com.abhinav.caisarra.features.minimateswidget.presentation

import com.abhinav.caisarra.features.minimateswidget.domain.fenPieceType
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceThemes

fun fenPieceDrawable(piece: Char): Int = PieceThemes.Classic.getPieceDrawable(
    if (piece.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK,
    fenPieceType(piece)
)
