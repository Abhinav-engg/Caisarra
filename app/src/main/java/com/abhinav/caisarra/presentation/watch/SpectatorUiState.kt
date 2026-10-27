package com.abhinav.caisarra.presentation.watch

import com.abhinav.caisarra.presentation.game.model.MoveUi
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceMoveAnimationEvent
import com.abhinav.caisarra.presentation.game.model.Square

data class SpectatorUiState(
    val isLoading: Boolean = true,
    val isConnected: Boolean = false,
    val errorMessage: String? = null,
    val gameId: String = "",
    val watchCode: String = "",
    val whitePlayerName: String = "White",
    val blackPlayerName: String = "Black",
    val whiteRating: Int? = null,
    val blackRating: Int? = null,
    val whiteTimeMs: Long = 0L,
    val blackTimeMs: Long = 0L,
    val isWhiteTurn: Boolean = true,
    val isWhiteInCheck: Boolean = false,
    val isBlackInCheck: Boolean = false,
    val isFlipped: Boolean = false,
    val board: Map<Square, Piece> = emptyMap(),
    val moves: List<MoveUi> = emptyList(),
    val rawMoves: List<String> = emptyList(),
    val lastMoveFrom: Square? = null,
    val lastMoveTo: Square? = null,
    val moveAnimationEvent: PieceMoveAnimationEvent? = null,
    val moveAnimationConsumedId: Long? = null,
    val capturedWhitePieces: List<Piece> = emptyList(),
    val capturedBlackPieces: List<Piece> = emptyList(),
    val spectatorCount: Int = 1,
    val status: String = "active",
    val result: String? = null,
    val endReason: String? = null,
    val isWatchEnded: Boolean = false,
    val timeControlMinutes: Int = 10,
    val rated: Boolean = true
)
