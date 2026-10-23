package com.abhinav.caisarra

import com.abhinav.caisarra.domain.chess.model.PieceType as DomainPieceType
import com.abhinav.caisarra.domain.chess.model.pointValue as domainPointValue
import com.abhinav.caisarra.presentation.game.model.GameUiState
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceType
import com.abhinav.caisarra.presentation.game.model.Square
import com.abhinav.caisarra.presentation.game.model.pointValue
import org.junit.Assert.assertEquals
import org.junit.Test

class PiecePointsAndAdvantageTest {

    @Test
    fun testPiecePointValues() {
        assertEquals(1, PieceType.PAWN.pointValue)
        assertEquals(3, PieceType.KNIGHT.pointValue)
        assertEquals(3, PieceType.BISHOP.pointValue)
        assertEquals(5, PieceType.ROOK.pointValue)
        assertEquals(9, PieceType.QUEEN.pointValue)
        assertEquals(0, PieceType.KING.pointValue)

        assertEquals(1, DomainPieceType.Pawn.domainPointValue)
        assertEquals(3, DomainPieceType.Knight.domainPointValue)
        assertEquals(3, DomainPieceType.Bishop.domainPointValue)
        assertEquals(5, DomainPieceType.Rook.domainPointValue)
        assertEquals(9, DomainPieceType.Queen.domainPointValue)
        assertEquals(0, DomainPieceType.King.domainPointValue)
    }

    @Test
    fun testAdvantageFromCapturedPiecesWhenBoardEmpty() {
        val stateEven = GameUiState(
            board = emptyMap(),
            capturedWhitePieces = emptyList(),
            capturedBlackPieces = emptyList()
        )
        assertEquals(0, stateEven.whiteAdvantage)
        assertEquals(0, stateEven.blackAdvantage)

        // White has captured 1 black pawn (1 pt)
        val stateWhiteUp1 = GameUiState(
            board = emptyMap(),
            capturedWhitePieces = emptyList(),
            capturedBlackPieces = listOf(Piece(PieceType.PAWN, PieceColor.BLACK))
        )
        assertEquals(1, stateWhiteUp1.whiteAdvantage)
        assertEquals(0, stateWhiteUp1.blackAdvantage)

        // Black has captured 1 white queen (9 pts), White captured 1 black rook (5 pts) -> Black advantage = +4
        val stateBlackUp4 = GameUiState(
            board = emptyMap(),
            capturedWhitePieces = listOf(Piece(PieceType.QUEEN, PieceColor.WHITE)),
            capturedBlackPieces = listOf(Piece(PieceType.ROOK, PieceColor.BLACK))
        )
        assertEquals(0, stateBlackUp4.whiteAdvantage)
        assertEquals(4, stateBlackUp4.blackAdvantage)
    }

    @Test
    fun testAdvantageFromBoardMaterial() {
        // Initial setup has equal material (39 points each)
        val board = mutableMapOf<Square, Piece>()
        // White: 8 pawns, 2 knights, 2 bishops, 2 rooks, 1 queen, 1 king
        repeat(8) { file -> board[Square(file, 1)] = Piece(PieceType.PAWN, PieceColor.WHITE) }
        board[Square(1, 0)] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
        board[Square(6, 0)] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
        board[Square(2, 0)] = Piece(PieceType.BISHOP, PieceColor.WHITE)
        board[Square(5, 0)] = Piece(PieceType.BISHOP, PieceColor.WHITE)
        board[Square(0, 0)] = Piece(PieceType.ROOK, PieceColor.WHITE)
        board[Square(7, 0)] = Piece(PieceType.ROOK, PieceColor.WHITE)
        board[Square(3, 0)] = Piece(PieceType.QUEEN, PieceColor.WHITE)
        board[Square(4, 0)] = Piece(PieceType.KING, PieceColor.WHITE)

        // Black: same pieces
        repeat(8) { file -> board[Square(file, 6)] = Piece(PieceType.PAWN, PieceColor.BLACK) }
        board[Square(1, 7)] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
        board[Square(6, 7)] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
        board[Square(2, 7)] = Piece(PieceType.BISHOP, PieceColor.BLACK)
        board[Square(5, 7)] = Piece(PieceType.BISHOP, PieceColor.BLACK)
        board[Square(0, 7)] = Piece(PieceType.ROOK, PieceColor.BLACK)
        board[Square(7, 7)] = Piece(PieceType.ROOK, PieceColor.BLACK)
        board[Square(3, 7)] = Piece(PieceType.QUEEN, PieceColor.BLACK)
        board[Square(4, 7)] = Piece(PieceType.KING, PieceColor.BLACK)

        val stateStart = GameUiState(board = board)
        assertEquals(0, stateStart.whiteAdvantage)
        assertEquals(0, stateStart.blackAdvantage)

        // Remove a black queen -> White should have +9 advantage
        board.remove(Square(3, 7))
        val stateWhiteUpQueen = GameUiState(board = board)
        assertEquals(9, stateWhiteUpQueen.whiteAdvantage)
        assertEquals(0, stateWhiteUpQueen.blackAdvantage)

        // Also remove a white rook (5 pts) -> White should now have 9 - 5 = +4 advantage
        board.remove(Square(0, 0))
        val stateWhiteUp4 = GameUiState(board = board)
        assertEquals(4, stateWhiteUp4.whiteAdvantage)
        assertEquals(0, stateWhiteUp4.blackAdvantage)
    }
}
