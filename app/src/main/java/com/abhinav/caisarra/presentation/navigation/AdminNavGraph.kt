
package com.abhinav.caisarra.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.AdminRepository
import com.abhinav.caisarra.presentation.screens.AdminLoginScreen
import com.abhinav.caisarra.presentation.screens.AdminScreen
import com.abhinav.caisarra.presentation.viewmodel.AdminLoginViewModel
import com.abhinav.caisarra.presentation.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

fun NavGraphBuilder.adminGraph(
    navController: NavController,
    authRepository: AuthRepository
) {
    composable(AppRoutes.ADMIN_LOGIN) {

        AdminLoginScreen(
            viewModel = screenViewModel {
                AdminLoginViewModel(authRepository)
            },

            onLoginSuccess = {
                navController.navigate(AppRoutes.ADMIN) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },

            onBackToUserLogin = {
                navController.navigate(AppRoutes.LOGIN) {
                    popUpTo(AppRoutes.ADMIN_LOGIN) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        )
    }

    composable(AppRoutes.ADMIN) {

        var authorized by remember {
            mutableStateOf<Boolean?>(null)
        }

        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            authorized = authRepository.isAdmin()

            if (authorized == false) {
                authRepository.logout()

                navController.navigate(AppRoutes.ADMIN_LOGIN) {
                    popUpTo(AppRoutes.ADMIN) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }

        when (authorized) {

            true -> {
                AdminScreen(
                    viewModel = screenViewModel {
                        AdminViewModel(
                            AdminRepository(
                                authRepository.createAdminService()
                            )
                        )
                    },
                    onLogout = {
                        scope.launch {
                            authRepository.logout()

                            navController.navigate(AppRoutes.ADMIN_LOGIN) {
                                popUpTo(navController.graph.id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            false, null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF00D5E8)
                    )
                }
            }
        }
    }
}
