package com.abhinav.caisarra.features.blockedplayers.presentation

import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.abhinav.caisarra.presentation.invite.FriendsViewModel
import com.abhinav.caisarra.presentation.navigation.AppRoutes
import com.abhinav.caisarra.presentation.navigation.navigateOnce
import com.abhinav.caisarra.presentation.navigation.safeBack
import com.abhinav.caisarra.presentation.navigation.screenViewModel

fun NavGraphBuilder.blockedPlayersGraph(navController: NavController) {
    composable(route = AppRoutes.BLOCKED_PLAYERS) {
        val application = LocalContext.current.applicationContext as Application
        BlockedPlayersScreen(
            viewModel = screenViewModel { FriendsViewModel(application) },
            onBack = { navController.safeBack(AppRoutes.SETTINGS_DETAIL) },
            onViewProfile = { navController.navigateOnce(AppRoutes.profile(it)) }
        )
    }
}
