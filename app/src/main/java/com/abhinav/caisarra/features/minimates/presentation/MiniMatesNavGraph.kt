package com.abhinav.caisarra.features.minimates.presentation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.features.minimates.data.MiniMatesRepository
import com.abhinav.caisarra.presentation.navigation.AppRoutes
import com.abhinav.caisarra.presentation.navigation.safeBack
import com.abhinav.caisarra.presentation.navigation.screenViewModel

fun NavGraphBuilder.miniMatesGraph(navController: NavController) {
    composable(route = AppRoutes.MINI_MATES) {
        val context = LocalContext.current
        MiniMatesScreen(
            viewModel = screenViewModel {
                MiniMatesViewModel(MiniMatesRepository(RetrofitInstance.createMiniMatesService(TokenManager.get(context))))
            },
            onBack = { navController.safeBack(AppRoutes.PUZZLES) }
        )
    }
}
