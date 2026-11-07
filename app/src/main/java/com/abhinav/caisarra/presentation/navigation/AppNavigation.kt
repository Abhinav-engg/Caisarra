
package com.abhinav.caisarra.presentation.navigation

import com.abhinav.caisarra.features.minimates.presentation.miniMatesGraph
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import com.abhinav.caisarra.data.local.OnboardingPreferencesStore
import com.abhinav.caisarra.data.local.PendingInviteStore
import com.abhinav.caisarra.data.local.PendingOtpStore
import com.abhinav.caisarra.data.repository.AuthRepository
import androidx.compose.foundation.layout.WindowInsets
@Composable
fun AppNavigation(
    authRepository: AuthRepository
) {
    val context = LocalContext.current.applicationContext
    val navController = rememberNavController()

    val scope = rememberCoroutineScope()

    val pendingOtpStore = remember(context) {
        PendingOtpStore(context)
    }

    val onboardingPreferencesStore = remember(context) {
        OnboardingPreferencesStore.get(context)
    }

    val pendingInviteStore = remember(context) {
        PendingInviteStore.get(context)
    }
    var isGuestUser by remember {
        mutableStateOf(false)
    }

    var friendsBackRoute by remember {
        mutableStateOf(AppRoutes.HOME)
    }

    var inviteBackRoute by remember {
        mutableStateOf(AppRoutes.PLAY_WITH_FRIEND)
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    LaunchedEffect(currentRoute) {
        isGuestUser = authRepository.isGuest()
    }
    val bottomNavigationRoutes = remember {
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
        !isGuestUser && currentRoute in bottomNavigationRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF080D13),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigateTo = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(AppRoutes.HOME) {
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
            startDestination = AppRoutes.SPLASH,
            modifier = Modifier.fillMaxSize()
                .padding(
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            authGraph(
                navController = navController,
                authRepository = authRepository,
                pendingOtpStore = pendingOtpStore,
                onboardingPreferencesStore = onboardingPreferencesStore,
                pendingInviteStore = pendingInviteStore,
                context = context,
                scope = scope
            )

            homeGraph(
                navController = navController,
                authRepository = authRepository,
                context = context,
                scope = scope,
                onOpenPlayGame = {
                    navController.navigateOnce(
                        AppRoutes.GAME_SETUP
                    )
                }
            )

            gameGraph(
                navController = navController,
                authRepository = authRepository,
                context = context,
                scope = scope,
                onSetFriendsBackRoute = { route ->
                    friendsBackRoute = route
                }
            )


            puzzleGraph(
                navController = navController,
                context = context
            )
            miniMatesGraph(navController)
            profileGraph(
                navController = navController,
                authRepository = authRepository,
                isGuestUser = isGuestUser,
                context = context,
                scope = scope
            )

            socialGraph(
                navController = navController,
                authRepository = authRepository,
                context = context,
                scope = scope,
                friendsBackRoute = {
                    friendsBackRoute
                },
                inviteBackRoute = {
                    inviteBackRoute
                },
                onSetInviteBackRoute = { route ->
                    inviteBackRoute = route
                },
                pendingInviteStore = pendingInviteStore
            )


            communityAndClubGraph(
                navController = navController,
                authRepository = authRepository,
                isGuestUser = isGuestUser,
                context = context,
                scope = scope
            )

            settingsAndMoreGraph(
                navController = navController,
                authRepository = authRepository,
                isGuestUser = isGuestUser,
                context = context,
                scope = scope,
                onSetFriendsBackRoute = { route ->
                    friendsBackRoute = route
                }
            )

            tournamentGraph(
                navController = navController,
                context = context
            )
        }
    }
}
