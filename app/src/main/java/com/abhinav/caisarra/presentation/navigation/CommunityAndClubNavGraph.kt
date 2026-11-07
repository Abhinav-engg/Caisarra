package com.abhinav.caisarra.presentation.navigation

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.ClubRepository
import com.abhinav.caisarra.data.repository.CommunityRepository
import com.abhinav.caisarra.data.repository.RemoteGameRepository
import com.abhinav.caisarra.presentation.club.ClubDetailScreen
import com.abhinav.caisarra.presentation.club.ClubScreen
import com.abhinav.caisarra.presentation.club.ClubViewModel
import com.abhinav.caisarra.presentation.community.CommunityScreen
import com.abhinav.caisarra.presentation.community.CommunityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun NavGraphBuilder.communityAndClubGraph(
    navController: NavController,
    authRepository: AuthRepository,
    isGuestUser: Boolean,
    context: Context,
    scope: CoroutineScope
) {

    composable(route = AppRoutes.COMMUNITY) {
        val tokenManager = remember {
            TokenManager.get(context)
        }
        val accessToken by tokenManager.accessTokenFlow.collectAsState(initial = null)
        val profile by rememberCurrentUserProfile(
            tokenManager = tokenManager,
            isGuest = isGuestUser
        )

        val communityViewModel = screenViewModel {
            CommunityViewModel(
                repository = CommunityRepository(
                    service = RetrofitInstance.createCommunityService(tokenManager)
                ),
                remoteGameRepository = RemoteGameRepository(
                    gamesService = RetrofitInstance.createGamesService(tokenManager)
                )
            )
        }

        CommunityScreen(
            viewModel = communityViewModel,
            isRegisteredUser = !accessToken.isNullOrBlank(),
            currentUsername = profile?.username,
            accessToken = accessToken,
            currentAvatarUrl = profile?.avatarUrl,
            currentDisplayName = profile?.displayName,
            onBack = {
                scope.launch {
                    navController.safeBack(homeRoute(authRepository))
                }
            }
        )
    }

    composable(route = AppRoutes.CLUBS) {
        val tokenManager = remember(context) {
            TokenManager.get(context)
        }

        val clubRepository = remember(tokenManager) {
            ClubRepository(
                service = RetrofitInstance.createClubService(tokenManager)
            )
        }

        val clubViewModel: ClubViewModel = viewModel(
            key = "club-list",
            factory = viewModelFactory {
                initializer {
                    ClubViewModel(clubRepository)
                }
            }
        )

        var isRegisteredUser by remember {
            mutableStateOf(false)
        }

        LaunchedEffect(authRepository) {
            isRegisteredUser = runCatching {
                authRepository.isLoggedIn() && !authRepository.isGuest()
            }.getOrDefault(false)
        }

        ClubScreen(
            viewModel = clubViewModel,
            isRegisteredUser = isRegisteredUser,
            onBack = {
                navController.safeBack(AppRoutes.MENU)
            },
            onOpenClub = { clubId ->
                navController.navigateOnce(
                    AppRoutes.clubDetail(clubId)
                )
            },
            onSignIn = {
                navController.navigateOnce(AppRoutes.LOGIN)
            }
        )
    }
    composable(
        route = AppRoutes.CLUB_DETAIL,
        arguments = listOf(
            navArgument("clubId") {
                type = NavType.StringType
            }
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = "caisarra://club/{clubId}" },
            navDeepLink { uriPattern = "https://caisaara.app/club/{clubId}" },
            navDeepLink { uriPattern = "https://caisaara.duckdns.org/club/{clubId}" }
        )
    ) { backStackEntry ->
        val clubId = backStackEntry.arguments?.getString("clubId").orEmpty()
        val tokenManager = remember(context) {
            TokenManager.get(context)
        }

        val clubRepository = remember(tokenManager) {
            ClubRepository(
                service = RetrofitInstance.createClubService(tokenManager)
            )
        }

        if (clubId.isBlank()) {
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
        } else {
            ClubDetailScreen(
                clubId = clubId,
                repository = clubRepository,
                onBack = {
                    navController.safeBack(AppRoutes.CLUBS)
                }
            )
        }
    }
}
