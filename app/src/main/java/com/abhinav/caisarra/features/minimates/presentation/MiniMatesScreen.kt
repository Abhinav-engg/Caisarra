package com.abhinav.caisarra.features.minimates.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun MiniMatesScreen(
    viewModel: MiniMatesViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    if (state.level == null) return MiniMatesLevelList(onBack = onBack, onLevelClick = viewModel::openLevel)
    BackHandler(onBack = viewModel::closeLevel)
    MiniMatesPlayContent(
        state = state,
        onSquareClick = viewModel::onSquareClick,
        onRetry = viewModel::retry,
        onNext = viewModel::nextLevel,
        onBack = viewModel::closeLevel
    )
}
