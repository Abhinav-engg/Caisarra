package com.abhinav.caisarra.presentation.navigation

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.ChatRepository
import com.abhinav.caisarra.data.repository.GameAnalysisRepository
import com.abhinav.caisarra.data.repository.GameRepository
import com.abhinav.caisarra.data.repository.RemoteGameRepository
import com.abhinav.caisarra.presentation.game.BotGameViewModel
import com.abhinav.caisarra.presentation.game.OnlineGameViewModel
import com.abhinav.caisarra.presentation.game.screens.GameAnalysisScreen
import com.abhinav.caisarra.presentation.game.screens.GameHistoryScreen
import com.abhinav.caisarra.presentation.game.screens.GameScreen
import com.abhinav.caisarra.features.newgame.presentation.NewGameScreen
import com.abhinav.caisarra.presentation.game.screens.MatchmakingScreen
import com.abhinav.caisarra.presentation.game.screens.OnlineGameScreen
import com.abhinav.caisarra.presentation.game.screens.PassAndPlaySetupScreen
import com.abhinav.caisarra.presentation.game.screens.TimeControlsScreen
import com.abhinav.caisarra.presentation.game.viewmodel.BotHistoryViewModel
import com.abhinav.caisarra.presentation.game.viewmodel.BotSetupViewModel
import com.abhinav.caisarra.presentation.game.viewmodel.GameAnalysisViewModel
import com.abhinav.caisarra.presentation.game.viewmodel.GameHistoryReviewViewModel
import com.abhinav.caisarra.presentation.game.viewmodel.GameSetupViewModel
import com.abhinav.caisarra.presentation.game.viewmodel.GameViewModel
import com.abhinav.caisarra.presentation.game.viewmodel.MatchmakingViewModel
import com.abhinav.caisarra.presentation.game.viewmodel.PassAndPlaySetupViewModel
import com.abhinav.caisarra.presentation.screens.BotGameScreen
import com.abhinav.caisarra.presentation.screens.BotHistoryScreen
import com.abhinav.caisarra.presentation.screens.BotSetupScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun NavGraphBuilder.gameGraph(
    navController: NavController,
    authRepository: AuthRepository,
    context: Context,
    scope: CoroutineScope,
    onSetFriendsBackRoute: (String) -> Unit
) {
    composable(route = AppRoutes.GAME_SETUP) {
        val setupViewModel = screenViewModel {
            GameSetupViewModel()
        }
        val setupState by setupViewModel.state.collectAsState()

        NewGameScreen(
            state = setupState,
            onOpenTimeControls = {
                navController.navigateOnce(AppRoutes.TIME_CONTROLS)
            },
            onStartMatch = {
                scope.launch {
                    if (authRepository.isGuest() || !authRepository.isLoggedIn()) {
                        Toast.makeText(
                            context,
                            "Online matchmaking requires a registered account.",
                            Toast.LENGTH_LONG
                        ).show()
                        navController.navigateOnce(AppRoutes.LOGIN)
                        return@launch
                    }

                    val minutes = setupState.minutes.toInt()
                    val increment = setupState.bonusSeconds.toInt()
                    val allowedControls = setOf(
                        1 to 0, 1 to 1, 2 to 1,
                        3 to 0, 3 to 2, 5 to 0,
                        5 to 2, 5 to 5, 10 to 0,
                        10 to 5, 15 to 10, 20 to 0,
                        30 to 0, 60 to 0,
                        1440 to 0, 2880 to 0,
                        4320 to 0, 7200 to 0,
                        10080 to 0, 20160 to 0
                    )

                    if ((minutes to increment) !in allowedControls) {
                        Toast.makeText(
                            context,
                            "Please select a supported time control.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }

                    navController.navigateOnce(
                        AppRoutes.matchmaking(minutes, increment)
                    )
                }
            },
            onPlayWithFriend = {
                navController.requireAuth(
                    context = context,
                    authRepository = authRepository,
                    scope = scope,
                    featureName = "Friends"
                ) {
                    onSetFriendsBackRoute(AppRoutes.GAME_SETUP)
                    navController.navigateOnce(AppRoutes.PLAY_WITH_FRIEND_HOME) {
                        launchSingleTop = true
                    }
                }
            },
            onTournaments = {
                navController.requireAuth(
                    context = context,
                    authRepository = authRepository,
                    scope = scope,
                    featureName = "Tournaments"
                ) {
                    navController.navigateOnce(AppRoutes.TOURNAMENTS)
                }
            },
            onPlayBots = {
                navController.navigateOnce(AppRoutes.BOT_SETUP)
            },
            onPassAndPlay = {
                navController.navigateOnce(AppRoutes.PASS_AND_PLAY_SETUP)
            },
            onBack = {
                scope.launch {
                    navController.safeBack(homeRoute(authRepository))
                }
            }
        )
    }
    composable(route = AppRoutes.PASS_AND_PLAY_SETUP) {
        val setupViewModel = screenViewModel {
            PassAndPlaySetupViewModel()
        }
        val setupState by setupViewModel.state.collectAsState()

        PassAndPlaySetupScreen(
            state = setupState,
            onEvent = setupViewModel::onEvent,
            onStart = {
                navController.navigateOnce(
                    AppRoutes.newGame(
                        white = "Player 1",
                        black = "Player 2",
                        minutes = setupState.minutes.toInt(),
                        flip = setupState.autoFlip,
                        undo = true,
                        clockEnabled = setupState.clockEnabled
                    )
                ) {
                    popUpTo(AppRoutes.PASS_AND_PLAY_SETUP) {
                        inclusive = true
                    }
                }
            },
            onBack = {
                scope.launch {
                    val fallbackRoute = if (authRepository.isGuest()) {
                        AppRoutes.GUEST_HOME
                    } else {
                        AppRoutes.GAME_SETUP
                    }
                    navController.safeBack(fallbackRoute)
                }
            }
        )
    }
    composable(route = AppRoutes.TIME_CONTROLS) { timeControlsEntry ->
        val setupEntry = remember(timeControlsEntry, navController) {
            runCatching {
                navController.getBackStackEntry(AppRoutes.GAME_SETUP)
            }.getOrNull()
        }

        if (setupEntry == null) {
            LaunchedEffect(timeControlsEntry) {
                navController.navigate(AppRoutes.GAME_SETUP) {
                    popUpTo(AppRoutes.TIME_CONTROLS) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        } else {
            val setupViewModel: GameSetupViewModel = viewModel(
                viewModelStoreOwner = setupEntry
            )
            val setupState by setupViewModel.state.collectAsState()

            TimeControlsScreen(
                state = setupState,
                onEvent = setupViewModel::onEvent,
                onConfirm = {
                    navController.safeBack(AppRoutes.GAME_SETUP)
                },
                onBack = {
                    navController.safeBack(AppRoutes.GAME_SETUP)
                }
            )
        }
    }
    composable(
        route = AppRoutes.MATCHMAKING,
        arguments = listOf(
            navArgument("minutes") {
                type = NavType.IntType
            },
            navArgument("incrementSeconds") {
                type = NavType.IntType
            }
        )
    ) { entry ->
        val minutes = entry.arguments?.getInt("minutes") ?: 10
        val increment = entry.arguments?.getInt("incrementSeconds") ?: 0

        val matchmakingViewModel = screenViewModel {
            MatchmakingViewModel(authRepository.createMatchmakingService())
        }

        LaunchedEffect(matchmakingViewModel, minutes, increment) {
            matchmakingViewModel.startMatchOnce(
                minutes = minutes,
                incrementSeconds = increment,
                rated = true
            )
        }

        MatchmakingScreen(
            viewModel = matchmakingViewModel,
            minutes = minutes,
            incrementSeconds = increment,
            onMatched = { gameId ->
                navController.navigate(AppRoutes.onlineGame(gameId)) {
                    popUpTo(AppRoutes.MATCHMAKING) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
            onBack = {
                navController.safeBack(AppRoutes.GAME_SETUP)
            }
        )
    }

    composable(
        route = AppRoutes.GAME,
        arguments = listOf(
            navArgument("resume") {
                type = NavType.BoolType
                defaultValue = false
            },
            navArgument("white") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("black") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("minutes") {
                type = NavType.IntType
                defaultValue = 10
            },
            navArgument("flip") {
                type = NavType.BoolType
                defaultValue = false
            },
            navArgument("undo") {
                type = NavType.BoolType
                defaultValue = true
            },
            navArgument("clock") {
                type = NavType.BoolType
                defaultValue = true
            }
        )
    ) { entry ->
        val resume = entry.arguments?.getBoolean("resume") ?: false
        val white = entry.arguments?.getString("white").orEmpty()
        val black = entry.arguments?.getString("black").orEmpty()
        val minutes = entry.arguments?.getInt("minutes") ?: 10
        val flip = entry.arguments?.getBoolean("flip") ?: false
        val undo = entry.arguments?.getBoolean("undo") ?: true
        val clockEnabled = entry.arguments?.getBoolean("clock") ?: true

        val gameViewModel = screenViewModel {
            GameViewModel(
                gameRepository = GameRepository.get(context.applicationContext),
                authRepository = authRepository,
                resumeGame = resume,
                newWhiteName = white,
                newBlackName = black,
                newTimeMinutes = minutes,
                newBoardFlipped = flip,
                newUndoEnabled = undo,
                clockEnabled = clockEnabled
            )
        }

        GameScreen(
            viewModel = gameViewModel,
            onHome = {
                scope.launch {
                    if (navController.isResumed()) {
                        val destination = if (authRepository.isGuest()) {
                            AppRoutes.GUEST_HOME
                        } else {
                            AppRoutes.HOME
                        }
                        val popped = navController.popBackStack(
                            destination,
                            inclusive = false
                        )
                        if (!popped) {
                            navController.navigate(destination) {
                                popUpTo(navController.graph.id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            },
            onNewGame = {
                navController.navigateOnce(AppRoutes.GAME_SETUP) {
                    popUpTo(AppRoutes.GAME) {
                        inclusive = true
                    }
                }
            }
        )
    }

    composable(
        route = AppRoutes.ONLINE_GAME,
        arguments = listOf(
            navArgument("gameId") {
                type = NavType.StringType
            }
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = "caisarra://game/online/{gameId}" },
            navDeepLink { uriPattern = "https://caisaara.app/game/{gameId}" },
            navDeepLink { uriPattern = "https://caisaara.duckdns.org/game/{gameId}" }
        )
    ) { entry ->
        val gameId = entry.arguments?.getString("gameId").orEmpty()
        val onlineViewModel = screenViewModel(key = "online_game_$gameId") {
            OnlineGameViewModel(context as Application, gameId)
        }
        val analysisViewModel = screenViewModel(key = "online_game_analysis_$gameId") {
            GameAnalysisViewModel(
                repository = GameAnalysisRepository(authRepository.createAnalysisService()),
                gameId = gameId
            )
        }

        OnlineGameScreen(
            viewModel = onlineViewModel,
            analysisViewModel = analysisViewModel,
            onHome = {
                navController.goHome()
            },
            onNewOpponent = { minutes, increment ->
                navController.navigate(AppRoutes.matchmaking(minutes, increment)) {
                    popUpTo(AppRoutes.ONLINE_GAME) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
            onRematchStarted = { rematchGameId, _, _ ->
                navController.navigate(AppRoutes.onlineGame(rematchGameId)) {
                    popUpTo(AppRoutes.ONLINE_GAME) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
            onStartAnalysis = {
                navController.navigate(AppRoutes.gameAnalysis(gameId))
            }
        )
    }

    composable(
        route = AppRoutes.GAME_HISTORY,
        arguments = listOf(
            navArgument("gameId") {
                type = NavType.StringType
            }
        )
    ) { entry ->
        val gameId = entry.arguments?.getString("gameId").orEmpty()
        val historyViewModel = screenViewModel {
            GameHistoryReviewViewModel(
                repository = GameRepository.get(context.applicationContext),
                gameId = gameId,
                authRepository = authRepository,
                remoteGameRepository = RemoteGameRepository(authRepository.createGamesService()),
                chatRepository = ChatRepository.get(context.applicationContext)
            )
        }

        GameHistoryScreen(
            viewModel = historyViewModel,
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                } else {
                    scope.launch {
                        navController.safeBack(homeRoute(authRepository))
                    }
                }
            },
            onAnalyze = {
                navController.navigate(AppRoutes.gameAnalysis(gameId))
            }
        )
    }
    composable(
        route = AppRoutes.GAME_ANALYSIS,
        arguments = listOf(
            navArgument("gameId") {
                type = NavType.StringType
            }
        )
    ) { entry ->
        val gameId = entry.arguments?.getString("gameId").orEmpty()
        val analysisViewModel = screenViewModel {
            GameAnalysisViewModel(
                repository = GameAnalysisRepository(authRepository.createAnalysisService()),
                gameId = gameId
            )
        }

        GameAnalysisScreen(
            viewModel = analysisViewModel,
            onBack = {
                navController.safePop()
            }
        )
    }

    composable(route = AppRoutes.BOT_SETUP) {
        val botSetupViewModel = screenViewModel {
            BotSetupViewModel(context as Application)
        }

        BotSetupScreen(
            viewModel = botSetupViewModel,
            onGameCreated = { gameId ->
                navController.navigate(AppRoutes.botGame(gameId)) {
                    popUpTo(AppRoutes.BOT_SETUP) {
                        inclusive = true
                    }
                }
            },
            onViewHistory = {
                navController.navigateOnce(AppRoutes.BOT_HISTORY)
            },
            onBack = {
                navController.safeBack(AppRoutes.GAME_SETUP)
            }
        )
    }

    composable(route = AppRoutes.BOT_HISTORY) {
        val botHistoryViewModel = screenViewModel {
            BotHistoryViewModel(context as Application)
        }

        BotHistoryScreen(
            viewModel = botHistoryViewModel,
            onOpenGame = { gameId ->
                navController.navigateOnce(AppRoutes.botGame(gameId))
            },
            onNewGame = {
                navController.navigate(AppRoutes.BOT_SETUP) {
                    popUpTo(AppRoutes.BOT_HISTORY) {
                        inclusive = true
                    }
                }
            },
            onBack = {
                navController.safeBack(AppRoutes.BOT_SETUP)
            }
        )
    }

    composable(
        route = AppRoutes.BOT_GAME,
        arguments = listOf(
            navArgument("gameId") {
                type = NavType.StringType
            }
        )
    ) { entry ->
        val gameId = entry.arguments?.getString("gameId").orEmpty()
        val botViewModel = screenViewModel {
            BotGameViewModel(context as Application, gameId)
        }

        BotGameScreen(
            viewModel = botViewModel,
            onHome = {
                navController.goHome()
            },
            onNewGame = {
                navController.navigate(AppRoutes.BOT_SETUP) {
                    popUpTo(AppRoutes.BOT_GAME) {
                        inclusive = true
                    }
                }
            }
        )
    }
}
