package com.abhinav.caisarra.presentation.navigation

import android.app.Application
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.presentation.screens.LoginScreen
import com.abhinav.caisarra.presentation.screens.ResetPasswordScreen
import com.abhinav.caisarra.presentation.screens.SetNewPasswordScreen
import com.abhinav.caisarra.presentation.screens.SignUpScreen
import com.abhinav.caisarra.presentation.screens.VerifyScreen
import com.abhinav.caisarra.presentation.viewmodel.LoginViewModel
import com.abhinav.caisarra.presentation.viewmodel.ResetPasswordViewModel
import com.abhinav.caisarra.presentation.viewmodel.SetNewPasswordViewModel
import com.abhinav.caisarra.presentation.viewmodel.SignUpViewModel
import com.abhinav.caisarra.presentation.viewmodel.VerifyPurpose
import com.abhinav.caisarra.presentation.viewmodel.VerifyViewModel
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.abhinav.caisarra.data.repository.AuthResult
import com.abhinav.caisarra.presentation.screens.RatingScreen
import com.abhinav.caisarra.presentation.viewmodel.RatingViewModel
import com.abhinav.caisarra.data.local.RegistrationDataStore
import com.abhinav.caisarra.presentation.screens.HomeScreen
import com.abhinav.caisarra.presentation.viewmodel.HomeViewModel
import com.abhinav.caisarra.presentation.game.screens.GameScreen
import com.abhinav.caisarra.presentation.game.screens.GameSetupScreen
import com.abhinav.caisarra.presentation.game.screens.TimeControlsScreen
import com.abhinav.caisarra.presentation.game.viewmodel.GameSetupViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.abhinav.caisarra.data.repository.GameRepository
import com.abhinav.caisarra.presentation.game.viewmodel.GameViewModel
import com.abhinav.caisarra.presentation.screens.GuestHomeScreen
import com.abhinav.caisarra.presentation.game.screens.GameHistoryScreen
import com.abhinav.caisarra.presentation.game.viewmodel.GameHistoryReviewViewModel
import com.abhinav.caisarra.data.local.PendingInviteStore
import com.abhinav.caisarra.data.local.PendingOtpStore
import com.abhinav.caisarra.data.repository.RemoteGameRepository
import com.abhinav.caisarra.presentation.game.OnlineGameViewModel
import com.abhinav.caisarra.presentation.game.screens.OnlineGameScreen
import com.abhinav.caisarra.presentation.invite.CreateInviteScreen
import com.abhinav.caisarra.presentation.invite.CreateInviteViewModel
import com.abhinav.caisarra.presentation.invite.JoinInviteScreen
import com.abhinav.caisarra.presentation.invite.JoinInviteViewModel
import com.abhinav.caisarra.presentation.invite.PlayWithFriendScreen
import com.abhinav.caisarra.presentation.screens.AllGameHistoryScreen
import com.abhinav.caisarra.data.repository.ProfileRepository
import com.abhinav.caisarra.presentation.screens.ProfileScreen
import com.abhinav.caisarra.presentation.viewmodel.ProfileViewModel
import com.abhinav.caisarra.data.repository.ChatRepository
import com.abhinav.caisarra.presentation.game.screens.MatchmakingScreen
import com.abhinav.caisarra.presentation.game.viewmodel.MatchmakingViewModel
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhinav.caisarra.data.repository.GameAnalysisRepository
import com.abhinav.caisarra.presentation.game.screens.BotSetupScreen
import com.abhinav.caisarra.presentation.game.viewmodel.BotSetupViewModel
import com.abhinav.caisarra.presentation.game.BotGameViewModel
import com.abhinav.caisarra.presentation.game.screens.BotGameScreen
import com.abhinav.caisarra.presentation.game.screens.PassAndPlaySetupScreen
import com.abhinav.caisarra.presentation.game.viewmodel.PassAndPlaySetupViewModel
import com.abhinav.caisarra.presentation.game.screens.GameAnalysisScreen
import com.abhinav.caisarra.presentation.game.viewmodel.GameAnalysisViewModel
import com.abhinav.caisarra.presentation.screens.EditProfileScreen
import androidx.navigation.compose.currentBackStackEntryAsState


