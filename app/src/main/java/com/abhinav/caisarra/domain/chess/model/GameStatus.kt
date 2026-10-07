package com.abhinav.caisarra.domain.chess.model

sealed interface GameStatus {
    data object Ongoing : GameStatus
    data object Check : GameStatus
    data class Checkmate(val winner: PieceColor) : GameStatus
    data object Stalemate : GameStatus
    data object DrawFiftyMove : GameStatus
    data object DrawRepetition : GameStatus
    data object DrawInsufficientMaterial : GameStatus
    data object DrawAgreement : GameStatus
    data class Resigned(val winner: PieceColor) : GameStatus
    data class Timeout(val winner: PieceColor) : GameStatus

    val isOver: Boolean get() = this != Ongoing && this != Check
}