
package com.abhinav.caisarra.presentation.navigation

import android.content.Intent
import android.widget.Toast
import androidx.navigation.navOptions
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhinav.caisarra.core.network.observeIsOnline
import com.abhinav.caisarra.core.ui.components.OfflineBanner
import com.abhinav.caisarra.features.blockedplayers.presentation.blockedPlayersGraph
import com.abhinav.caisarra.features.follow.presentation.followListGraph
import com.abhinav.caisarra.features.minimates.presentation.miniMatesGraph
import com.abhinav.caisarra.features.notificationsettings.presentation.notificationSettingsGraph
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
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
import com.abhinav.caisarra.features.profilelink.presentation.LocalOpenProfile
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import com.abhinav.caisarra.data.local.OnboardingPreferencesStore
import com.abhinav.caisarra.data.local.PendingInviteStore
import com.abhinav.caisarra.data.local.PendingOtpStore
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.presentation.navigation.adminGraph
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.collectAsState
import com.abhinav.caisarra.app.MainActivity
import com.abhinav.caisarra.data.repository.NotificationRepository

private val preAuthRoutes = setOf(
    AppRoutes.SPLASH,
    AppRoutes.ONBOARDING,
    AppRoutes.LOGIN,
    AppRoutes.REGISTER,
    AppRoutes.RESET_PASSWORD,
    AppRoutes.VERIFY,
    AppRoutes.NEW_PASSWORD,
    AppRoutes.GUEST_HOME
)

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
    val pendingNotificationIntent by MainActivity.pendingIntentFlow.collectAsState()
    var accountPromptShownFor by remember { mutableStateOf<Intent?>(null) }
    LaunchedEffect(pendingNotificationIntent, currentRoute) {
        val intent = pendingNotificationIntent ?: return@LaunchedEffect
        val notifType = intent.getStringExtra("notification_type")?.takeIf { it.isNotBlank() }
            ?: intent.getStringExtra("type")
        val notifId = intent.getStringExtra("notification_id")?.toLongOrNull()
        if (notifType.isNullOrBlank() && notifId == null) {
            val uri = intent.data ?: run {
                MainActivity.pendingIntentFlow.value = null
                return@LaunchedEffect
            }
            when (decideDeepLink(uri, currentRoute, preAuthRoutes, navController, authRepository)) {
                DeepLinkDecision.Wait -> Unit
                DeepLinkDecision.Drop -> MainActivity.pendingIntentFlow.value = null
                DeepLinkDecision.Open -> {
                    MainActivity.pendingIntentFlow.value = null
                    navController.navigate(uri, navOptions { launchSingleTop = true })
                }
                DeepLinkDecision.NeedsAccount -> {
                    if (accountPromptShownFor !== intent) {
                        accountPromptShownFor = intent
                        Toast.makeText(context, "Sign in to open this link.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            return@LaunchedEffect
        }
        if (currentRoute == null || currentRoute in preAuthRoutes) return@LaunchedEffect
        if (!authRepository.isLoggedIn()) return@LaunchedEffect
        MainActivity.pendingIntentFlow.value = null
        handleIntentNotificationNavigation(intent, notifType, navController)
        if (notifId != null && notifId > 0) {
            NotificationRepository.get(context).markAsRead(notifId)
        }
    }
    val bottomNavigationRoutes = remember {
        setOf(
            AppRoutes.HOME,
            AppRoutes.GAME_SETUP,
            AppRoutes.PUZZLES,
            AppRoutes.NEARBY,
            AppRoutes.COMMUNITY,
            AppRoutes.SOCIAL,
            AppRoutes.MENU
        )
    }

    val isOnline by remember(context) { context.observeIsOnline() }.collectAsStateWithLifecycle(initialValue = true)

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
                        if (currentRoute == AppRoutes.GAME_SETUP) navController.popBackStack(AppRoutes.HOME, false)
                        navController.navigateToTab(route)
                    }
                )
            }
        }
    ) { innerPadding ->

        CompositionLocalProvider(
            LocalOpenProfile provides { username -> navController.navigateOnce(AppRoutes.profile(username)) }
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(
                        bottom = innerPadding.calculateBottomPadding()
                    )
            ) {
            OfflineBanner(visible = !isOnline)
            NavHost(
                navController = navController,
                startDestination = AppRoutes.SPLASH,
                enterTransition = { menuEnterTransition() },
                popExitTransition = { menuPopExitTransition() },
                modifier = Modifier.fillMaxWidth()
                    .weight(1f)
                    .then(if (isOnline) Modifier else Modifier.consumeWindowInsets(WindowInsets.statusBars))
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
                adminGraph(
                    navController = navController,
                    authRepository = authRepository
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
                notificationSettingsGraph(navController)
                blockedPlayersGraph(navController)
                followListGraph(navController)
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
    }
}
