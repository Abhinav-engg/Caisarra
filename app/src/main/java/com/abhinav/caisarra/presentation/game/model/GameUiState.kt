
package com.abhinav.caisarra.presentation.game.model

data class GameUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val board: Map<Square, Piece> = emptyMap(),

    val whitePlayerName: String = "White",
    val blackPlayerName: String = "Black",
    val myColor: PieceColor? = null,
    val playerRating: Int? = null,
    val ratingChange: Int? = null,
    val opponentRating: Int? = null,
    val whiteRating: Int? = null,
    val blackRating: Int? = null,
    val isRated: Boolean = false,
    val timeControlMinutes: Int = 10,
    val incrementSeconds: Int = 0,
    val hasClock: Boolean = true,

    val whiteTimeMillis: Long = 10 * 60 * 1000L,
    val blackTimeMillis: Long = 10 * 60 * 1000L,

    val isDailyMode: Boolean = false,
    val dailyDaysPerMove: Int = 0,
    val lastMoveAccepted: Boolean? = null,

    val isWhiteTurn: Boolean = true,
    val selectedSquare: Square? = null,
    val legalMoves: List<Square> = emptyList(),
    val lastMoveFrom: Square? = null,
    val lastMoveTo: Square? = null,

    val isWhiteInCheck: Boolean = false,
    val isBlackInCheck: Boolean = false,

    val capturedWhitePieces: List<Piece> = emptyList(),
    val capturedBlackPieces: List<Piece> = emptyList(),
    val moves: List<MoveUi> = emptyList(),

    val isBoardFlipped: Boolean = false,
    val undoEnabled: Boolean = true,
    val promotionPending: Square? = null,
    val gameResult: GameResult? = null,
    val redoEnabled: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val moveAnimationEvent: PieceMoveAnimationEvent? = null,
    val moveAnimationConsumedId: Long? = null
) {
    val whiteAdvantage: Int
        get() {
            if (board.isNotEmpty()) {
                val whiteScore = board.values.filter { it.color == PieceColor.WHITE }.sumOf { it.pointValue }
                val blackScore = board.values.filter { it.color == PieceColor.BLACK }.sumOf { it.pointValue }
                return (whiteScore - blackScore).coerceAtLeast(0)
            }
            val whiteCapturedScore = capturedBlackPieces.sumOf { it.pointValue }
            val blackCapturedScore = capturedWhitePieces.sumOf { it.pointValue }
            return (whiteCapturedScore - blackCapturedScore).coerceAtLeast(0)
        }

    val blackAdvantage: Int
        get() {
            if (board.isNotEmpty()) {
                val whiteScore = board.values.filter { it.color == PieceColor.WHITE }.sumOf { it.pointValue }
                val blackScore = board.values.filter { it.color == PieceColor.BLACK }.sumOf { it.pointValue }
                return (blackScore - whiteScore).coerceAtLeast(0)
            }
            val whiteCapturedScore = capturedBlackPieces.sumOf { it.pointValue }
            val blackCapturedScore = capturedWhitePieces.sumOf { it.pointValue }
            return (blackCapturedScore - whiteCapturedScore).coerceAtLeast(0)
        }
}
