package com.abhinav.caisarra.features.dailytactics.presentation

import androidx.annotation.DrawableRes
import com.abhinav.caisarra.R
import com.abhinav.caisarra.presentation.navigation.AppRoutes

enum class DailyTactic(
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int,
    val route: String
) {
    PUZZLE_RUSH("Puzzle Rush", "3 min sprint", R.drawable.ic_tactic_puzzle_rush, AppRoutes.puzzleRush("3min")),
    SURVIVAL("Survival", "3 lives", R.drawable.ic_tactic_survival, AppRoutes.puzzleRush("survival")),
    CUSTOM_TACTICS("Custom Tactics", "Your themes", R.drawable.ic_tactic_custom, AppRoutes.CUSTOM_TACTICS),
    MINI_MATES("Mini Mates", "Mate in 1-3", R.drawable.ic_tactic_mini_mates, AppRoutes.MINI_MATES)
}
