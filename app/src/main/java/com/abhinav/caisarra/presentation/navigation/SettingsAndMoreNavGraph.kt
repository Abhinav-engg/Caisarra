package com.abhinav.caisarra.presentation.navigation

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.abhinav.caisarra.data.local.BOARD_THEME_KEY
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.local.settingsDataStore
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.ClubRepository
import com.abhinav.caisarra.data.repository.PrivacyRepository
import com.abhinav.caisarra.presentation.game.model.BoardThemeId
import com.abhinav.caisarra.presentation.game.model.BoardThemes
import com.abhinav.caisarra.presentation.notifications.NotificationsScreen
import com.abhinav.caisarra.presentation.notifications.NotificationsViewModel
import com.abhinav.caisarra.presentation.screens.DeleteAccountScreen
import com.abhinav.caisarra.presentation.screens.LeaderboardScreen
import com.abhinav.caisarra.presentation.screens.MenuScreen
import com.abhinav.caisarra.presentation.screens.NewsScreen
import com.abhinav.caisarra.presentation.screens.PrivacySettingsScreen
import com.abhinav.caisarra.presentation.screens.SettingsDetailScreen
import com.abhinav.caisarra.presentation.screens.ThemeScreen
import com.abhinav.caisarra.presentation.viewmodel.DeleteAccountViewModel
import com.abhinav.caisarra.presentation.viewmodel.LeaderboardViewModel
import com.abhinav.caisarra.presentation.viewmodel.NewsViewModel
import com.abhinav.caisarra.presentation.viewmodel.PrivacySettingsViewModel
import com.abhinav.caisarra.presentation.watch.SpectatorGameScreen
import com.abhinav.caisarra.presentation.watch.SpectatorGameViewModel
import com.abhinav.caisarra.presentation.watch.WatchScreen
import com.abhinav.caisarra.presentation.watch.WatchViewModel
import com.abhinav.caisarra.data.remote.admin.AdminService
import com.abhinav.caisarra.data.repository.AdminRepository
import com.abhinav.caisarra.presentation.screens.AdminScreen
import com.abhinav.caisarra.presentation.viewmodel.AdminViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


