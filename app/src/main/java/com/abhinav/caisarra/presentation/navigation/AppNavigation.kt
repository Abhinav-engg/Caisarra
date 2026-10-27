package com.abhinav.caisarra.presentation.navigation
import com.abhinav.caisarra.data.local.RatingPreferencesStore
import com.abhinav.caisarra.data.local.SessionExpiredEvent
import com.abhinav.caisarra.presentation.tournament.CreateTournamentScreen
import com.abhinav.caisarra.presentation.tournament.CreateTournamentViewModel
import com.abhinav.caisarra.presentation.tournament.TournamentDetailScreen
import com.abhinav.caisarra.presentation.tournament.TournamentDetailViewModel
import com.abhinav.caisarra.presentation.tournament.TournamentListScreen
import com.abhinav.caisarra.presentation.tournament.TournamentListViewModel
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
import androidx.activity.ComponentActivity
import com.abhinav.caisarra.presentation.common.ClickGuard
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.navigation.NavOptionsBuilder
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
import com.abhinav.caisarra.presentation.screens.AllGameHistoryScreen
import com.abhinav.caisarra.data.repository.ProfileRepository
import com.abhinav.caisarra.data.repository.RatingRepository
import com.abhinav.caisarra.presentation.screens.ProfileScreen
import com.abhinav.caisarra.presentation.viewmodel.ProfileViewModel
import com.abhinav.caisarra.data.repository.ChatRepository
import com.abhinav.caisarra.presentation.game.screens.MatchmakingScreen
import com.abhinav.caisarra.presentation.game.viewmodel.MatchmakingViewModel
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhinav.caisarra.data.repository.GameAnalysisRepository
import com.abhinav.caisarra.presentation.screens.BotSetupScreen
import com.abhinav.caisarra.presentation.screens.BotGameScreen
import com.abhinav.caisarra.presentation.screens.BotHistoryScreen
import com.abhinav.caisarra.presentation.game.viewmodel.BotSetupViewModel
import com.abhinav.caisarra.presentation.game.viewmodel.BotHistoryViewModel
import com.abhinav.caisarra.presentation.game.BotGameViewModel
import com.abhinav.caisarra.presentation.game.screens.PassAndPlaySetupScreen
import com.abhinav.caisarra.presentation.game.viewmodel.PassAndPlaySetupViewModel
import com.abhinav.caisarra.presentation.game.screens.GameAnalysisScreen
import com.abhinav.caisarra.presentation.game.viewmodel.GameAnalysisViewModel
import com.abhinav.caisarra.presentation.screens.EditProfileScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import com.abhinav.caisarra.presentation.invite.FriendsViewModel
import com.abhinav.caisarra.presentation.invite.PlayWithFriendHomeScreen
import com.abhinav.caisarra.presentation.invite.PlayWithFriendScreen
import com.abhinav.caisarra.data.location.LocationProvider
import com.abhinav.caisarra.data.repository.FriendsRepository
import com.abhinav.caisarra.data.repository.NearbyRepository
import com.abhinav.caisarra.presentation.nearby.NearbyViewModel
import com.abhinav.caisarra.presentation.screens.MenuScreen
import com.abhinav.caisarra.presentation.screens.StatsScreen
import com.abhinav.caisarra.presentation.viewmodel.StatsViewModel
import com.abhinav.caisarra.presentation.screens.ThemeScreen
import com.abhinav.caisarra.presentation.screens.MessagesScreen
import com.abhinav.caisarra.presentation.screens.LeaderboardScreen
import com.abhinav.caisarra.presentation.screens.DeleteAccountScreen
import com.abhinav.caisarra.presentation.screens.SettingsDetailScreen
import com.abhinav.caisarra.presentation.screens.NearbyScreen
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.repository.CommunityRepository
import com.abhinav.caisarra.presentation.community.CommunityScreen
import com.abhinav.caisarra.presentation.community.CommunityViewModel
import com.abhinav.caisarra.presentation.screens.PuzzlesScreen
import com.abhinav.caisarra.presentation.screens.DailyPuzzleScreen
import com.abhinav.caisarra.presentation.screens.RushScreen
import com.abhinav.caisarra.presentation.puzzles.PuzzlesHubViewModel
import com.abhinav.caisarra.presentation.puzzles.DailyPuzzleViewModel
import com.abhinav.caisarra.presentation.puzzles.RushViewModel
import com.abhinav.caisarra.data.repository.PuzzleRepository
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.data.repository.ClubRepository
import com.abhinav.caisarra.presentation.club.ClubScreen
import com.abhinav.caisarra.presentation.club.ClubViewModel
import com.abhinav.caisarra.presentation.club.ClubDetailScreen
import com.abhinav.caisarra.presentation.viewmodel.LeaderboardViewModel
import com.abhinav.caisarra.presentation.directmessage.DirectMessageScreen
import com.abhinav.caisarra.presentation.directmessage.DirectMessagesListScreen
import androidx.activity.compose.LocalActivity
import androidx.lifecycle.ViewModelProvider
import com.abhinav.caisarra.presentation.watch.WatchScreen
import com.abhinav.caisarra.presentation.watch.WatchViewModel
import com.abhinav.caisarra.presentation.watch.SpectatorGameScreen
import com.abhinav.caisarra.presentation.watch.SpectatorGameViewModel

