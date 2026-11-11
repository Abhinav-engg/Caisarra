package com.abhinav.caisarra.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry

private const val MENU_SLIDE_MS = 300

fun AnimatedContentTransitionScope<NavBackStackEntry>.menuEnterTransition(): EnterTransition =
    if (initialState.destination.route == AppRoutes.MENU) {
        slideIntoContainer(SlideDirection.Left, tween(MENU_SLIDE_MS))
    } else {
        fadeIn(tween(700))
    }

fun AnimatedContentTransitionScope<NavBackStackEntry>.menuPopExitTransition(): ExitTransition =
    if (targetState.destination.route == AppRoutes.MENU) {
        slideOutOfContainer(SlideDirection.Right, tween(MENU_SLIDE_MS))
    } else {
        fadeOut(tween(700))
    }
