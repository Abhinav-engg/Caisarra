package com.abhinav.caisarra.features.minimates.presentation

import androidx.activity.compose.BackHandler
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
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCyan

@Composable
fun MiniMatesScreen(
    viewModel: MiniMatesViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val overview = state.overview
    when {
        state.loading -> Box(Modifier.fillMaxSize().background(StatsBg), Alignment.Center) { CircularProgressIndicator(color = StatsCyan) }
        state.error != null -> FullScreenError(message = state.error, onRetry = viewModel::retry, onBack = onBack)
        state.level != null -> {
            BackHandler { viewModel.closeLevel() }
            MiniMatesPlayContent(
                state = state,
                onSquareClick = viewModel::onSquareClick,
                onHint = { viewModel.hint() },
                onRetry = { viewModel.retry() },
                onNext = { viewModel.nextLevel() },
                onBack = { viewModel.closeLevel() }
            )
        }
        overview != null -> MiniMatesLevelList(overview = overview, onBack = onBack, onLevelClick = { viewModel.openLevel(it) })
    }
}
