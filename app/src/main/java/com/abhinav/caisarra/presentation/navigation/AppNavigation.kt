package com.abhinav.caisarra.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abhinav.caisarra.presentation.screens.LoginScreen
import com.abhinav.caisarra.presentation.screens.SignUpScreen


object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
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
                onForgotPasswordClick = {},

                onSignUpClick = {navController.navigate(
                        AppRoutes.REGISTER
                    )
                }
            )
        }

        composable(
            route = AppRoutes.REGISTER
        ) {

            SignUpScreen   (
                onSignUpClick = {},

                onSignInClick = {navController.popBackStack()}
            )
        }
    }
}

