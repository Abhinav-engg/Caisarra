package com.abhinav.caisarra.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.presentation.components.GeneralButton
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

object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RESET_PASSWORD = "reset_password"
    const val VERIFY = "verify/{purpose}/{email}"
    const val NEW_PASSWORD = "new_password"
    const val HOME = "home"

    fun verify(email: String, purpose: VerifyPurpose) =
        "verify/${purpose.name}/${Uri.encode(email)}"
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
        startDestination = start
    ) {
        composable(route = AppRoutes.LOGIN) {
            LoginScreen(
                viewModel = screenViewModel { LoginViewModel(authRepository) },
                onLoginSuccess = { navController.goHome() },
                onForgotPasswordClick = { navController.navigate(AppRoutes.RESET_PASSWORD) },
                onSignUpClick = { navController.navigate(AppRoutes.REGISTER) }
            )
        }

        composable(route = AppRoutes.REGISTER) {
            SignUpScreen(
                viewModel = screenViewModel { SignUpViewModel(authRepository) },
                onSignUpSuccess = { email ->
                    navController.navigate(AppRoutes.verify(email, VerifyPurpose.REGISTRATION))
                },
                onSignInClick = { navController.popBackStack() }
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
                onVerified = {
                    if (purpose == VerifyPurpose.REGISTRATION) {
                        navController.goHome()
                    } else {
                        navController.navigate(AppRoutes.NEW_PASSWORD) {
                            popUpTo(AppRoutes.VERIFY) { inclusive = true }
                        }
                    }
                }
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
            val scope = rememberCoroutineScope()
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                GeneralButton(
                    text = "Log Out",
                    modifier = Modifier.width(200.dp),
                    onClick = {
                        scope.launch {
                            authRepository.logout()
                            navController.navigate(AppRoutes.LOGIN) {
                                popUpTo(AppRoutes.HOME) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}