package com.abhinav.caisarra.presentation.game.viewmodel

import androidx.lifecycle.ViewModel
import com.abhinav.caisarra.presentation.game.model.GameIntent
import com.abhinav.caisarra.presentation.game.model.GameResult
import com.abhinav.caisarra.presentation.game.model.GameUiState
import com.abhinav.caisarra.presentation.game.model.MoveUi
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceType
import com.abhinav.caisarra.presentation.game.model.Square
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeGameViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        GameUiState(
            board = fakeBoard(),
            whitePlayerName = "Player 1",
            blackPlayerName = "Player 2",
            whiteTimeMillis = 8 * 60 * 1000L,
            blackTimeMillis = 8 * 60 * 1000L,
            isWhiteTurn = true
        )
    )

    val state = _state.asStateFlow()

    fun onIntent(intent: GameIntent) {

        when (intent) {

            is GameIntent.SquareClicked -> {
                handleSquareClick(intent.square)
            }

            is GameIntent.Promote -> {
                promote(intent.pieceType)
            }

            GameIntent.FlipBoard -> {
                _state.update {
                    it.copy(
                        isBoardFlipped = !it.isBoardFlipped
                    )
                }
            }

            GameIntent.Resign -> {
                val result =
                    if (_state.value.isWhiteTurn) {
                        GameResult.BLACK_WINS
                    } else {
                        GameResult.WHITE_WINS
                    }

                _state.update {
                    it.copy(gameResult = result)
                }
            }

            GameIntent.OfferDraw -> {
                _state.update {
                    it.copy(
                        gameResult = GameResult.DRAW
                    )
                }
            }

            GameIntent.Undo -> {
            }

            GameIntent.DismissResult -> {
                _state.update {
                    it.copy(gameResult = null)
                }
            }
        }
    }

    private fun handleSquareClick(square: Square) {

        val current = _state.value
        val selected = current.selectedSquare

        if (selected == null) {

            val piece = current.board[square]

            if (piece != null) {

                val correctColor =
                    if (current.isWhiteTurn) {
                        PieceColor.WHITE
                    } else {
                        PieceColor.BLACK
                    }

                if (piece.color == correctColor) {

                    _state.update {
                        it.copy(
                            selectedSquare = square,
                            legalMoves = fakeLegalMoves(square)
                        )
                    }
                }
            }

            return
        }

        if (square == selected) {
            _state.update {
                it.copy(
                    selectedSquare = null,
                    legalMoves = emptyList()
                )
            }
            return
        }

        if (current.legalMoves.contains(square)) {
            makeFakeMove(
                from = selected,
                to = square
            )
        } else {

            val piece = current.board[square]

            if (
                piece != null &&
                piece.color ==
                if (current.isWhiteTurn) {
                    PieceColor.WHITE
                } else {
                    PieceColor.BLACK
                }
            ) {
                _state.update {
                    it.copy(
                        selectedSquare = square,
                        legalMoves = fakeLegalMoves(square)
                    )
                }
            }
        }
    }

    private fun makeFakeMove(
        from: Square,
        to: Square
    ) {
        val current = _state.value

        val movingPiece = current.board[from]
            ?: return

        val capturedPiece = current.board[to]

        val newBoard = current.board.toMutableMap()

        newBoard.remove(from)
        newBoard[to] = movingPiece

        val newMoves = current.moves.toMutableList()

        val moveNumber = newMoves.size + 1

        val move = MoveUi(
            moveNumber = moveNumber,
            whiteMove =
                if (movingPiece.color == PieceColor.WHITE) {
                    "Move"
                } else {
                    null
                },
            blackMove =
                if (movingPiece.color == PieceColor.BLACK) {
                    "Move"
                } else {
                    null
                }
        )

        newMoves.add(move)

        _state.update {

            val capturedWhite =
                if (
                    capturedPiece != null &&
                    capturedPiece.color == PieceColor.WHITE
                ) {
                    it.capturedWhitePieces +
                            capturedPiece
                } else {
                    it.capturedWhitePieces
                }

            val capturedBlack =
                if (
                    capturedPiece != null &&
                    capturedPiece.color == PieceColor.BLACK
                ) {
                    it.capturedBlackPieces +
                            capturedPiece
                } else {
                    it.capturedBlackPieces
                }

            it.copy(
                board = newBoard,
                selectedSquare = null,
                legalMoves = emptyList(),
                lastMoveFrom = from,
                lastMoveTo = to,
                isWhiteTurn = !it.isWhiteTurn,
                moves = newMoves,
                capturedWhitePieces = capturedWhite,
                capturedBlackPieces = capturedBlack
            )
        }
    }

    private fun promote(
        pieceType: PieceType
    ) {
        val square = _state.value.promotionPending ?: return
        val current = _state.value
        val pawn = current.board[square] ?: return
        val newBoard = current.board.toMutableMap()

        newBoard[square] = Piece(
                type = pieceType,
                color = pawn.color
            )

        _state.update {
            it.copy(
                board = newBoard,
                promotionPending = null
            )
        }
    }

    private fun fakeLegalMoves(
        square: Square
    ): List<Square> {

        val result = mutableListOf<Square>()

        val directions =
            listOf(
                -1 to 0,
                1 to 0,
                0 to -1,
                0 to 1
            )

        directions.forEach { (dr, dc) ->

            val row =
                square.row + dr

            val column =
                square.column + dc

            if (
                row in 0..7 &&
                column in 0..7
            ) {
                result.add(
                    Square(row, column)
                )
            }
        }

        return result
    }

    private fun fakeBoard(): Map<Square, Piece> {

        val board =
            mutableMapOf<Square, Piece>()


        board[Square(7, 0)] =
            Piece(PieceType.ROOK, PieceColor.WHITE)

        board[Square(7, 1)] =
            Piece(PieceType.KNIGHT, PieceColor.WHITE)

        board[Square(7, 2)] =
            Piece(PieceType.BISHOP, PieceColor.WHITE)

        board[Square(7, 3)] =
            Piece(PieceType.QUEEN, PieceColor.WHITE)

        board[Square(7, 4)] =
            Piece(PieceType.KING, PieceColor.WHITE)

        board[Square(7, 5)] =
            Piece(PieceType.BISHOP, PieceColor.WHITE)

        board[Square(7, 6)] =
            Piece(PieceType.KNIGHT, PieceColor.WHITE)

        board[Square(7, 7)] =
            Piece(PieceType.ROOK, PieceColor.WHITE)

        for (column in 0..7) {
            board[Square(6, column)] =
                Piece(
                    PieceType.PAWN,
                    PieceColor.WHITE
                )
        }


        board[Square(0, 0)] =
            Piece(PieceType.ROOK, PieceColor.BLACK)

        board[Square(0, 1)] =
            Piece(PieceType.KNIGHT, PieceColor.BLACK)

        board[Square(0, 2)] =
            Piece(PieceType.BISHOP, PieceColor.BLACK)

        board[Square(0, 3)] =
            Piece(PieceType.QUEEN, PieceColor.BLACK)

        board[Square(0, 4)] =
            Piece(PieceType.KING, PieceColor.BLACK)

        board[Square(0, 5)] =
            Piece(PieceType.BISHOP, PieceColor.BLACK)

        board[Square(0, 6)] =
            Piece(PieceType.KNIGHT, PieceColor.BLACK)

        board[Square(0, 7)] =
            Piece(PieceType.ROOK, PieceColor.BLACK)

        for (column in 0..7) {
            board[Square(1, column)] =
                Piece(
                    PieceType.PAWN,
                    PieceColor.BLACK
                )
        }

        return board
    }
}