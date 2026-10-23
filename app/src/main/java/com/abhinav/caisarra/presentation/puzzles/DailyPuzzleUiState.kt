package com.abhinav.caisarra.presentation.puzzles

import com.abhinav.caisarra.data.remote.dto.DailyPuzzleResponse
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceMoveAnimationEvent
import com.abhinav.caisarra.presentation.game.model.Square

sealed interface DailyPuzzleUiState {

    data object Loading : DailyPuzzleUiState

    data class Playing(
        val puzzle: DailyPuzzleResponse,
        val board: Map<Square, Piece>,
        val playerColor: PieceColor,
        val isBoardFlipped: Boolean,
        val attemptsLeft: Int,
        val streak: Int,
        val playerRating: Int,
        val selectedSquare: Square? = null,
        val legalMoves: List<Square> = emptyList(),
        val lastMoveFrom: Square? = null,
        val lastMoveTo: Square? = null,
        val isWhiteInCheck: Boolean = false,
        val isBlackInCheck: Boolean = false,
        val highlightedSquare: Square? = null,
        val moveAnimationEvent: PieceMoveAnimationEvent? = null,
        val moveAnimationConsumedId: Long? = null,
        val promotionPending: Pair<Square, Square>? = null,
        val message: String? = null,
        val isAwaitingServer: Boolean = false,
        val solutionMoves: List<String> = emptyList(),
        val solutionRevealed: Boolean = false,
        val isPlayingSolution: Boolean = false,
        val hintAvailable: Boolean = true,
        val canViewSolution: Boolean = false
    ) : DailyPuzzleUiState

    data class Solved(
        val puzzle: DailyPuzzleResponse,
        val board: Map<Square, Piece>,
        val isBoardFlipped: Boolean,
        val ratingChange: Int,
        val userRatingAfter: Int,
        val streak: Int,
        val message: String
    ) : DailyPuzzleUiState

    data class Failed(
        val puzzle: DailyPuzzleResponse,
        val board: Map<Square, Piece>,
        val isBoardFlipped: Boolean,
        val ratingChange: Int,
        val userRatingAfter: Int,
        val streak: Int,
        val message: String,
        val solutionMoves: List<String> = emptyList()
    ) : DailyPuzzleUiState

    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : DailyPuzzleUiState
}
