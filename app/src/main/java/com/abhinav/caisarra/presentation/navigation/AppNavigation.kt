package com.abhinav.caisarra.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.presentation.screens.LoginScreen
import com.abhinav.caisarra.presentation.screens.ResetPasswordScreen
import com.abhinav.caisarra.presentation.screens.SetNewPasswordScreen
import com.abhinav.caisarra.presentation.screens.SignUpScreen
import com.abhinav.caisarra.presentation.screens.VerifyScreen

import com.abhinav.caisarra.presentation.viewmodel.LoginViewModel
import com.abhinav.caisarra.presentation.viewmodel.ResetPasswordViewModel
import com.abhinav.caisarra.presentation.viewmodel.SignUpViewModel
import com.abhinav.caisarra.presentation.screens.GuestScreen
object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val GUEST = "guest"
    const val RESET_PASSWORD = "reset_password"
    const val VERIFY = "verify"
    const val SET_NEW_PASSWORD = "set_new_password"
    const val HOME = "home"
}

@Composable
private inline fun <reified VM : ViewModel> screenViewModel(crossinline create: () -> VM): VM =
    viewModel(factory = viewModelFactory { initializer { create() } })

private fun NavController.goHome() {
    navigate(AppRoutes.HOME) {
        popUpTo(AppRoutes.LOGIN) { inclusive = true }
    }
}

@Composable
fun AppNavigation(authRepository: AuthRepository) {
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        startDestination =
            if (authRepository.isLoggedIn()) AppRoutes.HOME else AppRoutes.LOGIN
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

    NavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN
    ) {
        composable(route = AppRoutes.LOGIN) {
            LoginScreen(
                viewModel = screenViewModel { LoginViewModel(authRepository) },
                onLoginSuccess = {navController.goHome() },
                onForgotPasswordClick = { navController.navigate(AppRoutes.RESET_PASSWORD) },
                onSignUpClick = {navController.navigate(AppRoutes.REGISTER) },
                onContinueAsGuestClick = {navController.navigate(AppRoutes.GUEST) }
            )
        }
        composable(route = AppRoutes.GUEST) {
            GuestScreen(
                onContinueClick = { username ->
                    navController.goHome()
                },
                onSignUpClick = {navController.navigate(AppRoutes.REGISTER) }
            )
        }

        composable(route = AppRoutes.REGISTER) {
            SignUpScreen(
                viewModel = screenViewModel { SignUpViewModel(authRepository) },

                onSignUpSuccess = { navController.goHome() },

                onSignInClick = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.REGISTER) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(route = AppRoutes.RESET_PASSWORD) {
            ResetPasswordScreen(
                viewModel = screenViewModel { ResetPasswordViewModel(authRepository) },
                onBackToSignIn = { navController.popBackStack() }
            )
        }
        composable(AppRoutes.VERIFY) {

            VerifyScreen(

                onContinue = { enteredCode ->

                    if (enteredCode == "123456") {
                        navController.navigate(AppRoutes.SET_NEW_PASSWORD)
                        true
                    } else {
                        false
                    }
                },

                onResendCode = {
                }
            )
        }
        composable(AppRoutes.SET_NEW_PASSWORD) {

            SetNewPasswordScreen(
                onResetAndSignIn = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.LOGIN) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = AppRoutes.HOME) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Home")
            }
        }
    }
}