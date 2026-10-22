package com.abhinav.caisarra

import com.abhinav.caisarra.domain.chess.engine.ChesslibEngine
import com.abhinav.caisarra.domain.chess.game.PassAndPlayGame
import com.abhinav.caisarra.domain.chess.game.mapper.toUiBoard
import com.abhinav.caisarra.domain.chess.model.Move
import com.abhinav.caisarra.domain.chess.model.PieceColor
import com.abhinav.caisarra.domain.chess.model.Square
import com.abhinav.caisarra.domain.chess.model.toMove
import com.abhinav.caisarra.presentation.game.model.GameUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameUndoRedoTest {

    @Test
    fun testPassAndPlayUndoRedo() {
        val game = PassAndPlayGame()
        assertFalse(game.canUndo)
        assertFalse(game.canRedo)

        // Move 1: e2e4 (from square file 4 rank 1 to file 4 rank 3)
        val move1 = Move(Square(4, 1), Square(4, 3))
        assertTrue(game.play(move1))
        assertTrue(game.canUndo)
        assertFalse(game.canRedo)
        assertEquals(PieceColor.Black, game.turn)

        // Move 2: e7e5 (from square file 4 rank 6 to file 4 rank 4)
        val move2 = Move(Square(4, 6), Square(4, 4))
        assertTrue(game.play(move2))
        assertEquals(2, game.moveList.size)
        assertEquals(PieceColor.White, game.turn)

        // Undo move 2
        assertTrue(game.undo())
        assertEquals(1, game.moveList.size)
        assertEquals(PieceColor.Black, game.turn)
        assertTrue(game.canUndo)
        assertTrue(game.canRedo)

        // Redo move 2
        assertTrue(game.redo())
        assertEquals(2, game.moveList.size)
        assertEquals(PieceColor.White, game.turn)
        assertFalse(game.canRedo)
    }

    @Test
    fun testReplayHistoryViewingWithoutModifyingLiveGame() {
        // Simulates rated game history viewing:
        // Moves: 1. e2e4 e7e5 2. g1f3 b8c6
        val moves = listOf("e2e4", "e7e5", "g1f3", "b8c6")

        // 1. Initial live position at ply 4
        val liveEngine = ChesslibEngine()
        moves.forEach { liveEngine.play(it.toMove()) }
        val liveBoard = liveEngine.pieces().toUiBoard()

        // 2. Undo/Back to ply 3 (after 2. Nf3)
        val ply3Engine = ChesslibEngine()
        moves.take(3).forEach { ply3Engine.play(it.toMove()) }
        val ply3Board = ply3Engine.pieces().toUiBoard()

        // 3. Undo/Back to ply 0 (initial board)
        val startEngine = ChesslibEngine()
        val startBoard = startEngine.pieces().toUiBoard()

        // Live engine is untouched
        assertEquals(liveBoard, liveEngine.pieces().toUiBoard())

        // Past positions differ from live position
        assertNotEquals(liveBoard, ply3Board)
        assertNotEquals(liveBoard, startBoard)

        // The move at ply 3 was Nf3 (White's knight on f3, which is column 5, row 5 in 0-indexed UI)
        assertNotNull(ply3Board.values.firstOrNull { it.type.name == "KNIGHT" && it.color.name == "WHITE" })
    }

    @Test
    fun testGameUiStateDefaultsForUndoRedo() {
        val state = GameUiState(
            isRated = true,
            canUndo = true,
            canRedo = false,
            undoEnabled = true
        )
        assertTrue(state.isRated)
        assertTrue(state.canUndo)
        assertFalse(state.canRedo)
        assertTrue(state.undoEnabled)
    }
}
