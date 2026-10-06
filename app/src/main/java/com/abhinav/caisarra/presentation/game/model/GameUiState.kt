package com.abhinav.caisarra.presentation.game.model

data class GameUiState(
    val board: Map<Square, Piece> = emptyMap(),

    val whitePlayerName: String = "White",
    val blackPlayerName: String = "Black",
    val whiteTimeMillis: Long = 10 * 60 * 1000L,
    val blackTimeMillis: Long = 10 * 60 * 1000L,
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
    val gameResult: GameResult? = null
)