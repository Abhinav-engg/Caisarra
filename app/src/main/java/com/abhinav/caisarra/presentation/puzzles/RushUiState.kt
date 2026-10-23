package com.abhinav.caisarra.presentation.puzzles

import com.abhinav.caisarra.data.remote.dto.RushPuzzleData
import com.abhinav.caisarra.data.remote.dto.RushSummaryDto
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceMoveAnimationEvent
import com.abhinav.caisarra.presentation.game.model.Square

sealed interface RushUiState {

    data object Loading : RushUiState

    data class Playing(
        val sessionId: String,
        val mode: String,
        val currentPuzzle: RushPuzzleData,
        val board: Map<Square, Piece>,
        val playerColor: PieceColor,
        val isBoardFlipped: Boolean,
        val score: Int = 0,
        val strikes: Int = 0,
        val strikesLeft: Int = 3,
        val secondsRemaining: Int? = null,
        val selectedSquare: Square? = null,
        val legalMoves: List<Square> = emptyList(),
        val lastMoveFrom: Square? = null,
        val lastMoveTo: Square? = null,
        val isWhiteInCheck: Boolean = false,
        val isBlackInCheck: Boolean = false,
        val moveAnimationEvent: PieceMoveAnimationEvent? = null,
        val moveAnimationConsumedId: Long? = null,
        val promotionPending: Pair<Square, Square>? = null,
        val isAwaitingServer: Boolean = false,
        val isCorrectFlash: Boolean = false,
        val isStrikeFlash: Boolean = false,
        val highlightedSquare: Square? = null
    ) : RushUiState

    data class GameOver(
        val summary: RushSummaryDto,
        val mode: String
    ) : RushUiState

    data class Error(
        val message: String
    ) : RushUiState
}
