package com.abhinav.caisarra.presentation.nearby

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.math.min

@Stable
class NearbyMapState {

    var size by mutableStateOf(IntSize.Zero)

    var scale by mutableFloatStateOf(40f)
        private set

    var center by mutableStateOf(Offset.Zero)
        private set

    private var userMoved = false

    private val screenCenter: Offset
        get() = Offset(size.width / 2f, size.height / 2f)

    private val minScale: Float
        get() = if (size == IntSize.Zero) 1f else min(size.width, size.height) / 2f / MAX_RADIUS_KM

    private fun fitScale(farthestKm: Float): Float =
        (min(size.width, size.height) * 0.36f / farthestKm.coerceAtLeast(2f))
            .coerceIn(minScale, MAX_SCALE)

    fun fitIfIdle(farthestKm: Float) {
        if (size == IntSize.Zero || userMoved) return
        scale = fitScale(farthestKm)
        center = screenCenter
    }

    fun transform(centroid: Offset, pan: Offset, zoom: Float) {
        userMoved = true
        val newScale = (scale * zoom).coerceIn(minScale, MAX_SCALE)
        val factor = newScale / scale
        center = centroid + (center - centroid) * factor + pan
        scale = newScale
    }

    fun isVisible(position: Offset): Boolean =
        position.x in -MARGIN..size.width + MARGIN && position.y in -MARGIN..size.height + MARGIN

    suspend fun recenter(farthestKm: Float) {
        userMoved = false
        animateTo(fitScale(farthestKm), screenCenter)
    }

    suspend fun zoomInto(position: Offset) {
        userMoved = true
        val target = (scale * 2.5f).coerceAtMost(MAX_SCALE)
        val factor = target / scale
        animateTo(target, screenCenter + (center - position) * factor)
    }

    suspend fun focus(position: Offset) {
        userMoved = true
        val target = Offset(size.width / 2f, size.height * 0.38f)
        animateTo(scale, center + (target - position))
    }

    private suspend fun animateTo(targetScale: Float, targetCenter: Offset) {
        val startScale = scale
        val startCenter = center
        animate(0f, 1f, animationSpec = tween(350)) { progress, _ ->
            scale = startScale + (targetScale - startScale) * progress
            center = startCenter + (targetCenter - startCenter) * progress
        }
    }

    private companion object {
        const val MAX_SCALE = 300f
        const val MAX_RADIUS_KM = 55f
        const val MARGIN = 120f
    }
}