package com.abhinav.caisarra.presentation.navigation

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
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.presentation.screens.GuestOptionsScreen
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

object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RESET_PASSWORD = "reset_password"
    const val VERIFY = "verify/{purpose}/{email}"
    const val NEW_PASSWORD = "new_password"
    const val HOME = "home"
    const val GAME_SETUP = "game_setup"
    //const val GAME = "game"
    const val GAME = "game?resume={resume}" +
                "&white={white}" +
                "&black={black}" +
                "&minutes={minutes}" +
                "&flip={flip}" +
                "&undo={undo}"
    const val GUEST_OPTIONS = "guest_options"
    const val RATING = "rating"

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

@Composable
fun AppNavigation(authRepository: AuthRepository) {
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        startDestination = when {
            authRepository.isLoggedIn() -> AppRoutes.HOME
            authRepository.isGuest() -> AppRoutes.GUEST_OPTIONS
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
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = start
    ) {
        composable(route = AppRoutes.LOGIN) {
            LoginScreen(
                viewModel = screenViewModel { LoginViewModel(authRepository) },
                onLoginSuccess = { navController.goHome() },
                onForgotPasswordClick = { navController.navigate(AppRoutes.RESET_PASSWORD) },
                onSignUpClick = { navController.navigate(AppRoutes.REGISTER) },
                onContinueAsGuestClick = {
                    scope.launch {
                        when (val result = authRepository.guestLogin()) {
                            is AuthResult.Success -> navController.navigate(AppRoutes.GUEST_OPTIONS)
                            is AuthResult.Error -> Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        composable(route = AppRoutes.REGISTER) {
            SignUpScreen(
                viewModel = screenViewModel {SignUpViewModel(
                        repository = authRepository,registrationDataStore = RegistrationDataStore(context.applicationContext)
                    )
                },
                onSignUpSuccess = { email ->
                    navController.navigate(AppRoutes.verify(email, VerifyPurpose.REGISTRATION))
                },
                onSignInClick = { navController.goToLogin() }
            )
        }
        composable(route = AppRoutes.GUEST_OPTIONS) {
            var guestId by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                guestId = authRepository.getGuestId().orEmpty()
            }

            GuestOptionsScreen(
                guestId = guestId,
                onLoginClick = { navController.goToLogin() },
                onSignUpClick = { navController.navigate(AppRoutes.REGISTER) },
                onLogoutClick = {
                    scope.launch {
                        authRepository.logout()
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(route = AppRoutes.RESET_PASSWORD) {
            ResetPasswordScreen(
                viewModel = screenViewModel { ResetPasswordViewModel(authRepository) },
                onSendCode = { email ->
                    navController.navigate(AppRoutes.verify(email, VerifyPurpose.RESET_PASSWORD))
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
                viewModel = screenViewModel { VerifyViewModel(authRepository, email, purpose) },
                email = email,
                purpose = purpose,
                onBack = { navController.popBackStack() },
                onVerified = {
                    if (purpose == VerifyPurpose.REGISTRATION) {
                    navController.navigate(AppRoutes.RATING) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                } else {
                        navController.navigate(AppRoutes.NEW_PASSWORD) {
                            popUpTo(AppRoutes.VERIFY) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(route = AppRoutes.RATING) {
            RatingScreen(
                viewModel = screenViewModel { RatingViewModel(authRepository) },
                onRated = { navController.goHome() }
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
                HomeViewModel(authRepository)
            }

            HomeScreen(
                viewModel = homeViewModel,

                onStartChallenge = {
                    navController.navigate(
                        AppRoutes.GAME_SETUP
                    )
                },
                onResumeGame = {
                    navController.navigate(
                        AppRoutes.resumeGame()
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
                    navController.navigate(
                        AppRoutes.HOME
                    ) {
                        popUpTo(
                            AppRoutes.HOME
                        ) {
                            inclusive = true
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
    }
}