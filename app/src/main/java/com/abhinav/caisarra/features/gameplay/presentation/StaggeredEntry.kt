package com.abhinav.caisarra.features.gameplay.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Modifier

fun Modifier.staggeredEntry(scope: AnimatedVisibilityScope, index: Int, count: Int): Modifier = with(scope) {
    val enterDelay = 70 + (count - 1 - index) * 55
    animateEnterExit(
        enter = slideInVertically(tween(320, enterDelay, FastOutSlowInEasing)) { it } +
            fadeIn(tween(260, enterDelay)) +
            scaleIn(tween(320, enterDelay), initialScale = 0.92f),
        exit = slideOutVertically(tween(160, index * 30)) { it / 2 } + fadeOut(tween(140, index * 30))
    )
}