object AppRoutes {
    const val WATCH = "watch"
    const val SPECTATE_GAME = "spectate/{watchCode}"
    fun spectateGame(watchCode: String): String =
        "spectate/${Uri.encode(watchCode.trim())}"

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
    const val BOT_HISTORY = "bot_history"
    const val PASS_AND_PLAY_SETUP = "pass_and_play_setup"
    const val TOURNAMENTS = "tournaments"
    const val CREATE_TOURNAMENT = "create_tournament"
    const val PLAY_WITH_FRIEND_HOME = "play_with_friend_home"
    const val TOURNAMENT_DETAIL = "tournament/{id}?code={code}"
    const val NEARBY = "nearby"
    const val COMMUNITY = "community"
    const val DIRECT_MESSAGES_LIST = "direct_messages_list"
    const val PUZZLES = "puzzles"
    const val DAILY_PUZZLE = "daily_puzzle"
    const val PUZZLE_RUSH = "puzzle_rush?mode={mode}"
    const val DIRECT_MESSAGE = "direct_message/{userId}/{username}"

    fun puzzleRush(mode: String = "3min") = "puzzle_rush?mode=$mode"
    const val SOCIAL = "social"
    const val MENU = "menu"
    const val STATS = "stats?username={username}&mode={mode}"
    fun stats(username: String? = null, mode: String? = null): String {
        val queryParts = mutableListOf<String>()
        if (!username.isNullOrBlank()) queryParts.add("username=${Uri.encode(username.trim())}")
        if (!mode.isNullOrBlank()) queryParts.add("mode=${Uri.encode(mode.trim())}")
        return if (queryParts.isEmpty()) "stats" else "stats?${queryParts.joinToString("&")}"
    }
    const val THEME = "theme"
    const val MESSAGES = "messages"
    const val CLUBS = "clubs"
    const val CLUB_DETAIL = "club_detail/{clubId}"

    fun clubDetail(clubId: String): String =
        "club_detail/${Uri.encode(clubId)}"
    const val LEADERBOARD = "leaderboard"
    const val DELETE_ACCOUNT = "delete_account"
    const val SETTINGS_DETAIL = "settings_detail"

    fun tournamentDetail(id: String, inviteCode: String? = null): String =
        "tournament/${Uri.encode(id)}" +
                (inviteCode?.let { "?code=${Uri.encode(it)}" } ?: "")

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

    fun gameHistory(
        gameId: String
    ): String {
        return "game_history/${Uri.encode(gameId)}"
    }
    fun onlineGame(gameId: String) = "online_game/${Uri.encode(gameId)}"

    fun botGame(gameId: String) = "bot_game/${Uri.encode(gameId)}"
    fun botHistory() = BOT_HISTORY

    fun joinInvite(code: String) = "play/${Uri.encode(code)}"
    fun matchmaking(minutes: Int, incrementSeconds: Int) = "matchmaking/$minutes/$incrementSeconds"
    fun gameAnalysis(gameId: String) = "game_analysis/${Uri.encode(gameId)}"

}

@Composable
private inline fun <reified VM : ViewModel> screenViewModel(crossinline create: () -> VM): VM =
    viewModel(factory = viewModelFactory { initializer { create() } })

private fun NavController.isResumed(): Boolean =
    currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

private fun NavController.navigateOnce(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    if (isResumed()) navigate(route, builder)
}


