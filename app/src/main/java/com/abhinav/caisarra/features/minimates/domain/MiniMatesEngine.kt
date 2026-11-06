package com.abhinav.caisarra.features.minimates.domain

import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceType
import com.abhinav.caisarra.presentation.game.model.Square

object MiniMatesEngine {
    private val straight = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
    private val diagonal = listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1)
    private val jumps = listOf(1 to 2, 2 to 1, -1 to 2, -2 to 1, 1 to -2, 2 to -1, -1 to -2, -2 to -1)

    private fun PieceColor.other() = if (this == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE

    private fun targets(position: MiniMatesPosition, from: Square, attacksOnly: Boolean = false): List<Square> {
        val piece = position.pieces[from] ?: return emptyList()
        val board = position.pieces
        fun inside(square: Square) = square.row in 0 until position.size && square.column in 0 until position.size
        fun open(square: Square) = inside(square) && board[square]?.color != piece.color
        fun step(dirs: List<Pair<Int, Int>>) = dirs.map { Square(from.row + it.first, from.column + it.second) }.filter(::open)
        fun slide(dirs: List<Pair<Int, Int>>) = dirs.flatMap { (dr, dc) ->
            val ray = generateSequence(Square(from.row + dr, from.column + dc)) { Square(it.row + dr, it.column + dc) }
                .takeWhile(::inside).toList()
            val stop = ray.indexOfFirst { board[it] != null }
            if (stop < 0) ray else ray.take(stop) + ray.subList(stop, stop + 1).filter(::open)
        }
        return when (piece.type) {
            PieceType.KING -> step(straight + diagonal)
            PieceType.KNIGHT -> step(jumps)
            PieceType.ROOK -> slide(straight)
            PieceType.BISHOP -> slide(diagonal)
            PieceType.QUEEN -> slide(straight + diagonal)
            PieceType.PAWN -> {
                val dir = if (piece.color == PieceColor.WHITE) -1 else 1
                val push = Square(from.row + dir, from.column).takeIf { !attacksOnly && inside(it) && board[it] == null }
                listOf(-1, 1).map { Square(from.row + dir, from.column + it) }
                    .filter { inside(it) && (attacksOnly || board[it]?.color == piece.color.other()) } + listOfNotNull(push)
            }
        }
    }

    fun play(position: MiniMatesPosition, move: MiniMatesMove): MiniMatesPosition {
        val piece = position.pieces.getValue(move.from)
        val promotes = piece.type == PieceType.PAWN && move.to.row in listOf(0, position.size - 1)
        val placed = if (promotes) piece.copy(type = PieceType.QUEEN) else piece
        return position.copy(pieces = position.pieces - move.from + (move.to to placed))
    }

    fun isInCheck(position: MiniMatesPosition, color: PieceColor): Boolean {
        val king = position.pieces.entries.firstOrNull { it.value == Piece(PieceType.KING, color) }?.key ?: return false
        return position.pieces.any { (square, piece) ->
            piece.color != color && king in targets(position, square, attacksOnly = true)
        }
    }

    fun legalMoves(position: MiniMatesPosition, color: PieceColor): List<MiniMatesMove> =
        position.pieces.filterValues { it.color == color }.keys.flatMap { from ->
            targets(position, from).map { MiniMatesMove(from, it) }
        }.filterNot { isInCheck(play(position, it), color) }

    fun isMate(position: MiniMatesPosition, color: PieceColor) =
        isInCheck(position, color) && legalMoves(position, color).isEmpty()

    fun canForceMate(position: MiniMatesPosition, movesLeft: Int): Boolean =
        legalMoves(position, PieceColor.WHITE).any { canForceMateAfter(play(position, it), movesLeft) }

    fun canForceMateAfter(position: MiniMatesPosition, movesLeft: Int): Boolean {
        if (isMate(position, PieceColor.BLACK)) return true
        val replies = legalMoves(position, PieceColor.BLACK)
        return movesLeft > 1 && replies.isNotEmpty() && replies.all { canForceMate(play(position, it), movesLeft - 1) }
    }

    fun bestDefense(position: MiniMatesPosition, movesLeft: Int): MiniMatesMove? {
        val replies = legalMoves(position, PieceColor.BLACK)
        return replies.firstOrNull { !canForceMate(play(position, it), movesLeft) } ?: replies.firstOrNull()
    }
}
