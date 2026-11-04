package com.abhinav.caisarra.presentation.navigation

import android.content.Context
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.repository.PuzzleRepository
import com.abhinav.caisarra.presentation.puzzles.CustomTacticsViewModel
import com.abhinav.caisarra.presentation.puzzles.DailyPuzzleViewModel
import com.abhinav.caisarra.presentation.puzzles.PuzzlesHubViewModel
import com.abhinav.caisarra.presentation.puzzles.RushViewModel
import com.abhinav.caisarra.presentation.screens.CustomTacticsScreen
import com.abhinav.caisarra.presentation.screens.DailyPuzzleScreen
import com.abhinav.caisarra.presentation.screens.PuzzlesScreen
import com.abhinav.caisarra.presentation.screens.RushScreen

fun NavGraphBuilder.puzzleGraph(
    navController: NavController,
    context: Context
) {
    composable(route = AppRoutes.PUZZLES) {
        val tokenManager = remember {
            TokenManager.get(context)
        }
        val hubViewModel = screenViewModel {
            PuzzlesHubViewModel(
                puzzleRepository = PuzzleRepository(
                    service = RetrofitInstance.createPuzzleService(tokenManager)
                )
            )
        }

        PuzzlesScreen(
            viewModel = hubViewModel,
            onPlayDailyPuzzle = {
                navController.navigateOnce(AppRoutes.DAILY_PUZZLE) {
                    launchSingleTop = true
                }
            },
            onPlayPuzzleRush = { mode ->
                navController.navigate(AppRoutes.puzzleRush(mode)) {
                    popUpTo(AppRoutes.PUZZLES) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            },
            onPlayCustomTactics = {
                navController.navigateOnce(AppRoutes.CUSTOM_TACTICS) {
                    launchSingleTop = true
                }
            }
        )
    }

    composable(route = AppRoutes.DAILY_PUZZLE) {
        val tokenManager = remember {
            TokenManager.get(context)
        }
        val dailyViewModel = screenViewModel {
            DailyPuzzleViewModel(
                puzzleRepository = PuzzleRepository(
                    service = RetrofitInstance.createPuzzleService(tokenManager)
                )
            )
        }

        DailyPuzzleScreen(
            viewModel = dailyViewModel,
            onBack = {
                navController.safeBack(AppRoutes.PUZZLES)
            }
        )
    }

    composable(route = AppRoutes.CUSTOM_TACTICS) {
        val tokenManager = remember {
            TokenManager.get(context)
        }
        val customTacticsViewModel = screenViewModel {
            CustomTacticsViewModel(
                repository = PuzzleRepository(
                    service = RetrofitInstance.createPuzzleService(tokenManager)
                )
            )
        }

        CustomTacticsScreen(
            viewModel = customTacticsViewModel,
            onBack = {
                navController.safeBack(AppRoutes.PUZZLES)
            }
        )
    }

    composable(
        route = AppRoutes.PUZZLE_RUSH,
        arguments = listOf(
            navArgument("mode") {
                type = NavType.StringType
                defaultValue = "3min"
            }
        )
    ) { backStackEntry ->
        val mode = backStackEntry.arguments?.getString("mode") ?: "3min"
        val tokenManager = remember {
            TokenManager.get(context)
        }
        val rushViewModel = screenViewModel {
            RushViewModel(
                puzzleRepository = PuzzleRepository(
                    service = RetrofitInstance.createPuzzleService(tokenManager)
                ),
                mode = mode
            )
        }

        RushScreen(
            viewModel = rushViewModel,
            onBack = {
                navController.safeBack(AppRoutes.PUZZLES)
            }
        )
    }
}
