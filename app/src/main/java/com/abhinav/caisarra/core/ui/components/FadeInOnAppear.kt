package com.abhinav.caisarra.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun Modifier.fadeInOnAppear(key: Any? = Unit): Modifier {
    val alpha = remember(key) { Animatable(0f) }
    LaunchedEffect(key) { alpha.animateTo(1f, tween(400)) }
    return graphicsLayer { this.alpha = alpha.value }
}
