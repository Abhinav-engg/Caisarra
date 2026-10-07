package com.abhinav.caisarra.domain.chess.model

data class Move(val from: Square, val to: Square, val promotion: PieceType? = null) {
    val uci: String get() = from.name + to.name + (promotion?.symbol ?: "")
}