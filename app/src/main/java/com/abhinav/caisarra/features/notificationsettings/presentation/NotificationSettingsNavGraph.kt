package com.abhinav.caisarra.features.notificationsettings.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.abhinav.caisarra.presentation.navigation.AppRoutes
import com.abhinav.caisarra.presentation.navigation.safeBack
import com.abhinav.caisarra.presentation.navigation.screenViewModel

fun NavGraphBuilder.notificationSettingsGraph(navController: NavController) {
    composable(route = AppRoutes.NOTIFICATION_SETTINGS) {
        NotificationSettingsScreen(
            viewModel = screenViewModel { NotificationSettingsViewModel() },
            onBack = { navController.safeBack(AppRoutes.SETTINGS_DETAIL) }
        )
    }
}
