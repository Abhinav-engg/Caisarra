package com.abhinav.caisarra.features.notificationsettings.presentation

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
fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.loading -> Box(Modifier.fillMaxSize().background(StatsBg), Alignment.Center) {
            CircularProgressIndicator(color = StatsCyan)
        }
        uiState.error != null -> FullScreenError(message = uiState.error, onRetry = viewModel::load, onBack = onBack)
        else -> NotificationSettingsContent(
            uiState = uiState,
            onBack = onBack,
            onPushEnabledChange = viewModel::setPushEnabled,
            onCategoryEnabledChange = viewModel::setCategoryEnabled,
            onUpdateErrorShown = viewModel::clearUpdateError
        )
    }
}
