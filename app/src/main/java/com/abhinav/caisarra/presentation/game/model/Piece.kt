package com.abhinav.caisarra.presentation.game.model

enum class PieceColor {
    WHITE,
    BLACK
}

enum class PieceType {
    KING,
    QUEEN,
    ROOK,
    BISHOP,
    KNIGHT,
    PAWN
}
enum class GameResult {
    WHITE_WINS,
    BLACK_WINS,
    DRAW
}

data class Piece(
    val type: PieceType,
    val color: PieceColor
)