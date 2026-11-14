package com.abhinav.caisarra.features.minimates.presentation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

fun AnimatedContentTransitionScope<MiniMatesUiState>.miniMatesTransition(): ContentTransform {
    val from = initialState.screenKey
    val to = targetState.screenKey
    val direction = if (to > from) 1 else -1
    return when {
        to == 0 -> (slideInVertically { it } + fadeIn()) togetherWith fadeOut()
        from == 0 -> (fadeIn() togetherWith (slideOutVertically { it } + fadeOut())).apply { targetContentZIndex = -1f }
        else -> (slideInHorizontally { it * direction } + fadeIn()) togetherWith (slideOutHorizontally { -it * direction } + fadeOut())
    }
}
