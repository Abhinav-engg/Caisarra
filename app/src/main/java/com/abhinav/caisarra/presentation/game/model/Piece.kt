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

val PieceType.pointValue: Int
    get() = when (this) {
        PieceType.PAWN -> 1
        PieceType.KNIGHT -> 3
        PieceType.BISHOP -> 3
        PieceType.ROOK -> 5
        PieceType.QUEEN -> 9
        PieceType.KING -> 0
    }

val Piece.pointValue: Int
    get() = type.pointValue