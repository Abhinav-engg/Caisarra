package com.abhinav.caisarra.features.puzzles.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.features.mascot.presentation.RushMascot
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.components.ChessMoveSound
import com.abhinav.caisarra.presentation.game.components.GameDecision
import com.abhinav.caisarra.presentation.game.components.GameDecisionDialog
import com.abhinav.caisarra.presentation.game.components.PromotionDialog
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.puzzles.RushUiState
import com.abhinav.caisarra.presentation.puzzles.RushViewModel
import com.abhinav.caisarra.presentation.puzzles.components.RushStrikesIndicator

@Composable
fun RushPlayingContent(state: RushUiState.Playing, viewModel: RushViewModel, onQuit: () -> Unit) {
    ChessMoveSound(board = state.board, moveEventId = state.moveAnimationEvent?.id, moveTo = state.moveAnimationEvent?.to)
    val survival = state.mode == "survival"
    var showEndRunDialog by rememberSaveable { mutableStateOf(false) }
    val endRun = { showEndRunDialog = true }
    BackHandler { showEndRunDialog = !showEndRunDialog }
    if (showEndRunDialog) {
        GameDecisionDialog(
            decision = GameDecision.EndRun,
            onConfirm = {
                showEndRunDialog = false
                viewModel.quitRush()
                onQuit()
            },
            onDismiss = { showEndRunDialog = false }
        )
    }
    PuzzleLayout(
        header = {
            CaisaaraTopBar(title = if (survival) "PUZZLE SURVIVAL" else "PUZZLE RUSH", onBack = endRun)
            PuzzleStatsCard(rushStats(state), Modifier.padding(horizontal = 12.dp))
        },
        board = {
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
                onMoveAnimationStarted = viewModel::onMoveAnimationStarted,
                moveFeedback = state.moveFeedback,
                onSquareClick = viewModel::onSquareClicked
            )
        },
        panel = {
            PuzzleInfoRow(
                title = "Puzzle ${state.score + state.strikes + 1}",
                subtitle = when {
                    state.isSkipping -> "Skipping…"
                    state.isAwaitingServer -> "Checking move…"
                    else -> "${if (state.playerColor == PieceColor.WHITE) "White" else "Black"} to move · ${state.currentPuzzle.rating}"
                }
            ) {
                RushStrikesIndicator(strikes = state.strikes, icon = if (survival) Icons.Outlined.FavoriteBorder else Icons.Outlined.Circle)
            }
            if (state.currentPuzzle.themes.isNotEmpty()) {
                PuzzleThemeChips(state.currentPuzzle.themes)
            }
            RushMascot(state = state, modifier = Modifier.fillMaxWidth(), mascotSize = 72.dp)
            RushActions(state, viewModel::requestHint, viewModel::skipPuzzle, endRun)
        }
    )
    state.promotionPending?.let {
        PromotionDialog(onPieceSelected = viewModel::onPromotionPieceSelected)
    }
}
