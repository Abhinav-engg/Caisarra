package com.abhinav.caisarra.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun rememberCountUp(
    target: Int,
    from: Int = 0,
    delayMillis: Long = 0L,
    durationMillis: Int = 900
): Int {
    val value = remember { Animatable(from.toFloat()) }
    LaunchedEffect(target, from) {
        value.snapTo(from.toFloat())
        delay(delayMillis)
        value.animateTo(target.toFloat(), tween(durationMillis, easing = FastOutSlowInEasing))
    }
    return value.value.roundToInt()
}