fun NavGraphBuilder.settingsAndMoreGraph(
    navController: NavController,
    authRepository: AuthRepository,
    isGuestUser: Boolean,
    context: Context,
    scope: CoroutineScope,
    onSetFriendsBackRoute: (String) -> Unit
) {
    composable(route = AppRoutes.MENU) {
        val tokenManager = remember {
            TokenManager.get(context)
        }
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val profile by rememberCurrentUserProfile(
            tokenManager = tokenManager,
            isGuest = isGuestUser,
            refreshKey = currentRoute
        )
        var isAdmin by remember {
            mutableStateOf(false)
        }

        LaunchedEffect(isGuestUser) {
            isAdmin = if (isGuestUser) {
                false
            } else {
                authRepository.isAdmin()
            }
        }

        val currentThemeKey by remember(context) {
            context.settingsDataStore.data.map { preferences ->
                preferences[BOARD_THEME_KEY] ?: BoardThemeId.MIDNIGHT_CYAN.storageKey
            }
        }.collectAsState(initial = BoardThemeId.MIDNIGHT_CYAN.storageKey)

        val activeTheme = remember(currentThemeKey) {
            BoardThemes.getByStorageKey(currentThemeKey)
        }

        MenuScreen(
            profileName = profile?.displayName,
            isAdmin = isAdmin,
            username = profile?.username,
            avatarUrl = profile?.avatarUrl,
            isGuest = isGuestUser,
            activeThemeName = activeTheme.name,
            onBack = {
                scope.launch {
                    navController.safeBack(homeRoute(authRepository))
                }
            },
            onSettingsClick = {
                navController.navigateOnce(AppRoutes.SETTINGS_DETAIL)
            },
            onProfileClick = {
                navController.navigateOnce(AppRoutes.profile())
            },
            onStatsClick = {
                navController.navigateOnce(AppRoutes.stats()) {
                    launchSingleTop = true
                }
            },
            onAdminClick = {
                scope.launch {
                    val authorized = authRepository.isAdmin()

                    if (authorized) {
                        navController.navigateOnce(AppRoutes.ADMIN)
                    } else {
                        Toast.makeText(
                            context,
                            "Admin access denied",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            onGameHistoryClick = {
                navController.navigateOnce(AppRoutes.ALL_GAME_HISTORY)
            },
            onThemeClick = {
                navController.navigateOnce(AppRoutes.THEME)
            },
            onFriendsClick = {
                navController.requireAuth(
                    context = context,
                    authRepository = authRepository,
                    scope = scope,
                    featureName = "Friends"
                ) {
                    onSetFriendsBackRoute(AppRoutes.MENU)
                    navController.navigateOnce(AppRoutes.PLAY_WITH_FRIEND) {
                        launchSingleTop = true
                    }
                }
            },
            onDirectMessagesClick = {
                navController.requireAuth(
                    context = context,
                    authRepository = authRepository,
                    scope = scope,
                    featureName = "Direct Messages"
                ) {
                    navController.navigateOnce(AppRoutes.DIRECT_MESSAGES_LIST) {
                        launchSingleTop = true
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
                navController.requireAuth(
                    context = context,
                    authRepository = authRepository,
                    scope = scope,
                    featureName = "Spectating"
                ) {
                    navController.navigateOnce(AppRoutes.WATCH)
                }
            },
            onNewsClick = {
                navController.navigateOnce(AppRoutes.NEWS)
            },
            onDeleteAccountClick = {
                navController.navigateOnce(AppRoutes.DELETE_ACCOUNT)
            },
            onPrivacyClick = {
                navController.requireAuth(
                    context = context,
                    authRepository = authRepository,
                    scope = scope,
                    featureName = "Privacy settings"
                ) {
                    navController.navigateOnce(AppRoutes.PRIVACY_SETTINGS)
                }
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
    composable(route = AppRoutes.ADMIN) {

        val tokenManager = remember(context) {
            TokenManager.get(context)
        }

        val adminService = remember(tokenManager) {
            RetrofitInstance.createAdminService(tokenManager)
        }

        val adminRepository = remember(adminService) {
            AdminRepository(adminService)
        }

        val adminViewModel = screenViewModel {
            AdminViewModel(adminRepository)
        }

        var authorized by remember {
            mutableStateOf<Boolean?>(null)
        }

        LaunchedEffect(Unit) {
            authorized = authRepository.isAdmin()
        }

        when (authorized) {
            true -> {
                AdminScreen(
                    viewModel = adminViewModel,
                    onBack = {
                        navController.safeBack(AppRoutes.MENU)
                    }
                )
            }

            false -> {
                LaunchedEffect(Unit) {
                    Toast.makeText(
                        context,
                        "You don't have admin permission",
                        Toast.LENGTH_SHORT
                    ).show()

                    navController.safeBack(AppRoutes.MENU)
                }
            }

            null -> {
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

    composable(route = AppRoutes.THEME) {
        BackHandler {
            navController.safeBack(AppRoutes.MENU)
        }

        ThemeScreen(
            onBack = {
                navController.safeBack(AppRoutes.MENU)
            }
        )
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
            viewModel = leaderboardViewModel,
            onBack = {
                navController.safeBack(AppRoutes.MENU)
            },
            onPlayerClick = { username ->
                navController.navigateOnce(AppRoutes.profile(username))
            }
        )
    }

    composable(route = AppRoutes.NEWS) {
        val tokenManager = remember(context) {
            TokenManager.get(context)
        }
        val newsService = remember(tokenManager) {
            RetrofitInstance.createNewsService(tokenManager)
        }
        val newsViewModel = screenViewModel {
            NewsViewModel(newsService)
        }

        NewsScreen(
            viewModel = newsViewModel,
            onBack = {
                navController.safeBack(AppRoutes.MENU)
            }
        )
    }

    composable(route = AppRoutes.SETTINGS_DETAIL) {
        BackHandler {
            navController.safeBack(AppRoutes.MENU)
        }

        SettingsDetailScreen(
            onBack = {
                navController.safeBack(AppRoutes.MENU)
            },
            onPrivacyClick = {
                navController.requireAuth(
                    context = context,
                    authRepository = authRepository,
                    scope = scope,
                    featureName = "Privacy settings"
                ) {
                    navController.navigateOnce(AppRoutes.PRIVACY_SETTINGS)
                }
            },
            onNotificationsClick = {
                navController.requireAuth(
                    context = context,
                    authRepository = authRepository,
                    scope = scope,
                    featureName = "Notification settings"
                ) {
                    navController.navigateOnce(AppRoutes.NOTIFICATION_SETTINGS)
                }
            },
            isGuest = isGuestUser,
            onDeleteAccountClick = {
                navController.navigateOnce(AppRoutes.DELETE_ACCOUNT)
            }
        )
    }

    composable(route = AppRoutes.PRIVACY_SETTINGS) {
        BackHandler {
            navController.safePop()
        }

        val tokenManager = remember {
            TokenManager.get(context)
        }
        val privacyService = remember {
            RetrofitInstance.createPrivacyService(tokenManager)
        }
        val privacyRepository = remember {
            PrivacyRepository(privacyService)
        }
        val privacyViewModel: PrivacySettingsViewModel = screenViewModel {
            PrivacySettingsViewModel(privacyRepository)
        }

        PrivacySettingsScreen(
            viewModel = privacyViewModel,
            onBack = {
                navController.safePop()
            }
        )
    }

    composable(route = AppRoutes.DELETE_ACCOUNT) {
        BackHandler {
            navController.safePop()
        }

        if (isGuestUser) {
            LaunchedEffect(Unit) {
                Toast.makeText(
                    context,
                    "Guest accounts cannot be deleted.",
                    Toast.LENGTH_SHORT
                ).show()
                navController.safePop()
            }
        } else {
            val deleteAccountViewModel: DeleteAccountViewModel = screenViewModel {
                DeleteAccountViewModel(authRepository)
            }

            DeleteAccountScreen(
                viewModel = deleteAccountViewModel,
                onBack = {
                    navController.safePop()
                },
                onConfirmDelete = {
                    Toast.makeText(
                        context,
                        "Account deleted successfully.",
                        Toast.LENGTH_LONG
                    ).show()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }

    composable(route = AppRoutes.WATCH) {
        val watchViewModel: WatchViewModel = viewModel()

        WatchScreen(
            viewModel = watchViewModel,
            onBack = {
                navController.safeBack(AppRoutes.MENU)
            },
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
            navDeepLink { uriPattern = "https://caisaara.app/watch/{watchCode}" },
            navDeepLink { uriPattern = "https://caisaara.duckdns.org/watch/{watchCode}" }
        )
    ) { backStackEntry ->
        val watchCode = backStackEntry.arguments?.getString("watchCode").orEmpty()
        val application = context.applicationContext as Application
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
            onBack = {
                navController.safePop()
            }
        )
    }
    composable(route = AppRoutes.NOTIFICATIONS) {
        val notifViewModel: NotificationsViewModel = viewModel()

        NotificationsScreen(
            viewModel = notifViewModel,
            onBack = {
                navController.safePop()
            },
            onNotificationClick = { notification ->
                handleNotificationItemClick(
                    notification = notification,
                    navController = navController
                )
            }
        )
    }
}
