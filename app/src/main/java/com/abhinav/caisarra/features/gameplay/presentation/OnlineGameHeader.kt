package com.abhinav.caisarra.features.gameplay.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.presentation.game.model.GameUiState

@Composable
fun OnlineGameHeader(
    state: GameUiState,
    pingMs: Long?,
    connected: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        CaisaaraTopBar(title = onlineGameTitle(state), onBack = onBack)
        OnlineGameStatusRow(
            isRated = state.isRated,
            isDailyMode = state.isDailyMode,
            pingMs = pingMs,
            connected = connected
        )
    }
}
