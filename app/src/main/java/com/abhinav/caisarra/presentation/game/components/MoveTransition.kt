package com.abhinav.caisarra.presentation.game.components

import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceMoveAnimationEvent
import com.abhinav.caisarra.presentation.game.model.PieceType
import com.abhinav.caisarra.presentation.game.model.Square
import kotlin.math.abs

data class PieceFlight(
    val piece: Piece,
    val from: Square,
    val to: Square
)

data class MoveTransition(
    val flights: List<PieceFlight>,
    val captured: Piece?,
    val capturedSquare: Square?
) {
    val isEmpty: Boolean get() = flights.isEmpty() && captured == null
}

fun moveTransitionFor(
    event: PieceMoveAnimationEvent,
    boardBefore: Map<Square, Piece>,
    boardAfter: Map<Square, Piece>,
    isStandardBoard: Boolean,
    primaryAlreadyPlaced: Boolean
): MoveTransition {
    val piece = event.piece
    val flights = mutableListOf<PieceFlight>()
    if (!primaryAlreadyPlaced) flights += PieceFlight(piece, event.from, event.to)

    val isCastle = isStandardBoard &&
            piece.type == PieceType.KING &&
            event.from.row == event.to.row &&
            abs(event.to.column - event.from.column) == 2
    if (isCastle) {
        val kingSide = event.to.column > event.from.column
        val rookFrom = Square(event.from.row, if (kingSide) 7 else 0)
        val rookTo = Square(event.from.row, if (kingSide) event.to.column - 1 else event.to.column + 1)
        val rook = boardAfter[rookTo] ?: boardBefore[rookFrom]
        if (rook != null && rook.type == PieceType.ROOK) flights += PieceFlight(rook, rookFrom, rookTo)
    }

    if (primaryAlreadyPlaced) return MoveTransition(flights, null, null)

    val direct = boardBefore[event.to]?.takeIf { it.color != piece.color }
    val enPassantSquare = Square(event.from.row, event.to.column)
    val enPassant = if (
        direct == null &&
        piece.type == PieceType.PAWN &&
        event.from.column != event.to.column
    ) {
        boardBefore[enPassantSquare]?.takeIf { it.type == PieceType.PAWN && it.color != piece.color }
    } else {
        null
    }

    return when {
        direct != null -> MoveTransition(flights, direct, event.to)
        enPassant != null -> MoveTransition(flights, enPassant, enPassantSquare)
        else -> MoveTransition(flights, null, null)
    }
}
