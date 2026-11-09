package com.abhinav.caisarra.presentation.navigation

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.abhinav.caisarra.core.network.userMessage
import com.abhinav.caisarra.data.local.PendingInviteStore
import com.abhinav.caisarra.data.location.LocationProvider
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.FriendsRepository
import com.abhinav.caisarra.data.repository.NearbyRepository
import com.abhinav.caisarra.presentation.directmessage.DirectMessageScreen
import com.abhinav.caisarra.presentation.directmessage.DirectMessagesListScreen
import com.abhinav.caisarra.presentation.invite.CreateInviteScreen
import com.abhinav.caisarra.presentation.invite.CreateInviteViewModel
import com.abhinav.caisarra.presentation.invite.FriendsViewModel
import com.abhinav.caisarra.presentation.invite.JoinInviteScreen
import com.abhinav.caisarra.presentation.invite.JoinInviteViewModel
import com.abhinav.caisarra.presentation.invite.PlayWithFriendHomeScreen
import com.abhinav.caisarra.presentation.invite.PlayWithFriendScreen
import com.abhinav.caisarra.presentation.nearby.NearbyViewModel
import com.abhinav.caisarra.presentation.screens.NearbyScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun NavGraphBuilder.socialGraph(
    navController: NavController,
    authRepository: AuthRepository,
    context: Context,
    scope: CoroutineScope,
    friendsBackRoute: () -> String,
    inviteBackRoute: () -> String,
    onSetInviteBackRoute: (String) -> Unit,
    pendingInviteStore: PendingInviteStore
) {
    composable(route = AppRoutes.PLAY_WITH_FRIEND) {
        val friendsViewModel = screenViewModel {
            FriendsViewModel(context as Application)
        }

        BackHandler {
            navController.safeBack(friendsBackRoute())
        }

        PlayWithFriendScreen(
            viewModel = friendsViewModel,
            onCreateMatchLink = {
                onSetInviteBackRoute(AppRoutes.PLAY_WITH_FRIEND)
                navController.navigateOnce(AppRoutes.CREATE_INVITE)
            },
            onBack = {
                navController.safeBack(friendsBackRoute())
            },
            onDirectMessage = { userId: Long, username: String, friendAvatarUrl: String? ->
                navController.requireAuth(
                    context = context,
                    authRepository = authRepository,
                    scope = scope,
                    featureName = "Direct messages"
                ) {
                    navController.navigate(
                        AppRoutes.directMessage(userId, username, friendAvatarUrl)
                    )
                }
            },
            onViewProfile = { username ->
                navController.navigateOnce(AppRoutes.profile(username))
            },
            onEnterGame = { gameId ->
                navController.navigate(AppRoutes.onlineGame(gameId)) {
                    launchSingleTop = true
                }
            }
        )
    }

    composable(route = AppRoutes.PLAY_WITH_FRIEND_HOME) {
        BackHandler {
            navController.safeBack(friendsBackRoute())
        }

        PlayWithFriendHomeScreen(
            onInviteFriends = {
                navController.navigateOnce(AppRoutes.PLAY_WITH_FRIEND)
            },
            onCreateChallengeLink = {
                onSetInviteBackRoute(AppRoutes.PLAY_WITH_FRIEND_HOME)
                navController.navigateOnce(AppRoutes.CREATE_INVITE)
            },
            onJoinByCode = { code ->
                navController.navigate(AppRoutes.joinInvite(code))
            },
            onBack = {
                navController.safeBack(friendsBackRoute())
            }
        )
    }

    composable(route = AppRoutes.CREATE_INVITE) {
        val createViewModel = screenViewModel {
            CreateInviteViewModel(context as Application)
        }

        BackHandler {
            navController.safeBack(inviteBackRoute())
        }

        CreateInviteScreen(
            viewModel = createViewModel,
            onEnterWaitingBoard = { gameId ->
                navController.navigate(AppRoutes.onlineGame(gameId)) {
                    popUpTo(AppRoutes.CREATE_INVITE) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
            onBack = {
                navController.safeBack(inviteBackRoute())
            }
        )
    }

    composable(
        route = AppRoutes.JOIN_INVITE,
        arguments = listOf(
            navArgument("code") {
                type = NavType.StringType
            }
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = "https://caisaara.app/play/{code}" },
            navDeepLink { uriPattern = "https://caisaara.duckdns.org/play/{code}" }
        )
    ) { entry ->
        val code = entry.arguments?.getString("code").orEmpty()
        val joinViewModel = screenViewModel {
            JoinInviteViewModel(context as Application, code)
        }

        JoinInviteScreen(
            viewModel = joinViewModel,
            onGameReady = { gameId ->
                navController.navigate(AppRoutes.onlineGame(gameId)) {
                    popUpTo(AppRoutes.JOIN_INVITE) {
                        inclusive = true
                    }
                }
            },
            onLogin = {
                pendingInviteStore.save(code)
                navController.goToLogin()
            },
            onBack = {
                pendingInviteStore.clear()
                scope.launch {
                    navController.safeBack(homeRoute(authRepository))
                }
            }
        )
    }
    composable(
        route = AppRoutes.DIRECT_MESSAGE,
        arguments = listOf(
            navArgument("userId") {
                type = NavType.LongType
            },
            navArgument("username") {
                type = NavType.StringType
            },
            navArgument("avatarUrl") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
        val username = backStackEntry.arguments?.getString("username") ?: "Player"
        val avatarUrl = backStackEntry.arguments?.getString("avatarUrl")

        var isLeavingScreen by remember(backStackEntry) {
            mutableStateOf(false)
        }

        DirectMessageScreen(
            userId = userId,
            username = username,
            initialAvatarUrl = avatarUrl,
            onViewProfile = { targetUsername ->
                navController.navigateOnce(AppRoutes.profile(targetUsername))
            },
            onBack = {
                if (!isLeavingScreen) {
                    isLeavingScreen = true
                    val popped = navController.popBackStack()
                    if (!popped) {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            }
        )
    }

    composable(route = AppRoutes.DIRECT_MESSAGES_LIST) {
        var isLeaving by remember {
            mutableStateOf(false)
        }

        BackHandler {
            if (!isLeaving) {
                isLeaving = true
                val popped = navController.popBackStack()
                if (!popped) {
                    navController.navigate(AppRoutes.MENU) {
                        launchSingleTop = true
                    }
                }
            }
        }

        DirectMessagesListScreen(
            onBack = {
                if (!isLeaving) {
                    isLeaving = true
                    val popped = navController.popBackStack()
                    if (!popped) {
                        navController.navigate(AppRoutes.MENU) {
                            launchSingleTop = true
                        }
                    }
                }
            },
            onOpenChat = { userId, username, chatAvatarUrl ->
                navController.navigate(
                    AppRoutes.directMessage(userId, username, chatAvatarUrl)
                )
            }
        )
    }

    composable(route = AppRoutes.NEARBY) {
        val nearbyViewModel = screenViewModel {
            NearbyViewModel(
                repository = NearbyRepository(authRepository.createNearbyService()),
                locationProvider = LocationProvider(context)
            )
        }

        NearbyScreen(
            viewModel = nearbyViewModel,
            onBack = {
                navController.safeBack(AppRoutes.HOME)
            },
            onAddFriend = { player ->
                navController.requireAuth(
                    context = context,
                    authRepository = authRepository,
                    scope = scope,
                    featureName = "Adding friends"
                ) {
                    val friendsRepo = FriendsRepository(context)
                    val currentStatus = friendsRepo.getFriendshipStatus(player.username)
                        .getOrNull()
                        ?.status
                    val statusMessage = when (currentStatus) {
                        "friends" -> "You are already friends with ${player.username}"
                        "request_sent" -> "Friend request already sent to ${player.username}"
                        "request_received" -> "${player.username} already sent you a friend request. Check your requests."
                        else -> null
                    }
                    if (statusMessage != null) {
                        Toast.makeText(context, statusMessage, Toast.LENGTH_SHORT).show()
                        return@requireAuth
                    }
                    friendsRepo.sendFriendRequest(player.username)
                        .onSuccess {
                            Toast.makeText(
                                context,
                                "Friend request sent to ${player.username}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .onFailure { error ->
                            Toast.makeText(
                                context,
                                error.userMessage("Could not send friend request"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            },
            onChat = { player ->
                navController.requireAuth(
                    context = context,
                    authRepository = authRepository,
                    scope = scope,
                    featureName = "Direct messages"
                ) {
                    navController.navigate(
                        AppRoutes.directMessage(player.userId.toLong(), player.username)
                    )
                }
            },
            onGameStarted = { gameId ->
                navController.navigate(AppRoutes.onlineGame(gameId)) {
                    launchSingleTop = true
                }
            }
        )
    }
    composable(route = AppRoutes.SOCIAL) {
        LaunchedEffect(Unit) {
            navController.navigate(AppRoutes.COMMUNITY) {
                popUpTo(AppRoutes.SOCIAL) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }
}
