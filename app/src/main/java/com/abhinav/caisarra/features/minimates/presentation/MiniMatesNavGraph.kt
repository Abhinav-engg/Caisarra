package com.abhinav.caisarra.features.minimates.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.abhinav.caisarra.presentation.navigation.AppRoutes
import com.abhinav.caisarra.presentation.navigation.safeBack
import com.abhinav.caisarra.presentation.navigation.screenViewModel

fun NavGraphBuilder.miniMatesGraph(navController: NavController) {
    composable(route = AppRoutes.MINI_MATES) {
        MiniMatesScreen(
            viewModel = screenViewModel { MiniMatesViewModel() },
            onBack = { navController.safeBack(AppRoutes.PUZZLES) }
        )
    }
}
