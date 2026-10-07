package com.abhinav.caisarra.domain.chess.engine

import com.abhinav.caisarra.domain.chess.model.BoardPiece
import com.abhinav.caisarra.domain.chess.model.GameStatus
import com.abhinav.caisarra.domain.chess.model.Move
import com.abhinav.caisarra.domain.chess.model.PieceColor
import com.abhinav.caisarra.domain.chess.model.Square
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece as LibPiece
import com.github.bhlangonijr.chesslib.PieceType as LibPieceType
import com.github.bhlangonijr.chesslib.Square as LibSquare

class ChesslibEngine : ChessEngine {

    private var board = Board()

    override fun pieces(): List<BoardPiece> = LibSquare.values().mapNotNull { sq ->
        if (sq == LibSquare.NONE) return@mapNotNull null
        val piece = board.getPiece(sq)
        if (piece == LibPiece.NONE) null else piece.toBoardPiece(sq.toSquare())
    }

    override fun turn(): PieceColor = board.sideToMove.toColor()

    override fun legalMovesFrom(square: Square): List<Move> {
        val from = square.toLib()
        return board.legalMoves().filter { it.from == from }.map { it.toMove() }
    }

    override fun isPromotion(from: Square, to: Square) =
        legalMovesFrom(from).any { it.to == to && it.promotion != null }

    override fun capturedBy(move: Move): BoardPiece? {
        val target = board.getPiece(move.to.toLib())
        if (target != LibPiece.NONE) return target.toBoardPiece(move.to)
        val mover = board.getPiece(move.from.toLib())
        if (mover.pieceType == LibPieceType.PAWN && move.from.file != move.to.file) {
            val captured = Square(move.to.file, move.from.rank)
            return board.getPiece(captured.toLib()).toBoardPiece(captured)
        }
        return null
    }

    override fun play(move: Move): Boolean {
        val libMove = move.toLibMove(board.sideToMove)
        if (!board.legalMoves().contains(libMove)) return false
        board.doMove(libMove)
        return true
    }

    override fun undo(): Boolean = board.undoMove() != null

    override fun status(): GameStatus = when {
        board.isMated() -> GameStatus.Checkmate(turn().opposite())
        board.isStaleMate() -> GameStatus.Stalemate
        board.isInsufficientMaterial() -> GameStatus.DrawInsufficientMaterial
        board.isRepetition() -> GameStatus.DrawRepetition
        board.halfMoveCounter >= 100 -> GameStatus.DrawFiftyMove
        board.isKingAttacked() -> GameStatus.Check
        else -> GameStatus.Ongoing
    }

    override fun checkSquare(): Square? =
        if (board.isKingAttacked()) board.getKingSquare(board.sideToMove).toSquare() else null

    override fun fen(): String = board.fen

    override fun reset() {
        board = Board()
    }
}