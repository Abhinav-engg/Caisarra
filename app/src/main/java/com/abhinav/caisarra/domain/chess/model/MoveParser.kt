package com.abhinav.caisarra.domain.chess.model

fun String.toMove(): Move {
    val from = Square(this[0] - 'a', this[1] - '1')
    val to = Square(this[2] - 'a', this[3] - '1')
    val promotion = if (length > 4) PieceType.values().first { it.symbol == this[4].toString() } else null
    return Move(from, to, promotion)
}