package com.abhinav.caisarra.domain.chess.model

enum class PieceType(val symbol: String) {
    Pawn("p"),
    Knight("n"),
    Bishop("b"),
    Rook("r"),
    Queen("q"),
    King("k")
}

val PieceType.pointValue: Int
    get() = when (this) {
        PieceType.Pawn -> 1
        PieceType.Knight -> 3
        PieceType.Bishop -> 3
        PieceType.Rook -> 5
        PieceType.Queen -> 9
        PieceType.King -> 0
    }