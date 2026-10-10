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

object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RESET_PASSWORD = "reset_password"
    const val VERIFY = "verify/{purpose}/{email}"
    const val NEW_PASSWORD = "new_password"
    const val HOME = "home"
    const val GAME_SETUP = "game_setup"
    const val GAME_HISTORY = "game_history/{gameId}"
    const val GAME = "game?resume={resume}" +
            "&white={white}" +
            "&black={black}" +
            "&minutes={minutes}" +
            "&flip={flip}" +
            "&undo={undo}"
    const val GUEST_HOME = "guest_options"
    const val RATING = "rating"
    const val CREATE_INVITE = "create_invite"
    const val JOIN_INVITE = "play/{code}"
    const val ONLINE_GAME = "online_game/{gameId}"

    fun verify(email: String, purpose: VerifyPurpose) =
        "verify/${purpose.name}/${Uri.encode(email)}"

    fun newGame(
        white: String,
        black: String,
        minutes: Int,
        flip: Boolean,
        undo: Boolean
    ): String {
        return "game" + "?resume=false" +
                "&white=${Uri.encode(white)}" +
                "&black=${Uri.encode(black)}" +
                "&minutes=$minutes" +
                "&flip=$flip" +
                "&undo=$undo"
    }

    fun resumeGame(): String {
        return "game" + "?resume=true" +
                "&white=" +
                "&black=" +
                "&minutes=10" +
                "&flip=false" +
                "&undo=true"
    }

    fun gameHistory(
        gameId: String
    ): String {
        return "game_history/${Uri.encode(gameId)}"
    }

    fun onlineGame(gameId: String) = "online_game/${Uri.encode(gameId)}"

    fun joinInvite(code: String) = "play/${Uri.encode(code)}"
}

@Composable
private inline fun <reified VM : ViewModel> screenViewModel(crossinline create: () -> VM): VM =
    viewModel(factory = viewModelFactory { initializer { create() } })

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

    val gameRepository = remember {
        GameRepository.get(context.applicationContext)
    }

    fun openPlayGame() {
        scope.launch {
            val ownerId = authRepository.getUsername()
                ?: authRepository.getGuestId()
                ?: "local-user"

            val unfinishedGame = gameRepository.getUnfinished(ownerId)

            if (unfinishedGame != null) {
                navController.navigate(AppRoutes.resumeGame())
            } else {
                navController.navigate(AppRoutes.GAME_SETUP)
            }
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
            val guestHomeViewModel =
                screenViewModel {
                    HomeViewModel(
                        repository = authRepository,
                        gameRepository = GameRepository.get(
                            context.applicationContext
                        )
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
                onPlayGame = {
                    openPlayGame()
                },
                onReviewGame = { gameId ->
                    navController.navigate(
                        AppRoutes.gameHistory(gameId)
                    )
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

                isLoggingOut = isLoggingOut
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
                onBackToSignIn = { navController.popBackStack() }
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
                        navController.popBackStack()
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
                    )
                )
            }


            HomeScreen(
                viewModel = homeViewModel,
                onStartChallenge = {
                    openPlayGame()
                },
                onPlayWithFriend = {
                    navController.navigate(AppRoutes.CREATE_INVITE)
                },
                onReviewGame = { gameId ->
                    navController.navigate(
                        AppRoutes.gameHistory(gameId)
                    )
                },
                onLogoutSuccess = {
                    navController.goToLogin()
                }
            )
        }

        composable(
            route = AppRoutes.GAME_SETUP
        ) {

            val setupViewModel =
                screenViewModel {
                    GameSetupViewModel()
                }

            GameSetupScreen(
                viewModel = setupViewModel,
                onStartGame = {
                        white,
                        black,
                        minutes,
                        flip,
                        undo ->
                    navController.navigate(
                        AppRoutes.newGame(
                            white = white,
                            black = black,
                            minutes = minutes,
                            flip = flip,
                            undo = undo
                        )
                    )
                },
                onBack = {
                    navController.popBackStack()
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
            val gameId = entry.arguments
                    ?.getString("gameId")
                    .orEmpty()
            val historyViewModel = screenViewModel {
                    GameHistoryReviewViewModel(
                        repository = GameRepository.get(
                                context.applicationContext
                            ),
                        gameId = gameId
                    )
                }
                ?.getString("gameId")
                .orEmpty()

            val historyViewModel = screenViewModel {
                GameHistoryReviewViewModel(
                    repository = GameRepository.get(
                        context.applicationContext
                    ),

                    gameId = gameId
                )
            }

            GameHistoryScreen(
                viewModel = historyViewModel,
                onBack = {
                    navController.popBackStack()
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
                }
            )
        ) { entry ->
            val resume = entry.arguments?.getBoolean("resume") ?: false
            val white = entry.arguments?.getString("white").orEmpty()
            val black = entry.arguments?.getString("black").orEmpty()
            val minutes = entry.arguments?.getInt("minutes") ?: 10
            val flip = entry.arguments?.getBoolean("flip") ?: false
            val undo = entry.arguments?.getBoolean("undo") ?: true

            val gameViewModel =
                screenViewModel {
                    GameViewModel(
                        gameRepository = GameRepository.get(context.applicationContext),
                        gameRepository = GameRepository.get(
                            context.applicationContext
                        ),

                        authRepository = authRepository,
                        resumeGame = resume,
                        newWhiteName = white,
                        newBlackName = black,
                        newTimeMinutes = minutes,
                        newBoardFlipped = flip,
                        newUndoEnabled = undo
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

        composable(route = AppRoutes.CREATE_INVITE) {
            val createViewModel = screenViewModel {
                CreateInviteViewModel(context as Application)
            }

            CreateInviteScreen(
                viewModel = createViewModel,
                onGameReady = { gameId ->
                    navController.navigate(AppRoutes.onlineGame(gameId)) {
                        popUpTo(AppRoutes.CREATE_INVITE) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
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
                    if (!navController.popBackStack()) navController.goHome()
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
                onHome = { navController.goHome() }
            )
        }
    }
}