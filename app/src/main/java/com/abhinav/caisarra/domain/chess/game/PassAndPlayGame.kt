package com.abhinav.caisarra.domain.chess.game

import com.abhinav.caisarra.domain.chess.engine.ChessEngine
import com.abhinav.caisarra.domain.chess.engine.ChesslibEngine
import com.abhinav.caisarra.domain.chess.model.BoardPiece
import com.abhinav.caisarra.domain.chess.model.GameStatus
import com.abhinav.caisarra.domain.chess.model.Move
import com.abhinav.caisarra.domain.chess.model.PieceColor
import com.abhinav.caisarra.domain.chess.model.Square

class PassAndPlayGame(private val engine: ChessEngine = ChesslibEngine()) {

    private val moves = mutableListOf<Move>()
    private val captures = mutableListOf<BoardPiece?>()
    private var forcedStatus: GameStatus? = null

    val pieces: List<BoardPiece> get() = engine.pieces()
    val turn: PieceColor get() = engine.turn()
    val checkSquare: Square? get() = engine.checkSquare()
    val lastMove: Move? get() = moves.lastOrNull()
    val moveList: List<String> get() = moves.map { it.uci }
    val captured: List<BoardPiece> get() = captures.filterNotNull()
    val status: GameStatus get() = forcedStatus ?: engine.status()
    val fen: String get() = engine.fen()

    fun legalTargets(square: Square): List<Square> =
        engine.legalMovesFrom(square).map { it.to }.distinct()

    fun needsPromotion(from: Square, to: Square) = engine.isPromotion(from, to)

    fun play(move: Move): Boolean {
        if (status.isOver) return false
        val capturedPiece = engine.capturedBy(move)
        if (!engine.play(move)) return false
        moves += move
        captures += capturedPiece
        return true
    }

    fun undo(): Boolean {
        if (moves.isEmpty() || status.isOver) return false
        if (!engine.undo()) return false
        moves.removeAt(moves.lastIndex)
        captures.removeAt(captures.lastIndex)
        return true
    }

    fun resign() {
        forcedStatus = GameStatus.Resigned(turn.opposite())
    }

    fun agreeDraw() {
        forcedStatus = GameStatus.DrawAgreement
    }

    fun timeout(loser: PieceColor) {
        forcedStatus = GameStatus.Timeout(loser.opposite())
    }

    fun reset() {
        engine.reset()
        moves.clear()
        captures.clear()
        forcedStatus = null
    }
}