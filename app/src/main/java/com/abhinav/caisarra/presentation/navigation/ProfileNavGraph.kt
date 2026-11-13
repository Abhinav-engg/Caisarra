package com.abhinav.caisarra.presentation.navigation

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.ProfileRepository
import com.abhinav.caisarra.data.repository.PuzzleRepository
import com.abhinav.caisarra.data.repository.RatingRepository
import com.abhinav.caisarra.data.repository.RemoteGameRepository
import com.abhinav.caisarra.presentation.screens.EditProfileScreen
import com.abhinav.caisarra.presentation.screens.ProfileScreen
import com.abhinav.caisarra.presentation.screens.StatsScreen
import com.abhinav.caisarra.presentation.viewmodel.ProfileViewModel
import com.abhinav.caisarra.presentation.viewmodel.StatsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun NavGraphBuilder.profileGraph(
    navController: NavController,
    authRepository: AuthRepository,
    isGuestUser: Boolean,
    context: Context,
    scope: CoroutineScope
) {
    composable(
        route = AppRoutes.PROFILE,
        arguments = listOf(
            navArgument("username") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val usernameArg = backStackEntry.arguments?.getString("username")
        val profileViewModel = screenViewModel(key = usernameArg ?: "me") {
            ProfileViewModel(
                repository = ProfileRepository(
                    service = authRepository.createProfileService()
                ),
                ratingRepository = RatingRepository(
                    service = authRepository.createRatingService()
                ),
                remoteGameRepository = RemoteGameRepository(
                    gamesService = authRepository.createGamesService()
                ),
                puzzleRepository = PuzzleRepository(
                    service = authRepository.createPuzzleService()
                ),
                initialUsername = usernameArg
            )
        }

        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val profileEntry = currentBackStackEntry
            ?.takeIf { it.destination.route?.startsWith("profile") == true }

        val refreshRequested by (
                profileEntry?.savedStateHandle
                    ?.getStateFlow("profile_refresh_requested", false)
                    ?: remember {
                        MutableStateFlow(false)
                    }
                ).collectAsState()

        LaunchedEffect(refreshRequested, profileEntry) {
            if (refreshRequested) {
                profileViewModel.loadProfile()
                profileEntry?.savedStateHandle?.set("profile_refresh_requested", false)
            }
        }

        ProfileScreen(
            viewModel = profileViewModel,
            isGuest = isGuestUser,
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                } else {
                    scope.launch {
                        navController.safeBack(homeRoute(authRepository))
                    }
                }
            },
            onEditProfile = {
                navController.navigate(AppRoutes.EDIT_PROFILE) {
                    launchSingleTop = true
                }
            },
            onStatsClick = { mode, username ->
                navController.navigateOnce(
                    AppRoutes.stats(username = username, mode = mode.ratingKey)
                ) {
                    launchSingleTop = true
                }
            },
            onReviewGame = { gameId ->
                navController.navigateOnce(AppRoutes.gameHistory(gameId))
            },
            onDirectMessage = { userId, username ->
                navController.requireAuth(
                    context = context,
                    authRepository = authRepository,
                    scope = scope,
                    featureName = "Direct messages"
                ) {
                    navController.navigate(
                        AppRoutes.directMessage(userId, username)
                    )
                }
            },
            onEnterGame = { gameId ->
                navController.navigate(AppRoutes.onlineGame(gameId)) {
                    launchSingleTop = true
                }
            }
        )
    }

    composable(route = AppRoutes.EDIT_PROFILE) {
        val editProfileViewModel = screenViewModel {
            ProfileViewModel(
                repository = ProfileRepository(
                    service = authRepository.createProfileService()
                ),
                ratingRepository = RatingRepository(
                    service = authRepository.createRatingService()
                )
            )
        }

        EditProfileScreen(
            viewModel = editProfileViewModel,
            onBack = {
                navController.safeBack(AppRoutes.profile())
            },
            onSaved = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("profile_refresh_requested", true)
                navController.safeBack(AppRoutes.profile())
            }
        )
    }

    composable(
        route = AppRoutes.STATS,
        arguments = listOf(
            navArgument("username") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument("mode") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val usernameArg = backStackEntry.arguments?.getString("username")
        val modeArg = backStackEntry.arguments?.getString("mode")

        val statsViewModel = screenViewModel {
            StatsViewModel(
                authRepository = authRepository,
                ratingRepository = RatingRepository(
                    service = authRepository.createRatingService()
                ),
                profileRepository = ProfileRepository(
                    service = authRepository.createProfileService()
                ),
                puzzleRepository = PuzzleRepository(
                    service = authRepository.createPuzzleService()
                ),
                initialUsername = usernameArg,
                initialMode = modeArg
            )
        }

        StatsScreen(
            viewModel = statsViewModel,
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                } else {
                    navController.safePop(AppRoutes.HOME)
                }
            },
            onGameClick = { gameId ->
                navController.navigateOnce(AppRoutes.gameHistory(gameId))
            },
            onOpponentClick = { username ->
                navController.navigateOnce(AppRoutes.profile(username))
            },
            onSignIn = {
                navController.navigateOnce(AppRoutes.LOGIN)
            }
        )
    }
}
