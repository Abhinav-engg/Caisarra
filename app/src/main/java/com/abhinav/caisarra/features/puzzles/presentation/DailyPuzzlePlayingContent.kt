package com.abhinav.caisarra.features.puzzles.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.features.gameplay.presentation.MoveNavigationBar
import com.abhinav.caisarra.features.mascot.presentation.DailyPuzzleMascot
import com.abhinav.caisarra.presentation.game.components.ChessMoveSound
import com.abhinav.caisarra.presentation.game.components.PromotionDialog
import com.abhinav.caisarra.presentation.puzzles.DailyPuzzleUiState
import com.abhinav.caisarra.presentation.puzzles.DailyPuzzleViewModel

@Composable
fun DailyPuzzlePlayingContent(state: DailyPuzzleUiState.Playing, viewModel: DailyPuzzleViewModel, onBack: () -> Unit) {
    ChessMoveSound(board = state.board, moveEventId = state.moveAnimationEvent?.id, moveTo = state.moveAnimationEvent?.to)
    val reviewing = state.solutionRevealed && !state.isPlayingSolution
    PuzzleLayout(
        header = {
            CaisaaraTopBar(title = "DAILY PUZZLE", onBack = onBack, actions = { StreakBadge(state.streak) })
            DailyPuzzleMascot(state = state, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), mascotSize = 88.dp)
        },
        board = { DailyPuzzleBoard(state, viewModel::onMoveAnimationStarted, viewModel::onSquareClicked) },
        panel = { DailyPuzzlePanel(state, viewModel) },
        footer = {
            MoveNavigationBar(
                canGoBack = reviewing && state.solutionPly > 0,
                canGoForward = reviewing && state.solutionPly < state.solutionMoves.size,
                onBack = { viewModel.stepSolution(-1) },
                onForward = { viewModel.stepSolution(1) }
            )
        }
    )
    state.promotionPending?.let {
        PromotionDialog(onPieceSelected = viewModel::onPromotionPieceSelected)
    }
}