object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RESET_PASSWORD = "reset_password"
    const val VERIFY = "verify/{purpose}/{email}"
    const val NEW_PASSWORD = "new_password"
    const val HOME = "home"
    const val GAME_SETUP = "game_setup"
    const val TIME_CONTROLS = "time_controls"
    const val GAME_HISTORY = "game_history/{gameId}"
    const val GAME = "game?resume={resume}" +
            "&white={white}" +
            "&black={black}" +
            "&minutes={minutes}" +
            "&flip={flip}" +
            "&undo={undo}" +
            "&clock={clock}"
    const val GUEST_HOME = "guest_options"
    const val RATING = "rating"
    const val CREATE_INVITE = "create_invite"
    const val PLAY_WITH_FRIEND = "play_with_friend"
    const val JOIN_INVITE = "play/{code}"
    const val ONLINE_GAME = "online_game/{gameId}"
    const val ALL_GAME_HISTORY = "all_game_history"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val MATCHMAKING = "matchmaking/{minutes}/{incrementSeconds}"
    const val GAME_ANALYSIS = "game_analysis/{gameId}"


    const val BOT_SETUP = "bot_setup"
    const val BOT_GAME = "bot_game/{gameId}"
    const val PASS_AND_PLAY_SETUP = "pass_and_play_setup"

    fun verify(email: String, purpose: VerifyPurpose) =
        "verify/${purpose.name}/${Uri.encode(email)}"

    fun newGame(
        white: String,
        black: String,
        minutes: Int,
        flip: Boolean,
        undo: Boolean,
        clockEnabled: Boolean
    ): String {
        return "game" + "?resume=false" +
                "&white=${Uri.encode(white)}" +
                "&black=${Uri.encode(black)}" +
                "&minutes=$minutes" +
                "&flip=$flip" +
                "&undo=$undo" +
                "&clock=$clockEnabled"
    }

//    fun resumeGame(): String {
//        return "game" + "?resume=true" +
//                "&white=" +
//                "&black=" +
//                "&minutes=10" +
//                "&flip=false" +
//                "&undo=true"
//    }

    fun gameHistory(
        gameId: String
    ): String {
        return "game_history/${Uri.encode(gameId)}"
    }
    fun onlineGame(gameId: String) = "online_game/${Uri.encode(gameId)}"

    fun botGame(gameId: String) = "bot_game/${Uri.encode(gameId)}"

    fun joinInvite(code: String) = "play/${Uri.encode(code)}"
    fun matchmaking(minutes: Int, incrementSeconds: Int) = "matchmaking/$minutes/$incrementSeconds"
    fun gameAnalysis(gameId: String) = "game_analysis/${Uri.encode(gameId)}"

}

@Composable
private inline fun <reified VM : ViewModel> screenViewModel(crossinline create: () -> VM): VM =
    viewModel(factory = viewModelFactory { initializer { create() } })

/**
 * Navigate back safely. If the current destination has no previous entry,
 * replace the navigation stack with the supplied fallback route.
 */
