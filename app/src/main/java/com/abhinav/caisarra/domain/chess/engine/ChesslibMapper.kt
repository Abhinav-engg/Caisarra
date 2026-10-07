package com.abhinav.caisarra.domain.chess.engine

import com.abhinav.caisarra.domain.chess.model.BoardPiece
import com.abhinav.caisarra.domain.chess.model.Move
import com.abhinav.caisarra.domain.chess.model.PieceColor
import com.abhinav.caisarra.domain.chess.model.PieceType
import com.abhinav.caisarra.domain.chess.model.Square
import com.github.bhlangonijr.chesslib.Piece as LibPiece
import com.github.bhlangonijr.chesslib.PieceType as LibPieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square as LibSquare
import com.github.bhlangonijr.chesslib.move.Move as LibMove

fun LibSquare.toSquare(): Square {
    val squareName = toString()
    return Square(file = squareName[0] - 'A', rank = squareName[1] - '1')
}

fun Square.toLib(): LibSquare = LibSquare.valueOf("${'A' + file}${rank + 1}")

fun Side.toColor() = if (this == Side.WHITE) PieceColor.White else PieceColor.Black

fun LibPieceType.toPieceType() = when (this) {
    LibPieceType.PAWN -> PieceType.Pawn
    LibPieceType.KNIGHT -> PieceType.Knight
    LibPieceType.BISHOP -> PieceType.Bishop
    LibPieceType.ROOK -> PieceType.Rook
    LibPieceType.QUEEN -> PieceType.Queen
    LibPieceType.KING -> PieceType.King
    else -> error("Unsupported piece type")
}

fun PieceType.toLib() = when (this) {
    PieceType.Pawn -> LibPieceType.PAWN
    PieceType.Knight -> LibPieceType.KNIGHT
    PieceType.Bishop -> LibPieceType.BISHOP
    PieceType.Rook -> LibPieceType.ROOK
    PieceType.Queen -> LibPieceType.QUEEN
    PieceType.King -> LibPieceType.KING
}

fun LibPiece.toBoardPiece(square: Square) = BoardPiece(pieceType.toPieceType(), pieceSide.toColor(), square)

fun LibMove.toMove() = Move(
    from = from.toSquare(),
    to = to.toSquare(),
    promotion = promotion.takeIf { it != LibPiece.NONE }?.pieceType?.toPieceType()
)

fun Move.toLibMove(side: Side) = LibMove(
    from.toLib(),
    to.toLib(),
    promotion?.let { LibPiece.make(side, it.toLib()) } ?: LibPiece.NONE
)