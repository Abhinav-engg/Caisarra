package com.abhinav.caisarra.presentation.navigation

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.abhinav.caisarra.data.repository.TournamentRepository
import com.abhinav.caisarra.presentation.tournament.CreateTournamentScreen
import com.abhinav.caisarra.presentation.tournament.CreateTournamentViewModel
import com.abhinav.caisarra.presentation.tournament.TournamentDetailScreen
import com.abhinav.caisarra.presentation.tournament.TournamentDetailViewModel
import com.abhinav.caisarra.presentation.tournament.TournamentListScreen
import com.abhinav.caisarra.presentation.tournament.TournamentListViewModel

fun NavGraphBuilder.tournamentGraph(
    navController: NavController,
    context: Context
) {
    composable(
        route = AppRoutes.TOURNAMENT_INVITE_LINK,
        arguments = listOf(
            navArgument("code") {
                type = NavType.StringType
            }
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = "https://caisaara.app/tournaments/invite/{code}" },
            navDeepLink { uriPattern = "https://caisaara.duckdns.org/tournaments/invite/{code}" }
        )
    ) { entry ->
        val code = entry.arguments?.getString("code").orEmpty()

        LinkResolverScreen(
            resolveRoute = {
                TournamentRepository.get(context)
                    .getTournamentByInvite(code)
                    .fold(
                        onSuccess = { tournament ->
                            AppRoutes.tournamentDetail(tournament.id, code)
                        },
                        onFailure = {
                            Toast.makeText(context, "Tournament link is invalid", Toast.LENGTH_SHORT).show()
                            AppRoutes.TOURNAMENTS
                        }
                    )
            },
            onResolved = { route ->
                navController.navigate(route) {
                    popUpTo(AppRoutes.TOURNAMENT_INVITE_LINK) {
                        inclusive = true
                    }
                }
            }
        )
    }

    composable(route = AppRoutes.TOURNAMENTS) {
        val listViewModel = screenViewModel {
            TournamentListViewModel(context as Application)
        }

        TournamentListScreen(
            viewModel = listViewModel,
            onCreate = {
                navController.navigate(AppRoutes.CREATE_TOURNAMENT)
            },
            onOpenTournament = { id, code ->
                navController.navigate(AppRoutes.tournamentDetail(id, code))
            },
            onBack = {
                navController.safeBack(AppRoutes.GAME_SETUP)
            }
        )
    }

    composable(route = AppRoutes.CREATE_TOURNAMENT) {
        val createViewModel = screenViewModel {
            CreateTournamentViewModel(context as Application)
        }

        CreateTournamentScreen(
            viewModel = createViewModel,
            onCreated = { id ->
                navController.navigate(AppRoutes.tournamentDetail(id)) {
                    popUpTo(AppRoutes.CREATE_TOURNAMENT) {
                        inclusive = true
                    }
                }
            },
            onBack = {
                navController.safeBack(AppRoutes.TOURNAMENTS)
            }
        )
    }

    composable(
        route = AppRoutes.TOURNAMENT_DETAIL,
        arguments = listOf(
            navArgument("id") {
                type = NavType.StringType
            },
            navArgument("code") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = "caisarra://tournament/{id}?code={code}" },
            navDeepLink { uriPattern = "https://caisaara.app/tournament/{id}?code={code}" },
            navDeepLink { uriPattern = "https://caisaara.duckdns.org/tournament/{id}?code={code}" }
        )
    ) { entry ->
        val id = entry.arguments?.getString("id").orEmpty()
        val code = entry.arguments?.getString("code")
        val detailViewModel = screenViewModel {
            TournamentDetailViewModel(context as Application, id, code)
        }

        TournamentDetailScreen(
            viewModel = detailViewModel,
            onOpenGame = { gameId ->
                navController.navigateOnce(AppRoutes.onlineGame(gameId))
            },
            onBack = {
                navController.safeBack(AppRoutes.TOURNAMENTS)
            }
        )
    }
}