private fun NavController.safeBack(fallbackRoute: String) {
    if (currentDestination?.route == fallbackRoute) return

    val popped = runCatching { popBackStack() }.getOrDefault(false)
    if (!popped) {
        runCatching {
            navigate(fallbackRoute) {
                popUpTo(graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}

private suspend fun homeRoute(authRepository: AuthRepository): String =
    if (authRepository.isGuest()) AppRoutes.GUEST_HOME else AppRoutes.HOME

private fun NavController.goHome() {
    navigate(AppRoutes.HOME) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavController.goToLogin() {
    navigate(AppRoutes.LOGIN) {
        popUpTo(AppRoutes.LOGIN) { inclusive = false }
        launchSingleTop = true
    }
}

private fun NavController.goHomeThenPendingInvite(store: PendingInviteStore) {
    val code = store.consume()
    goHome()
    if (code != null) navigate(AppRoutes.joinInvite(code))
}

@Composable
fun AppNavigation(authRepository: AuthRepository) {
    val context = LocalContext.current.applicationContext
    val pendingOtpStore = remember {
        PendingOtpStore(context)
    }
    val pendingInviteStore = remember {
        PendingInviteStore.get(context)
    }
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val pending = pendingOtpStore.getPendingVerification()
        startDestination = when {
            pending != null -> {
                val purpose = runCatching {
                    VerifyPurpose.valueOf(pending.purpose)
                }.getOrNull()

                if (purpose != null) {
                    AppRoutes.verify(pending.email, purpose)
                } else {
                    pendingOtpStore.clear()
                    AppRoutes.LOGIN
                }
            }

            authRepository.isLoggedIn() -> AppRoutes.HOME
            authRepository.isGuest() -> AppRoutes.GUEST_HOME
            else -> AppRoutes.LOGIN
        }
    }

    val start = startDestination
    if (start == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF071017))
        )
        return
    }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    fun openPlayGame() {
        navController.navigate(AppRoutes.GAME_SETUP)
    }

    fun startLocalGame(minutes: Int) {
        navController.navigate(
            AppRoutes.newGame(
                white = "Player 1",
                black = "Player 2",
                minutes = minutes,
                flip = false,
                undo = true,
                clockEnabled = true
            )
        ) {
            popUpTo(AppRoutes.GAME_SETUP) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = start
    ) {
        composable(route = AppRoutes.LOGIN) {
            LoginScreen(
                viewModel = screenViewModel { LoginViewModel(authRepository) },
                onLoginSuccess = { navController.goHomeThenPendingInvite(pendingInviteStore) },
                onForgotPasswordClick = { navController.navigate(AppRoutes.RESET_PASSWORD) },
                onSignUpClick = { navController.navigate(AppRoutes.REGISTER) },
                onContinueAsGuestClick = {
                    scope.launch {
                        when (val result = authRepository.guestLogin()) {
                            is AuthResult.Success -> {
                                navController.navigate(AppRoutes.GUEST_HOME) {
                                    popUpTo(AppRoutes.LOGIN) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }

                            is AuthResult.Error -> {
                                Toast.makeText(
                                    context,
                                    result.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            )
        }

        composable(route = AppRoutes.REGISTER) {
            SignUpScreen(
                viewModel = screenViewModel {
                    SignUpViewModel(
                        repository = authRepository,
                        registrationDataStore =
                            RegistrationDataStore(context.applicationContext)
                    )
                },
                onSignUpSuccess = { email ->
                    scope.launch {
                        pendingOtpStore.save(
                            email = email,
                            purpose = VerifyPurpose.REGISTRATION.name
                        )

                        navController.navigate(
                            AppRoutes.verify(
                                email,
                                VerifyPurpose.REGISTRATION
                            )
                        )
                    }
                },
                onSignInClick = {
                    navController.goToLogin()
                }
            )
        }

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
                    gameRepository = GameRepository.get(
                        context.applicationContext
                    ),
                    remoteGameRepository = RemoteGameRepository(
                        authRepository.createGamesService()
                    ),
                    profileRepository = ProfileRepository(
                        service = authRepository.createProfileService()
                    )

                )

            }


            val guestHomeState by guestHomeViewModel.state.collectAsState()
            LaunchedEffect(Unit) {
                guestId = authRepository.getGuestId().orEmpty()
            }
            GuestHomeScreen(
                guestId = guestId,
                gameHistory = guestHomeState.gameHistory,
                isLoadingHistory = guestHomeState.isLoadingHistory,
                onPlayGame = {
                    navController.navigate(AppRoutes.PASS_AND_PLAY_SETUP)
                },
                onReviewGame = { gameId ->
                    navController.navigate(
                        AppRoutes.gameHistory(gameId)
                    )
                },
                onViewAllHistory = {
                    navController.navigate(AppRoutes.ALL_GAME_HISTORY)
                },
                onSignIn = {
                    navController.goToLogin()
                },
                onSignUp = {
                    navController.navigate(
                        AppRoutes.REGISTER
                    )
                },
                onLogout = {
                    if (!isLoggingOut) {
                        scope.launch {
                            isLoggingOut = true
                            authRepository.logout()
                            navController.navigate(
                                AppRoutes.LOGIN
                            ) {
                                popUpTo(
                                    navController.graph.id
                                ) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                },

                isLoggingOut = isLoggingOut,
                onOpenProfile = {
                    navController.navigate(AppRoutes.PROFILE)
                }
            )
        }

        composable(route = AppRoutes.ALL_GAME_HISTORY) {
            val allHistoryViewModel = screenViewModel {
                HomeViewModel(
                    repository = authRepository,
                    gameRepository = GameRepository.get(
                        context.applicationContext
                    ),
                    remoteGameRepository = RemoteGameRepository(
                        gamesService = authRepository.createGamesService()
                    ),
                    profileRepository = ProfileRepository(
                        service = authRepository.createProfileService()
                    )

                )
            }

            AllGameHistoryScreen(
                viewModel = allHistoryViewModel,
                onBack = {
                    scope.launch {
                        navController.safeBack(homeRoute(authRepository))
                    }
                },
                onReviewGame = { gameId ->
                    navController.navigate(
                        AppRoutes.gameHistory(gameId)
                    )
                }
            )
        }


        composable(route = AppRoutes.RESET_PASSWORD) {
            ResetPasswordScreen(
                viewModel = screenViewModel { ResetPasswordViewModel(authRepository) },
                onSendCode = { email ->
                    scope.launch {
                        pendingOtpStore.save(
                            email = email,
                            purpose = VerifyPurpose.RESET_PASSWORD.name
                        )

                        navController.navigate(
                            AppRoutes.verify(
                                email,
                                VerifyPurpose.RESET_PASSWORD
                            )
                        )
                    }
                },
                onBackToSignIn = { navController.safeBack(AppRoutes.LOGIN) }
            )
        }

        composable(route = AppRoutes.VERIFY) { entry ->
            val email = entry.arguments?.getString("email").orEmpty()
            val purpose = VerifyPurpose.valueOf(
                entry.arguments?.getString("purpose") ?: VerifyPurpose.RESET_PASSWORD.name
            )
            VerifyScreen(
                viewModel = screenViewModel {
                    VerifyViewModel(
                        repository = authRepository,
                        email = email,
                        purpose = purpose,
                        pendingOtpStore = pendingOtpStore
                    )
                },
                email = email,
                purpose = purpose,
                onBack = {
                    scope.launch {
                        pendingOtpStore.clear()
                        navController.safeBack(AppRoutes.LOGIN)
                    }
                },
                onVerified = {
                    if (purpose == VerifyPurpose.REGISTRATION) {
                        navController.navigate(AppRoutes.RATING) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                        }
                    } else {
                        navController.navigate(AppRoutes.NEW_PASSWORD) {
                            popUpTo(AppRoutes.VERIFY) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }

        composable(route = AppRoutes.RATING) {
            RatingScreen(
                viewModel = screenViewModel { RatingViewModel(authRepository) },
                onRated = { navController.goHomeThenPendingInvite(pendingInviteStore) }
            )
        }

        composable(route = AppRoutes.NEW_PASSWORD) {
            SetNewPasswordScreen(
                viewModel = screenViewModel { SetNewPasswordViewModel(authRepository) },
                onPasswordUpdated = {
                    navController.popBackStack(AppRoutes.LOGIN, inclusive = false)
                }
            )
        }

        composable(route = AppRoutes.HOME) {

            val homeViewModel = screenViewModel {
                HomeViewModel(
                    repository = authRepository,
                    gameRepository = GameRepository.get(
                        context.applicationContext
                    ),
                    remoteGameRepository = RemoteGameRepository(
                        gamesService = authRepository.createGamesService()
                    ),
                    profileRepository = ProfileRepository(
                        service = authRepository.createProfileService()
                    )

                )
            }

            HomeScreen(
                viewModel = homeViewModel,
                onStartChallenge = {openPlayGame()},
                onViewAllHistory = {navController.navigate(AppRoutes.ALL_GAME_HISTORY)},



                onReviewGame = { gameId ->
                    navController.navigate(
                        AppRoutes.gameHistory(gameId)
                    )
                },
                onLogoutSuccess = {
                    navController.goToLogin()
                },
                onOpenProfile = {
                    navController.navigate(AppRoutes.PROFILE)
                }
            )
        }

        composable(route = AppRoutes.GAME_SETUP
        ) {
            val setupViewModel = screenViewModel { GameSetupViewModel() }
            val setupState by setupViewModel.state.collectAsState()
            GameSetupScreen(
                state = setupState,
                onOpenTimeControls = { navController.navigate(AppRoutes.TIME_CONTROLS) },
                onStartMatch = {
                    scope.launch {
                        if (authRepository.isGuest() || !authRepository.isLoggedIn()
                        ) {
                            Toast.makeText(context,
                                "Online matchmaking requires a registered account.",
                                Toast.LENGTH_LONG
                            ).show()

                            navController.navigate(AppRoutes.LOGIN)
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
                        navController.navigate(
                            AppRoutes.matchmaking(minutes, increment)
                        )
                    }
                },


                onPlayWithFriend = { navController.navigate(AppRoutes.PLAY_WITH_FRIEND) },
                onPlayBots = { navController.navigate(AppRoutes.BOT_SETUP) },
                onPassAndPlay = {
                    navController.navigate(AppRoutes.PASS_AND_PLAY_SETUP)
                },
                onBack = {
                    scope.launch {
                        navController.safeBack(homeRoute(authRepository))
                    }
                }
            )
        }

        composable(route = AppRoutes.PASS_AND_PLAY_SETUP) {
            val setupViewModel = screenViewModel { PassAndPlaySetupViewModel() }
            val setupState by setupViewModel.state.collectAsState()

            PassAndPlaySetupScreen(
                state = setupState,
                onEvent = setupViewModel::onEvent,
                onStart = {
                    navController.navigate(
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
                onBack = { navController.safeBack(AppRoutes.GAME_SETUP) }
            )
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
                MatchmakingViewModel(
                    authRepository.createMatchmakingService()
                )
            }

            LaunchedEffect(matchmakingViewModel, minutes, increment) {
                matchmakingViewModel.startMatch(
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
                onBack = { navController.safeBack(AppRoutes.GAME_SETUP) }
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
                        popUpTo(AppRoutes.TIME_CONTROLS) { inclusive = true }
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
                    onConfirm = { navController.safeBack(AppRoutes.GAME_SETUP) },
                    onBack = { navController.safeBack(AppRoutes.GAME_SETUP) }
                )
            }
        }

        composable(
            route = AppRoutes.GAME_HISTORY,
            arguments = listOf(
                navArgument("gameId") {
                    type = NavType.StringType
                }
            )
        ) { entry ->

            val gameId = entry.arguments
                ?.getString("gameId")
                .orEmpty()

            val historyViewModel = screenViewModel {
                GameHistoryReviewViewModel(
                    repository = GameRepository.get(
                        context.applicationContext
                    ),
                    gameId = gameId,
                    authRepository = authRepository,
                    remoteGameRepository = RemoteGameRepository(
                        authRepository.createGamesService()
                    ),
                    chatRepository = ChatRepository.get(
                        context.applicationContext
                    )
                )
            }

            GameHistoryScreen(
                viewModel = historyViewModel,
                onBack = {
                    scope.launch {
                        navController.safeBack(homeRoute(authRepository))
                    }
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

            val gameViewModel =
                screenViewModel {
                    GameViewModel(

                        gameRepository = GameRepository.get(
                            context.applicationContext
                        ),

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
                },

                onNewGame = {
                    navController.navigate(
                        AppRoutes.GAME_SETUP
                    ) {
                        popUpTo(
                            AppRoutes.GAME
                        ) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(route = AppRoutes.PLAY_WITH_FRIEND) {
            PlayWithFriendScreen(
                onCreateMatchLink = { navController.navigate(AppRoutes.CREATE_INVITE) },
                onBack = { navController.safeBack(AppRoutes.GAME_SETUP) }
            )
        }

        composable(route = AppRoutes.CREATE_INVITE) {
            val createViewModel = screenViewModel {
                CreateInviteViewModel(context as Application)
            }

            CreateInviteScreen(
                viewModel = createViewModel,
                onEnterWaitingBoard = { gameId ->
                    navController.navigate(AppRoutes.onlineGame(gameId)) {
                        popUpTo(AppRoutes.CREATE_INVITE) { inclusive = true }
                    }
                },
                onBack = { navController.safeBack(AppRoutes.PLAY_WITH_FRIEND) }
            )
        }

        composable(
            route = AppRoutes.JOIN_INVITE,
            arguments = listOf(
                navArgument("code") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://caisaara.duckdns.org/play/{code}" }
            )
        ) { entry ->
            val code = entry.arguments?.getString("code").orEmpty()
            val joinViewModel = screenViewModel {
                JoinInviteViewModel(context as Application, code)
            }

            JoinInviteScreen(
                viewModel = joinViewModel,
                onGameReady = { gameId ->
                    navController.navigate(AppRoutes.onlineGame(gameId)) {
                        popUpTo(AppRoutes.JOIN_INVITE) { inclusive = true }
                    }
                },
                onLogin = {
                    pendingInviteStore.save(code)
                    navController.goToLogin()
                },
                onBack = {
                    pendingInviteStore.clear()
                    scope.launch {
                        navController.safeBack(homeRoute(authRepository))
                    }
                }
            )
        }

        composable(
            route = AppRoutes.ONLINE_GAME,
            arguments = listOf(
                navArgument("gameId") { type = NavType.StringType }
            )
        ) { entry ->
            val gameId = entry.arguments?.getString("gameId").orEmpty()
            val onlineViewModel = screenViewModel {
                OnlineGameViewModel(context as Application, gameId)
            }

            OnlineGameScreen(
                viewModel = onlineViewModel,
                onHome = { navController.goHome() },
                onNewOpponent = { minutes, increment ->
                    navController.navigate(AppRoutes.matchmaking(minutes, increment)) {
                        popUpTo(AppRoutes.ONLINE_GAME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRematchStarted = { rematchGameId, _, _ ->
                    navController.navigate(AppRoutes.onlineGame(rematchGameId)) {
                        popUpTo(AppRoutes.ONLINE_GAME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onStartAnalysis = {
                    navController.navigate(AppRoutes.gameAnalysis(gameId))
                }
            )
        }

        composable(
            route = AppRoutes.GAME_ANALYSIS,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
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
                onBack = { navController.safeBack(AppRoutes.ONLINE_GAME) }
            )
        }

        composable(route = AppRoutes.PROFILE) {
            val profileViewModel = screenViewModel {
                ProfileViewModel(
                    ProfileRepository(
                        authRepository.createProfileService()
                    )
                )
            }

            var isGuest by remember {
                mutableStateOf(false)
            }

            LaunchedEffect(Unit) {
                isGuest = authRepository.isGuest()
            }
            val currentBackStackEntry by navController.currentBackStackEntryAsState()

            val profileEntry = currentBackStackEntry
                ?.takeIf { it.destination.route == AppRoutes.PROFILE }

            val refreshRequested by (
                    profileEntry?.savedStateHandle
                        ?.getStateFlow("profile_refresh_requested", false)
                        ?: remember {
                            kotlinx.coroutines.flow.MutableStateFlow(false)
                        }
                    ).collectAsState()

            LaunchedEffect(refreshRequested, profileEntry) {
                if (refreshRequested) {
                    profileViewModel.loadProfile()
                    profileEntry?.savedStateHandle
                        ?.set("profile_refresh_requested", false)
                }
            }

            ProfileScreen(
                viewModel = profileViewModel,
                isGuest = isGuest,
                onBack = {
                    scope.launch {
                        navController.safeBack(homeRoute(authRepository))
                    }
                },
                onEditProfile = {
                        navController.navigate(AppRoutes.EDIT_PROFILE){
                            launchSingleTop = true
                        }

                }
            )
        }

        composable(route = AppRoutes.EDIT_PROFILE) {
            val editProfileViewModel = screenViewModel {
                ProfileViewModel(
                    ProfileRepository(
                        authRepository.createProfileService()
                    )
                )
            }

            EditProfileScreen(
                viewModel = editProfileViewModel,
                onBack = {
                    navController.safeBack(AppRoutes.PROFILE)
                },
                onSaved = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("profile_refresh_requested", true)
                    navController.safeBack(AppRoutes.PROFILE)
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
                        popUpTo(AppRoutes.BOT_SETUP) { inclusive = true }
                    }
                },
                onBack = { navController.safeBack(AppRoutes.GAME_SETUP) }
            )
        }

        composable(
            route = AppRoutes.BOT_GAME,
            arguments = listOf(
                navArgument("gameId") { type = NavType.StringType }
            )
        ) { entry ->
            val gameId = entry.arguments?.getString("gameId").orEmpty()
            val botViewModel = screenViewModel {
                BotGameViewModel(context as Application, gameId)
            }

            BotGameScreen(
                viewModel = botViewModel,
                onHome = { navController.goHome() },
                onNewGame = {
                    navController.navigate(AppRoutes.BOT_SETUP) {
                        popUpTo(AppRoutes.BOT_GAME) { inclusive = true }
                    }
                }
            )
        }

    }
}
