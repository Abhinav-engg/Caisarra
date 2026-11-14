package com.abhinav.caisarra.features.notificationsettings.presentation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.local.settingsDataStore
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.repository.NotificationRepository
import com.abhinav.caisarra.features.notificationsettings.data.DevicePushRepository
import com.abhinav.caisarra.features.notificationsettings.data.NotificationPreferencesRepository
import com.abhinav.caisarra.presentation.navigation.AppRoutes
import com.abhinav.caisarra.presentation.navigation.safeBack
import com.abhinav.caisarra.presentation.navigation.screenViewModel

fun NavGraphBuilder.notificationSettingsGraph(navController: NavController) {
    composable(route = AppRoutes.NOTIFICATION_SETTINGS) {
        val context = LocalContext.current
        NotificationSettingsScreen(
            viewModel = screenViewModel {
                NotificationSettingsViewModel(
                    NotificationPreferencesRepository(
                        RetrofitInstance.createNotificationPreferencesService(TokenManager.get(context))
                    ),
                    DevicePushRepository(context.settingsDataStore, NotificationRepository.get(context))
                )
            },
            onBack = { navController.safeBack(AppRoutes.SETTINGS_DETAIL) }
        )
    }
}
