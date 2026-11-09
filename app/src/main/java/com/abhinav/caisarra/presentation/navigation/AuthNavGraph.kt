package com.abhinav.caisarra.presentation.navigation

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.abhinav.caisarra.data.local.OnboardingPreferencesStore
import com.abhinav.caisarra.data.local.PendingInviteStore
import com.abhinav.caisarra.data.local.PendingOtpStore
import com.abhinav.caisarra.data.local.RegistrationDataStore
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.AuthResult
import com.abhinav.caisarra.presentation.screens.LoginScreen
import com.abhinav.caisarra.presentation.screens.OnboardingScreen
import com.abhinav.caisarra.presentation.screens.RatingScreen
import com.abhinav.caisarra.presentation.screens.ResetPasswordScreen
import com.abhinav.caisarra.presentation.screens.SetNewPasswordScreen
import com.abhinav.caisarra.presentation.screens.SignUpScreen
import com.abhinav.caisarra.presentation.screens.SplashScreen
import com.abhinav.caisarra.presentation.screens.VerifyScreen
import com.abhinav.caisarra.presentation.viewmodel.LoginViewModel
import com.abhinav.caisarra.presentation.viewmodel.RatingViewModel
import com.abhinav.caisarra.presentation.viewmodel.ResetPasswordViewModel
import com.abhinav.caisarra.presentation.viewmodel.SetNewPasswordViewModel
import com.abhinav.caisarra.presentation.viewmodel.SignUpViewModel
import com.abhinav.caisarra.presentation.viewmodel.VerifyPurpose
import com.abhinav.caisarra.presentation.viewmodel.VerifyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun NavGraphBuilder.authGraph(
    navController: NavController,
    authRepository: AuthRepository,
    pendingOtpStore: PendingOtpStore,
    onboardingPreferencesStore: OnboardingPreferencesStore,
    pendingInviteStore: PendingInviteStore,
    context: Context,
    scope: CoroutineScope
) {
    composable(
        route = AppRoutes.SPLASH,
        exitTransition = {
            fadeOut(animationSpec = tween(durationMillis = 300))
        }
    ) {
        SplashScreen(
            loadDestination = {
                resolveStartDestination(
                    authRepository = authRepository,
                    pendingOtpStore = pendingOtpStore,
                    onboardingStore = onboardingPreferencesStore
                )
            },
            onSplashFinished = { targetRoute ->
                navController.navigate(targetRoute) {
                    popUpTo(AppRoutes.SPLASH) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        )
    }

    composable(
        route = AppRoutes.ONBOARDING,
        exitTransition = {
            fadeOut(animationSpec = tween(durationMillis = 300))
        }
    ) {
        OnboardingScreen(
            onFinishOnboarding = {
                onboardingPreferencesStore.setHasSeenOnboarding(true)
                navController.navigate(AppRoutes.LOGIN) {
                    popUpTo(AppRoutes.ONBOARDING) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        )
    }

    composable(route = AppRoutes.LOGIN) {
        LoginScreen(
            viewModel = screenViewModel {
                LoginViewModel(repository = authRepository)
            },
            onLoginSuccess = {
                scope.launch {
                    if (authRepository.isAdmin()) {
                        navController.navigate(AppRoutes.ADMIN) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    } else {
                        navController.goHomeThenPendingInvite(
                            pendingInviteStore
                        )
                    }
                }
            },
            onForgotPasswordClick = {
                navController.navigateOnce(AppRoutes.RESET_PASSWORD)
            },
            onSignUpClick = {
                navController.navigateOnce(AppRoutes.REGISTER)
            },
            onAdminLoginClick = {
                navController.navigateOnce(AppRoutes.ADMIN_LOGIN)
            },
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
                    registrationDataStore = RegistrationDataStore(context.applicationContext)
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
                            email = email,
                            purpose = VerifyPurpose.REGISTRATION
                        )
                    )
                }
            },
            onSignInClick = {
                navController.goToLogin()
            }
        )
    }

    composable(route = AppRoutes.RESET_PASSWORD) {
        ResetPasswordScreen(
            viewModel = screenViewModel {
                ResetPasswordViewModel(repository = authRepository)
            },
            onSendCode = { email ->
                scope.launch {
                    pendingOtpStore.save(
                        email = email,
                        purpose = VerifyPurpose.RESET_PASSWORD.name
                    )
                    navController.navigate(
                        AppRoutes.verify(
                            email = email,
                            purpose = VerifyPurpose.RESET_PASSWORD
                        )
                    )
                }
            },
            onBackToSignIn = {
                navController.safeBack(AppRoutes.LOGIN)
            }
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
            viewModel = screenViewModel {
                RatingViewModel(repository = authRepository)
            },
            onRated = {
                navController.goHomeThenPendingInvite(pendingInviteStore)
            }
        )
    }

    composable(route = AppRoutes.NEW_PASSWORD) {
        SetNewPasswordScreen(
            viewModel = screenViewModel {
                SetNewPasswordViewModel(repository = authRepository)
            },
            onPasswordUpdated = {
                navController.popBackStack(
                    route = AppRoutes.LOGIN,
                    inclusive = false
                )
            }
        )
    }
}
