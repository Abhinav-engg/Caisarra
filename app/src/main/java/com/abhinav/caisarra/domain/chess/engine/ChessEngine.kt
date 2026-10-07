package com.abhinav.caisarra.domain.chess.engine

import com.abhinav.caisarra.domain.chess.model.BoardPiece
import com.abhinav.caisarra.domain.chess.model.GameStatus
import com.abhinav.caisarra.domain.chess.model.Move
import com.abhinav.caisarra.domain.chess.model.PieceColor
import com.abhinav.caisarra.domain.chess.model.Square

interface ChessEngine {
    fun pieces(): List<BoardPiece>
    fun turn(): PieceColor
    fun legalMovesFrom(square: Square): List<Move>
    fun isPromotion(from: Square, to: Square): Boolean
    fun capturedBy(move: Move): BoardPiece?
    fun play(move: Move): Boolean
    fun undo(): Boolean
    fun status(): GameStatus
    fun checkSquare(): Square?
    fun fen(): String
    fun reset()
}