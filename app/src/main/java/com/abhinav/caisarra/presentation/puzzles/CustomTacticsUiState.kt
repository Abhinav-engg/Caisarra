package com.abhinav.caisarra.presentation.puzzles

import com.abhinav.caisarra.data.remote.dto.CustomPuzzleResponse
import com.abhinav.caisarra.data.remote.dto.PuzzleThemeDto
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceMoveAnimationEvent
import com.abhinav.caisarra.presentation.game.model.Square

data class CustomTacticsFilters(
    val selectedThemes: Set<String> = emptySet(),
    val minRating: Int = 800,
    val maxRating: Int = 2000,
    val excludeSolved: Boolean = true,
    val reviewMistakes: Boolean = false
)

sealed interface CustomTacticsUiState {

    data object Loading : CustomTacticsUiState

    data class Filters(
        val availableThemes: List<PuzzleThemeDto> = emptyList(),
        val filters: CustomTacticsFilters = CustomTacticsFilters(),
        val isLoadingThemes: Boolean = false,
        val isFetchingPuzzle: Boolean = false,
        val error: String? = null
    ) : CustomTacticsUiState

    data class Playing(
        val puzzle: CustomPuzzleResponse,
        val board: Map<Square, Piece>,
        val playerColor: PieceColor,
        val isBoardFlipped: Boolean,
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
        val message: String? = null,
        val elapsedTimeMs: Long = 0L
    ) : CustomTacticsUiState

    data class Result(
        val puzzle: CustomPuzzleResponse,
        val board: Map<Square, Piece>,
        val isBoardFlipped: Boolean,
        val correct: Boolean,
        val solved: Boolean,
        val attemptedMoves: List<String>,
        val solutionMoves: List<String>,
        val ratingChange: Int,
        val userRatingBefore: Int,
        val userRatingAfter: Int,
        val rated: Boolean
    ) : CustomTacticsUiState

    data class Error(
        val message: String
    ) : CustomTacticsUiState
}