private fun NavController.safeBack(fallbackRoute: String) {
    if (!ClickGuard.canClick() || !isResumed()) return
    if (currentDestination?.route == fallbackRoute) return
    val fallbackExists = runCatching { getBackStackEntry(fallbackRoute) }.isSuccess
    if (fallbackExists) {
        popBackStack(fallbackRoute, inclusive = false)
    } else if (!popBackStack()) {
        navigate(fallbackRoute) {
            popUpTo(graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }
}

private fun NavController.safePop(fallbackRoute: String = AppRoutes.HOME) {
    if (!ClickGuard.canClick() || !isResumed()) return
    if (!popBackStack() && currentDestination?.route != fallbackRoute) {
        navigate(fallbackRoute) {
            popUpTo(graph.id) { inclusive = true }
            launchSingleTop = true
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
    val isGuestUser by authRepository.isGuestFlow.collectAsState(initial = false)

    LaunchedEffect(Unit) {
        val pending = pendingOtpStore.getPendingVerification()
        val isGuest = authRepository.isGuest()
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
            isGuest -> AppRoutes.GUEST_HOME
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
    var friendsBackRoute by remember {
        mutableStateOf(AppRoutes.GAME_SETUP)
    }
    var inviteBackRoute by remember {
        mutableStateOf(AppRoutes.PLAY_WITH_FRIEND_HOME)
    }

    LaunchedEffect(Unit) {
        SessionExpiredEvent.events.collect {
            navController.navigate(AppRoutes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    fun openPlayGame() {
        navController.navigateOnce(AppRoutes.GAME_SETUP)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    val bottomNavRoutes = remember {
        setOf(
            AppRoutes.HOME,
            AppRoutes.PUZZLES,
            AppRoutes.NEARBY,
            AppRoutes.COMMUNITY,
            AppRoutes.SOCIAL,
            AppRoutes.MENU
        )
    }
    val showBottomBar =
        !isGuestUser && currentRoute in bottomNavRoutes


    Scaffold(
        containerColor = Color(0xFF080D13),
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigateTo = { route ->
                        val homeRoute = if (isGuestUser) AppRoutes.GUEST_HOME else AppRoutes.HOME
                        val targetRoute = if (route == AppRoutes.HOME) {
                            homeRoute
                        } else {
                            route
                        }
                        if (currentRoute != targetRoute) {
                            navController.navigate(targetRoute) {
                                popUpTo(homeRoute) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = start,
            modifier = Modifier.padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
        composable(route = AppRoutes.LOGIN) {
            LoginScreen(
                viewModel = screenViewModel { LoginViewModel(authRepository) },
                onLoginSuccess = { navController.goHomeThenPendingInvite(pendingInviteStore) },
                onForgotPasswordClick = { navController.navigateOnce(AppRoutes.RESET_PASSWORD) },
                onSignUpClick = { navController.navigateOnce(AppRoutes.REGISTER) },
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
                    ),
                    ratingPreferenceStore = RatingPreferencesStore.get(
                        context.applicationContext
                    ),
                    puzzleRepository = PuzzleRepository(
                        service = authRepository.createPuzzleService()
                    )
                )
            }


            val guestHomeState by guestHomeViewModel.state.collectAsState()
            val activity = LocalContext.current as? ComponentActivity
            androidx.activity.compose.BackHandler { activity?.finish() }
            LaunchedEffect(Unit) {
                if (authRepository.isLoggedIn()) {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.GUEST_HOME) { inclusive = true }
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
                    navController.navigateOnce(
                        AppRoutes.gameHistory(gameId)
                    )
                },
                onViewAllHistory = {
                    navController.navigateOnce(AppRoutes.ALL_GAME_HISTORY)
                },
                onSignIn = {
                    navController.goToLogin()
                },
                onSignUp = {
                    navController.navigateOnce(
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
                    ),
                    ratingPreferenceStore = RatingPreferencesStore.get(
                        context.applicationContext
                    ),
                    puzzleRepository = PuzzleRepository(
                        service = authRepository.createPuzzleService()
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
                    navController.navigateOnce(
                        AppRoutes.gameHistory(gameId)
                    )
                }
            )
        }
        composable(route = AppRoutes.COMMUNITY) {
            val tokenManager = remember {
                TokenManager.get(context)
            }

            val accessToken by tokenManager.accessTokenFlow
                .collectAsState(initial = null)
            var profileUsername by remember(accessToken) {
                mutableStateOf<String?>(null)
            }

            var profileDisplayName by remember(accessToken) {
                mutableStateOf<String?>(null)
            }

            var profileAvatarUrl by remember(accessToken) {
                mutableStateOf<String?>(null)
            }

            LaunchedEffect(accessToken) {
                if (accessToken.isNullOrBlank()) {
                    profileUsername = null
                    profileDisplayName = null
                    profileAvatarUrl = null
                } else {
                    runCatching {
                        val profile = ProfileRepository(
                            service = RetrofitInstance.createProfileService(tokenManager)
                        ).getMyProfile()

                        profileUsername = profile.username
                    }
                }
            }
            val communityViewModel = screenViewModel {
                CommunityViewModel(
                    repository = CommunityRepository(
                        RetrofitInstance.createCommunityService(
                            tokenManager
                        )
                    ),
                    remoteGameRepository = RemoteGameRepository(
                        RetrofitInstance.createGamesService(
                            tokenManager
                        )
                    )
                )
            }


            CommunityScreen(
                viewModel = communityViewModel,
                isRegisteredUser = !accessToken.isNullOrBlank(),
                currentUsername = profileUsername,
                accessToken = accessToken,
                currentAvatarUrl = profileAvatarUrl,
                currentDisplayName = profileDisplayName,

                onBack = {
                    scope.launch {
                        navController.safeBack(homeRoute(authRepository))
                    }
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
                    ),
                    ratingPreferenceStore = RatingPreferencesStore.get(
                        context.applicationContext
                    ),
                    puzzleRepository = PuzzleRepository(
                        service = authRepository.createPuzzleService()
                    )
                )
            }

            val activity = LocalContext.current as? ComponentActivity
            androidx.activity.compose.BackHandler { activity?.finish() }

            HomeScreen(
                viewModel = homeViewModel,
                onStartChallenge = { openPlayGame() },
                onStartQuickPlay = {
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
                        navController.navigateOnce(
                            AppRoutes.matchmaking(10, 0)
                        )
                    }
                },
                onOpenActiveGame = { gameId ->
                    navController.navigateOnce(
                        AppRoutes.onlineGame(gameId)
                    )
                },
                onViewAllHistory = { navController.navigateOnce(AppRoutes.ALL_GAME_HISTORY) },



                onReviewGame = { gameId ->
                    navController.navigateOnce(
                        AppRoutes.gameHistory(gameId)
                    )
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
                    navController.navigateOnce(AppRoutes.PROFILE)
                },
                onStreakClick = {
                    navController.navigateOnce(AppRoutes.DAILY_PUZZLE)
                },
                onOpenCommunity = {
                    navController.navigate(AppRoutes.COMMUNITY)
                },
                onStatsClick = { mode ->
                    navController.navigateOnce(AppRoutes.stats(mode = mode.ratingKey))
                }
            )
        }

        composable(route = AppRoutes.GAME_SETUP
        ) {
            val setupViewModel = screenViewModel { GameSetupViewModel() }
            val setupState by setupViewModel.state.collectAsState()
            GameSetupScreen(
                state = setupState,
                onOpenTimeControls = { navController.navigateOnce(AppRoutes.TIME_CONTROLS) },
                onStartMatch = {
                    scope.launch {
                        if (authRepository.isGuest() || !authRepository.isLoggedIn()
                        ) {
                            Toast.makeText(context,
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
                    scope.launch {
                        if (
                            authRepository.isGuest() ||
                            !authRepository.isLoggedIn()
                        ) {
                            Toast.makeText(
                                context,
                                "Friends require a registered account. Please sign in.",
                                Toast.LENGTH_LONG
                            ).show()

                            navController.navigateOnce(AppRoutes.LOGIN)
                            return@launch
                        }

                        friendsBackRoute = AppRoutes.GAME_SETUP

                        navController.navigateOnce(
                            AppRoutes.PLAY_WITH_FRIEND_HOME
                        ) {
                            launchSingleTop = true
                        }
                    }
                },


                onTournaments = {
                    scope.launch {
                        if (authRepository.isGuest() || !authRepository.isLoggedIn()) {
                            Toast.makeText(
                                context,
                                "Tournaments require a registered account.",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.navigateOnce(AppRoutes.LOGIN)
                            return@launch
                        }
                        navController.navigateOnce(AppRoutes.TOURNAMENTS)
                    }
                },
                onPlayBots = { navController.navigateOnce(AppRoutes.BOT_SETUP) },
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
            val setupViewModel = screenViewModel { PassAndPlaySetupViewModel() }
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
            composable(
                route = AppRoutes.DIRECT_MESSAGE,
                arguments = listOf(
                    navArgument("userId") {
                        type = NavType.LongType
                    },
                    navArgument("username") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->

                val userId = backStackEntry.arguments
                    ?.getLong("userId") ?: 0L

                val username = backStackEntry.arguments
                    ?.getString("username") ?: "Player"


                var isLeavingScreen by remember(backStackEntry) {
                    mutableStateOf(false)
                }

                DirectMessageScreen(
                    userId = userId,
                    username = username,
                    onBack = {
                        if (!isLeavingScreen) {
                            isLeavingScreen = true

                            val popped = navController.popBackStack()

                            if (!popped) {
                                navController.navigate(AppRoutes.HOME) {
                                    popUpTo(navController.graph.id) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                )
            }
            composable(
                route = AppRoutes.DIRECT_MESSAGES_LIST
            ) {
                var isLeaving by remember {
                    mutableStateOf(false)
                }

                androidx.activity.compose.BackHandler {
                    if (!isLeaving) {
                        isLeaving = true

                        val popped = navController.popBackStack()

                        if (!popped) {
                            navController.navigate(AppRoutes.MENU) {
                                launchSingleTop = true
                            }
                        }
                    }
                }

                DirectMessagesListScreen(
                    onBack = {
                        if (!isLeaving) {
                            isLeaving = true

                            val popped = navController.popBackStack()

                            if (!popped) {
                                navController.navigate(AppRoutes.MENU) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    onOpenChat = { userId, username ->
                        navController.navigate(
                            "${AppRoutes.DIRECT_MESSAGE.substringBefore("/{")}/$userId/${Uri.encode(username)}"
                        )
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
                    navController.navigateOnce(
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
                val friendsViewModel = screenViewModel {
                    FriendsViewModel(context as Application)
                }

                androidx.activity.compose.BackHandler {
                    navController.safeBack(friendsBackRoute)
                }

                PlayWithFriendScreen(
                    viewModel = friendsViewModel,
                    onCreateMatchLink = {
                        inviteBackRoute = AppRoutes.PLAY_WITH_FRIEND
                        navController.navigateOnce(AppRoutes.CREATE_INVITE)
                    },
                    onBack = {
                        navController.safeBack(friendsBackRoute)
                    },
                    onDirectMessage = { userId, username ->
                        scope.launch {
                            val isGuest = authRepository.isGuest()
                            val isLoggedIn = authRepository.isLoggedIn()

                            if (isGuest || !isLoggedIn) {
                                Toast.makeText(
                                    context,
                                    "Direct messages require a registered account.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                navController.navigate(
                                    "direct_message/$userId/${Uri.encode(username)}"
                                )
                            }
                        }
                    }

                )
            }

            composable(route = AppRoutes.PLAY_WITH_FRIEND_HOME) {
                androidx.activity.compose.BackHandler {
                    navController.safeBack(friendsBackRoute)
                }

                PlayWithFriendHomeScreen(
                    onInviteFriends = {
                        navController.navigateOnce(
                            AppRoutes.PLAY_WITH_FRIEND
                        )
                    },
                    onCreateChallengeLink = {
                        inviteBackRoute = AppRoutes.PLAY_WITH_FRIEND_HOME
                        navController.navigateOnce(AppRoutes.CREATE_INVITE)
                    },
                    onBack = {
                        navController.safeBack(friendsBackRoute)
                    }
                )
            }



            composable(route = AppRoutes.CREATE_INVITE) {
                val createViewModel = screenViewModel {
                    CreateInviteViewModel(context as Application)
                }

                androidx.activity.compose.BackHandler {
                    navController.safeBack(inviteBackRoute)
                }

                CreateInviteScreen(
                    viewModel = createViewModel,

                    onEnterWaitingBoard = { gameId ->
                        navController.navigate(AppRoutes.onlineGame(gameId)) {
                            popUpTo(AppRoutes.CREATE_INVITE) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },

                    onBack = {
                        navController.safeBack(inviteBackRoute)
                    }
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

        composable(route = AppRoutes.TOURNAMENTS) {
            val listViewModel = screenViewModel {
                TournamentListViewModel(context as Application)
            }

            TournamentListScreen(
                viewModel = listViewModel,
                onCreate = { navController.navigate(AppRoutes.CREATE_TOURNAMENT) },
                onOpenTournament = { id, code ->
                    navController.navigate(AppRoutes.tournamentDetail(id, code))
                },
                onBack = { navController.safeBack(AppRoutes.GAME_SETUP) }
            )
        }

        composable(route = AppRoutes.CREATE_TOURNAMENT) {
            val createViewModel = screenViewModel {
                CreateTournamentViewModel(context as Application)
            }

            CreateTournamentScreen(
                viewModel = createViewModel,
                onCreated = { id ->
                    navController.navigate(AppRoutes.tournamentDetail(id)) {
                        popUpTo(AppRoutes.CREATE_TOURNAMENT) { inclusive = true }
                    }
                },
                onBack = { navController.safeBack(AppRoutes.TOURNAMENTS) }
            )
        }

        composable(
            route = AppRoutes.TOURNAMENT_DETAIL,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("code") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            val code = entry.arguments?.getString("code")
            val detailViewModel = screenViewModel {
                TournamentDetailViewModel(context as Application, id, code)
            }

            TournamentDetailScreen(
                viewModel = detailViewModel,
                onOpenGame = { gameId -> navController.navigate(AppRoutes.onlineGame(gameId)) },
                onBack = { navController.safeBack(AppRoutes.TOURNAMENTS) }
            )
        }

        composable(
            route = AppRoutes.ONLINE_GAME,
            arguments = listOf(
                navArgument("gameId") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "caisarra://game/online/{gameId}" },
                navDeepLink { uriPattern = "https://caisaara.duckdns.org/game/{gameId}" }
            )
        ) { entry ->
            val gameId = entry.arguments?.getString("gameId").orEmpty()
            val onlineViewModel = screenViewModel {
                OnlineGameViewModel(context as Application, gameId)
            }
            val analysisViewModel = screenViewModel {
                GameAnalysisViewModel(
                    repository = GameAnalysisRepository(authRepository.createAnalysisService()),
                    gameId = gameId
                )
            }

            OnlineGameScreen(
                viewModel = onlineViewModel,
                analysisViewModel = analysisViewModel,
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
                onBack = { navController.safePop() }
            )
        }

        composable(route = AppRoutes.PROFILE) {
            val profileViewModel = screenViewModel {
                ProfileViewModel(
                    repository = ProfileRepository(
                        authRepository.createProfileService()
                    ),
                    ratingRepository = RatingRepository(
                        authRepository.createRatingService()
                    )
                )
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
                isGuest = isGuestUser,
                onBack = {
                    scope.launch {
                        navController.safeBack(homeRoute(authRepository))
                    }
                },
                onEditProfile = {
                    navController.navigate(AppRoutes.EDIT_PROFILE){
                        launchSingleTop = true
                    }
                },
                onStatsClick = { mode ->
                    navController.navigateOnce(AppRoutes.stats(mode = mode.ratingKey))
                }
            )
        }

        composable(route = AppRoutes.EDIT_PROFILE) {
            val editProfileViewModel = screenViewModel {
                ProfileViewModel(
                    repository = ProfileRepository(
                        authRepository.createProfileService()
                    ),
                    ratingRepository = RatingRepository(
                        authRepository.createRatingService()
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

        composable(route = AppRoutes.NEARBY) {
            val nearbyViewModel = screenViewModel {
                NearbyViewModel(
                    repository = NearbyRepository(authRepository.createNearbyService()),
                    locationProvider = LocationProvider(context)
                )
            }

            NearbyScreen(
                viewModel = nearbyViewModel,
                onBack = { navController.safeBack(AppRoutes.HOME) },
                onChallenge = { player ->
                    scope.launch {
                        if (authRepository.isGuest() || !authRepository.isLoggedIn()) {
                            Toast.makeText(
                                context,
                                "Challenging players requires a registered account.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            inviteBackRoute = AppRoutes.NEARBY
                            navController.navigate(AppRoutes.CREATE_INVITE)
                        }
                    }
                },
                onAddFriend = { player ->
                    scope.launch {
                        if (authRepository.isGuest() || !authRepository.isLoggedIn()) {
                            Toast.makeText(
                                context,
                                "Adding friends requires a registered account.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }
                        val friendsRepo = FriendsRepository(context)
                        friendsRepo.sendFriendRequest(player.username)
                            .onSuccess {
                                Toast.makeText(
                                    context,
                                    "Friend request sent to ${player.username}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .onFailure { error ->
                                Toast.makeText(
                                    context,
                                    error.message ?: "Could not send friend request",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                },
                onChat = { player ->
                    scope.launch {
                        if (authRepository.isGuest() || !authRepository.isLoggedIn()) {
                            Toast.makeText(
                                context,
                                "Direct messages require a registered account.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            navController.navigate(
                                "direct_message/${player.userId}/${Uri.encode(player.username)}"
                            )
                        }
                    }
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
                onViewHistory = {
                    navController.navigateOnce(AppRoutes.BOT_HISTORY)
                },
                onBack = { navController.safeBack(AppRoutes.GAME_SETUP) }
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
                        popUpTo(AppRoutes.BOT_HISTORY) { inclusive = true }
                    }
                },
                onBack = { navController.safeBack(AppRoutes.BOT_SETUP) }
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

        composable(route = AppRoutes.PUZZLES) {
            val tokenManager = remember { TokenManager.get(context) }
            val hubViewModel = screenViewModel {
                PuzzlesHubViewModel(
                    PuzzleRepository(
                        RetrofitInstance.createPuzzleService(tokenManager)
                    )
                )
            }
            PuzzlesScreen(
                viewModel = hubViewModel,
                onPlayDailyPuzzle = {
                    navController.navigateOnce(AppRoutes.DAILY_PUZZLE) {
                        launchSingleTop = true
                    }
                },
                onPlayPuzzleRush = { mode ->
                    navController.navigate(AppRoutes.puzzleRush(mode)) {
                        popUpTo(AppRoutes.PUZZLES) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = AppRoutes.DAILY_PUZZLE) {
            val tokenManager = remember { TokenManager.get(context) }
            val dailyViewModel = screenViewModel {
                DailyPuzzleViewModel(
                    PuzzleRepository(
                        RetrofitInstance.createPuzzleService(tokenManager)
                    )
                )
            }
            DailyPuzzleScreen(
                viewModel = dailyViewModel,
                onBack = {
                    navController.safePop()
                }
            )
        }

        composable(
            route = AppRoutes.PUZZLE_RUSH,
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "3min"
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "3min"
            val tokenManager = remember { TokenManager.get(context) }
            val rushViewModel = screenViewModel {
                RushViewModel(
                    puzzleRepository = PuzzleRepository(
                        RetrofitInstance.createPuzzleService(tokenManager)
                    ),
                    mode = mode
                )
            }
            RushScreen(
                viewModel = rushViewModel,
                onBack = {
                    navController.safePop()
                }
            )
        }

        composable(route = AppRoutes.SOCIAL) {
            LaunchedEffect(Unit) {
                navController.navigate(AppRoutes.COMMUNITY) {
                    popUpTo(AppRoutes.SOCIAL) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable(route = AppRoutes.MENU) {
            MenuScreen(
                onBack = {
                    scope.launch {
                        navController.safeBack(homeRoute(authRepository))
                    }
                },
                onSettingsClick = {
                    navController.navigateOnce(AppRoutes.SETTINGS_DETAIL)
                },
                onProfileClick = {
                    navController.navigateOnce(AppRoutes.PROFILE)
                },
                onStatsClick = {
                    navController.navigateOnce(AppRoutes.stats())
                },
                onGameHistoryClick = {
                    navController.navigateOnce(AppRoutes.ALL_GAME_HISTORY)
                },
                onThemeClick = {
                    navController.navigateOnce(AppRoutes.THEME)
                },

                onFriendsClick = {
                    scope.launch {
                        if (
                            authRepository.isGuest() ||
                            !authRepository.isLoggedIn()
                        ) {
                            Toast.makeText(
                                context,
                                "Friends require a registered account. Please sign in.",
                                Toast.LENGTH_SHORT
                            ).show()

                            navController.navigateOnce(AppRoutes.LOGIN)
                        } else {
                            friendsBackRoute = AppRoutes.MENU

                            navController.navigateOnce(
                                AppRoutes.PLAY_WITH_FRIEND
                            ) {
                                launchSingleTop = true
                            }
                        }
                    }
                },
                onDirectMessagesClick = {
                    scope.launch {
                        val isGuest = authRepository.isGuest()
                        val isLoggedIn = authRepository.isLoggedIn()

                        if (isGuest || !isLoggedIn) {
                            Toast.makeText(
                                context,
                                "Direct Messages require a registered account.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            navController.navigateOnce(
                                AppRoutes.DIRECT_MESSAGES_LIST
                            ) {
                                launchSingleTop = true
                            }
                        }
                    }
                },

                onClubsClick = {
                    navController.navigateOnce(AppRoutes.CLUBS)
                },
                onLeaderboardClick = {
                    navController.navigateOnce(AppRoutes.LEADERBOARD)
                },
                onWatchClick = {
                    scope.launch {
                        if (authRepository.isGuest() || !authRepository.isLoggedIn()) {
                            Toast.makeText(
                                context,
                                "Spectating requires a registered account. Please sign in.",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigateOnce(AppRoutes.LOGIN)
                        } else {
                            navController.navigateOnce(AppRoutes.WATCH)
                        }
                    }
                },
                onDeleteAccountClick = {
                    navController.navigateOnce(AppRoutes.DELETE_ACCOUNT)
                },
                onLogoutClick = {
                    scope.launch {
                        authRepository.logout()
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
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
                            authRepository.createRatingService()
                        ),
                        profileRepository = ProfileRepository(
                            authRepository.createProfileService()
                        ),
                        initialUsername = usernameArg,
                        initialMode = modeArg
                    )
                }

                StatsScreen(
                    viewModel = statsViewModel,
                    onBack = {
                        navController.safeBack(AppRoutes.MENU)
                    },
                    onGameClick = { gameId ->
                        navController.navigateOnce(
                            AppRoutes.gameHistory(gameId)
                        )
                    },
                    onSignIn = {
                        navController.navigateOnce(AppRoutes.LOGIN)
                    }
                )
            }

            composable(route = AppRoutes.THEME) {
                androidx.activity.compose.BackHandler {
                    navController.safeBack(AppRoutes.MENU)
                }

                ThemeScreen(
                    onBack = {
                        navController.safeBack(AppRoutes.MENU)
                    }
                )
            }


            composable(route = AppRoutes.MESSAGES) {
            MessagesScreen(
                onBack = { navController.safePop() }
            )
        }


            composable(route = AppRoutes.CLUBS) {
                val tokenManager = remember(context) {
                    TokenManager.get(context)
                }

                val clubRepository = remember(tokenManager) {
                    ClubRepository(
                        RetrofitInstance.createClubService(tokenManager)
                    )
                }

                val clubViewModel: ClubViewModel = viewModel(
                    key = "club-list",
                    factory = viewModelFactory {
                        initializer {
                            ClubViewModel(clubRepository)
                        }
                    }
                )

                var isRegisteredUser by remember {
                    mutableStateOf(false)
                }

                LaunchedEffect(authRepository) {
                    isRegisteredUser = runCatching {
                        authRepository.isLoggedIn() &&
                                !authRepository.isGuest()
                    }.getOrDefault(false)
                }

                ClubScreen(
                    viewModel = clubViewModel,
                    isRegisteredUser = isRegisteredUser,
                    onBack = {
                        navController.safeBack(AppRoutes.MENU)
                    },
                    onOpenClub = { clubId ->
                        navController.navigateOnce(
                            AppRoutes.clubDetail(clubId)
                        )
                    },
                    onSignIn = {
                        navController.navigateOnce(AppRoutes.LOGIN)
                    }
                )
            }

            composable(
                route = AppRoutes.CLUB_DETAIL,
                arguments = listOf(
                    navArgument("clubId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->

                val clubId = backStackEntry.arguments
                    ?.getString("clubId")
                    .orEmpty()

                val tokenManager = remember(context) {
                    TokenManager.get(context)
                }

                val clubRepository = remember(tokenManager) {
                    ClubRepository(
                        RetrofitInstance.createClubService(tokenManager)
                    )
                }

                if (clubId.isBlank()) {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                } else {
                    ClubDetailScreen(
                        clubId = clubId,
                        repository = clubRepository,
                        onBack = {
                            navController.safeBack(AppRoutes.CLUBS)
                        }
                    )
                }
            }


            composable(route = AppRoutes.LEADERBOARD) {
                val tokenManager = remember(context) {
                    TokenManager.get(context)
                }

                val leaderboardService = remember(tokenManager) {
                    RetrofitInstance.createLeaderboardService(tokenManager)
                }

                val clubService = remember(tokenManager) {
                    RetrofitInstance.createClubService(tokenManager)
                }

                val clubRepository = remember(clubService) {
                    ClubRepository(clubService)
                }

                val leaderboardViewModel = screenViewModel {
                    LeaderboardViewModel(
                        service = leaderboardService,
                        clubService = clubService,
                        clubRepository = clubRepository
                    )
                }

                LeaderboardScreen(
                    onBack = {
                        navController.safeBack(AppRoutes.MENU)
                    },
                    viewModel = leaderboardViewModel
                )
            }




            composable(route = AppRoutes.SETTINGS_DETAIL) {
                androidx.activity.compose.BackHandler {
                    navController.safeBack(AppRoutes.MENU)
                }

                SettingsDetailScreen(
                    onBack = {
                        navController.safeBack(AppRoutes.MENU)
                    }
                )
            }


            composable(route = AppRoutes.DELETE_ACCOUNT) {
            DeleteAccountScreen(
                onBack = { navController.safePop() },
                onConfirmDelete = {
                    scope.launch {
                        authRepository.logout()
                        Toast.makeText(
                            context,
                            "Account deletion request submitted.",
                            Toast.LENGTH_LONG
                        ).show()
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(route = AppRoutes.WATCH) {
            val watchViewModel: WatchViewModel = viewModel()
            WatchScreen(
                viewModel = watchViewModel,
                onBack = { navController.safeBack(AppRoutes.MENU) },
                onNavigateToSpectate = { code ->
                    navController.navigateOnce(AppRoutes.spectateGame(code))
                }
            )
        }

        composable(
            route = AppRoutes.SPECTATE_GAME,
            arguments = listOf(
                navArgument("watchCode") {
                    type = NavType.StringType
                }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://caisaara.duckdns.org/watch/{watchCode}"
                }
            )
        ) { backStackEntry ->
            val watchCode = backStackEntry.arguments?.getString("watchCode").orEmpty()
            val application = LocalContext.current.applicationContext as Application
            val spectatorViewModel = viewModel<SpectatorGameViewModel>(
                key = "spectate_$watchCode",
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return SpectatorGameViewModel(application, watchCode) as T
                    }
                }
            )
            SpectatorGameScreen(
                viewModel = spectatorViewModel,
                onBack = { navController.safePop() }
            )
        }

    }
    }
}