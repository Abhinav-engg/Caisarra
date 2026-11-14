package com.abhinav.caisarra.features.puzzles.presentation

import androidx.compose.runtime.Composable
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.model.Square
import com.abhinav.caisarra.presentation.puzzles.DailyPuzzleUiState

@Composable
fun DailyPuzzleBoard(
    state: DailyPuzzleUiState.Playing,
    onMoveAnimationStarted: (Long) -> Unit,
    onSquareClick: (Square) -> Unit
) {
    ChessBoard(
        board = state.board,
        selectedSquare = state.selectedSquare ?: state.highlightedSquare,
        legalMoves = state.legalMoves,
        lastMoveFrom = state.lastMoveFrom,
        lastMoveTo = state.lastMoveTo,
        isWhiteInCheck = state.isWhiteInCheck,
        isBlackInCheck = state.isBlackInCheck,
        isFlipped = state.isBoardFlipped,
        moveAnimationEvent = state.moveAnimationEvent,
        moveAnimationConsumedId = state.moveAnimationConsumedId,
        onMoveAnimationStarted = onMoveAnimationStarted,
        moveFeedback = state.moveFeedback,
        onSquareClick = onSquareClick
    )
}
