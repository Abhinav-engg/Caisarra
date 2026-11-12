package com.abhinav.caisarra.features.minimates.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.abhinav.caisarra.core.ui.components.FullScreenError
import com.abhinav.caisarra.presentation.game.components.ChessMoveSound
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCyan

@Composable
fun MiniMatesScreen(
    viewModel: MiniMatesViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    ChessMoveSound(board = state.board, moveEventId = state.moveAnimation?.id, moveTo = state.moveAnimation?.to)
    BackHandler(enabled = state.showLevels && state.level != null) { viewModel.showLevels(false) }
    when {
        state.loading -> Box(Modifier.fillMaxSize().background(StatsBg), Alignment.Center) { CircularProgressIndicator(color = StatsCyan) }
        state.error != null -> FullScreenError(message = state.error, onRetry = viewModel::retry, onBack = onBack)
        else -> AnimatedContent(targetState = state, contentKey = { it.screenKey }, transitionSpec = { miniMatesTransition() }, label = "miniMates") { screen ->
            val overview = screen.overview
            if (screen.screenKey != 0) {
                MiniMatesPlayContent(
                    state = screen,
                    onSquareClick = viewModel::onSquareClick,
                    onMoveAnimationStarted = viewModel::onMoveAnimationStarted,
                    onHint = { viewModel.hint() },
                    onRetry = { viewModel.retry() },
                    onNext = { viewModel.nextLevel() },
                    onLevels = { viewModel.showLevels(true) },
                    onBack = onBack
                )
            } else if (overview != null) {
                MiniMatesLevelList(
                    overview = overview,
                    currentLevel = screen.level?.levelNumber,
                    onBack = { if (screen.level != null) viewModel.showLevels(false) else onBack() },
                    onLevelClick = { if (it == screen.level?.levelNumber && screen.status == MiniMatesStatus.PLAYING) viewModel.showLevels(false) else viewModel.openLevel(it) }
                )
            }
        }
    }
}
