package com.abhinav.caisarra.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abhinav.caisarra.presentation.screens.LoginScreen
import com.abhinav.caisarra.presentation.screens.ResetPasswordScreen
import com.abhinav.caisarra.presentation.screens.SetNewPasswordScreen
import com.abhinav.caisarra.presentation.screens.SignUpScreen
import com.abhinav.caisarra.presentation.screens.VerifyScreen


object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RESET_PASSWORD = "reset_password"
    const val VERIFY = "verify"
    const val SET_NEW_PASSWORD = "set_new_password"

}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController,
        startDestination = AppRoutes.LOGIN
    ) {
        composable(route = AppRoutes.LOGIN) {
            LoginScreen(
                onSignInClick = {},
                onForgotPasswordClick = {
                    navController.navigate(AppRoutes.RESET_PASSWORD)
                },
                onSignUpClick = {
                    navController.navigate(AppRoutes.REGISTER)
                }
            )
        }

        composable(route = AppRoutes.REGISTER) {
            SignUpScreen(
                onSignUpClick = {},
                onSignInClick = { navController.popBackStack() }
            )
        }

        composable(route = AppRoutes.RESET_PASSWORD) {
            ResetPasswordScreen(
                onSendCode = {
                },
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


    }
}