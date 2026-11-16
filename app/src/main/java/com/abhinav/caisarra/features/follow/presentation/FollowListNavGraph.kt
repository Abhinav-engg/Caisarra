package com.abhinav.caisarra.features.follow.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.features.follow.data.FollowRepository
import com.abhinav.caisarra.features.follow.domain.FollowListTab
import com.abhinav.caisarra.presentation.navigation.AppRoutes
import com.abhinav.caisarra.presentation.navigation.navigateOnce
import com.abhinav.caisarra.presentation.navigation.screenViewModel

fun NavGraphBuilder.followListGraph(navController: NavController) {
    composable(
        route = AppRoutes.FOLLOW_LIST,
        arguments = listOf(
            navArgument("userId") { type = NavType.LongType },
            navArgument("username") { type = NavType.StringType; defaultValue = "" },
            navArgument("tab") { type = NavType.StringType; defaultValue = FollowListTab.FOLLOWERS.name }
        )
    ) { entry ->
        val context = LocalContext.current
        val args = requireNotNull(entry.arguments)
        val currentUsername by produceState<String?>(null) { value = TokenManager.get(context).getUsername() }
        FollowListScreen(
            viewModel = screenViewModel(key = "${args.getLong("userId")}") {
                FollowListViewModel(FollowRepository(context), args.getLong("userId"), FollowListTab.valueOf(args.getString("tab").orEmpty()))
            },
            title = (args.getString("username")?.takeIf { it.isNotBlank() } ?: currentUsername.orEmpty()).uppercase(),
            currentUsername = currentUsername,
            onBack = { navController.popBackStack() },
            onOpenProfile = { navController.navigateOnce(AppRoutes.profile(it)) }
        )
    }
}
