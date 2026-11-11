package com.abhinav.caisarra.presentation.navigation

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.abhinav.caisarra.data.local.RatingPreferencesStore
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.GameRepository
import com.abhinav.caisarra.data.repository.ProfileRepository
import com.abhinav.caisarra.data.repository.PuzzleRepository
import com.abhinav.caisarra.data.repository.RemoteGameRepository
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
import com.abhinav.caisarra.presentation.screens.AllGameHistoryScreen
import com.abhinav.caisarra.presentation.screens.GuestHomeScreen
import com.abhinav.caisarra.presentation.screens.HomeScreen
import com.abhinav.caisarra.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


fun NavGraphBuilder.homeGraph(
    navController: NavController,
    authRepository: AuthRepository,
    context: Context,
    scope: CoroutineScope,
    onOpenPlayGame: () -> Unit
) {
    composable(route = AppRoutes.GUEST_HOME) {
        var guestId by remember {
            mutableStateOf("")
        }

        var isLoggingOut by remember {
            mutableStateOf(false)
        }

        val guestHomeViewModel = screenViewModel {
            HomeViewModel(
                repository = authRepository,
                gameRepository = GameRepository.get(context.applicationContext),
                remoteGameRepository = RemoteGameRepository(
                    gamesService = authRepository.createGamesService()
                ),
                profileRepository = ProfileRepository(
                    service = authRepository.createProfileService()
                ),
                ratingPreferenceStore = RatingPreferencesStore.get(context.applicationContext),
                puzzleRepository = PuzzleRepository(
                    service = authRepository.createPuzzleService()
                )
            )
        }

        val guestHomeState by guestHomeViewModel.state.collectAsState()
        val activity = LocalContext.current as? ComponentActivity

        BackHandler {
            activity?.finish()
        }

        LaunchedEffect(Unit) {
            if (authRepository.isLoggedIn()) {
                navController.navigate(AppRoutes.HOME) {
                    popUpTo(AppRoutes.GUEST_HOME) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
                return@LaunchedEffect
            }
            guestId = authRepository.getGuestId().orEmpty()
        }

        GuestHomeScreen(
            guestId = guestId,
            gameHistory = guestHomeState.gameHistory,
            isLoadingHistory = guestHomeState.isLoadingHistory,
            onPlayGame = {
                navController.navigateOnce(AppRoutes.PASS_AND_PLAY_SETUP)
            },
            onReviewGame = { gameId ->
                navController.navigateOnce(AppRoutes.gameHistory(gameId))
            },
            onViewAllHistory = {
                navController.navigateOnce(AppRoutes.ALL_GAME_HISTORY)
            },
            onSignIn = {
                navController.goToLogin()
            },
            onSignUp = {
                navController.navigateOnce(AppRoutes.REGISTER)
            },
            onLogout = {
                if (!isLoggingOut) {
                    scope.launch {
                        isLoggingOut = true
                        authRepository.logout()
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            },
            isLoggingOut = isLoggingOut
        )
    }

    composable(route = AppRoutes.ALL_GAME_HISTORY) {
        val allHistoryViewModel = screenViewModel {
            HomeViewModel(
                repository = authRepository,
                gameRepository = GameRepository.get(context.applicationContext),
                remoteGameRepository = RemoteGameRepository(
                    gamesService = authRepository.createGamesService()
                ),
                profileRepository = ProfileRepository(
                    service = authRepository.createProfileService()
                ),
                ratingPreferenceStore = RatingPreferencesStore.get(context.applicationContext),
                puzzleRepository = PuzzleRepository(
                    service = authRepository.createPuzzleService()
                )
            )
        }

        AllGameHistoryScreen(
            viewModel = allHistoryViewModel,
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                } else {
                    scope.launch {
                        navController.safeBack(homeRoute(authRepository))
                    }
                }
            },
            onReviewGame = { gameId ->
                navController.navigateOnce(AppRoutes.gameHistory(gameId))
            }
        )
    }

    composable(route = AppRoutes.HOME) {
        val homeViewModel = screenViewModel {
            HomeViewModel(
                repository = authRepository,
                gameRepository = GameRepository.get(context.applicationContext),
                remoteGameRepository = RemoteGameRepository(
                    gamesService = authRepository.createGamesService()
                ),
                profileRepository = ProfileRepository(
                    service = authRepository.createProfileService()
                ),
                ratingPreferenceStore = RatingPreferencesStore.get(context.applicationContext),
                puzzleRepository = PuzzleRepository(
                    service = authRepository.createPuzzleService()
                )
            )
        }

        val activity = LocalContext.current as? ComponentActivity

        BackHandler {
            activity?.finish()
        }

        HomeScreen(
            viewModel = homeViewModel,
            onStartChallenge = {
                onOpenPlayGame()
            },
            onStartQuickPlay = { mode ->
                val minutes = when (mode) {
                    GameMode.BULLET -> 1
                    GameMode.BLITZ -> 3
                    GameMode.RAPID -> 10
                    GameMode.DAILY -> 1440
                    else -> 10
                }
                navController.requireAuth(
                    context = context,
                    authRepository = authRepository,
                    scope = scope,
                    featureName = "Online matchmaking"
                ) {
                    navController.navigateOnce(AppRoutes.matchmaking(minutes, 0))
                }
            },
            onOpenActiveGame = { gameId ->
                navController.navigateOnce(AppRoutes.onlineGame(gameId))
            },
            onViewAllHistory = {
                navController.navigateOnce(AppRoutes.ALL_GAME_HISTORY)
            },
            onReviewGame = { gameId ->
                navController.navigateOnce(AppRoutes.gameHistory(gameId))
            },
            onOpenTactic = { tactic ->
                navController.navigateOnce(tactic.route)
            },
            onOpenPuzzles = {
                navController.navigateToTab(AppRoutes.PUZZLES)
            },
            onLogoutSuccess = {
                navController.goToLogin()
            },
            onOpenNearby = {
                navController.navigateOnce(AppRoutes.NEARBY) {
                    launchSingleTop = true
                }
            },
            onPlayBots = {
                navController.navigateOnce(AppRoutes.BOT_SETUP)
            },
            onOpenProfile = {
                navController.navigateOnce(AppRoutes.profile())
            },
            onStreakClick = {
                navController.navigateOnce(AppRoutes.DAILY_PUZZLE)
            },
            onOpenCommunity = {
                navController.navigate(AppRoutes.COMMUNITY)
            },
            onOpenNotifications = {
                navController.requireAuth(
                    context = context,
                    authRepository = authRepository,
                    scope = scope,
                    featureName = "Notifications"
                ) {
                    navController.navigateOnce(AppRoutes.NOTIFICATIONS)
                }
            },
            onStatsClick = { mode ->
                navController.navigateOnce(AppRoutes.stats(mode = mode.ratingKey)) {
                    launchSingleTop = true
                }
            }
        )
    }
}
