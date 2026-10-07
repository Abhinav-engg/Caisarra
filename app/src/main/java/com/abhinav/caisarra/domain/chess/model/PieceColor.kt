package com.abhinav.caisarra.domain.chess.model

enum class PieceColor {
    White,
    Black;

    fun opposite() = if (this == White) Black else White
}