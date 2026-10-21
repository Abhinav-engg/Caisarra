package com.abhinav.caisarra.presentation.nearby.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.data.remote.dto.NearbyPlayer
import com.abhinav.caisarra.presentation.nearby.NearbyColors
import com.abhinav.caisarra.presentation.nearby.NearbyMapState
import com.abhinav.caisarra.presentation.nearby.buildClusters
import com.abhinav.caisarra.presentation.nearby.playerOffset
import kotlinx.coroutines.launch
import kotlin.math.sin

private val RingStepsKm = listOf(1, 2, 5, 10, 25, 50)

@Composable
fun NearbyMap(
    state: NearbyMapState,
    players: List<NearbyPlayer>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 56.dp.toPx() }
    val anchorPx = with(density) { 22.dp.toPx() }
    val selectedAnchorPx = with(density) { 28.dp.toPx() }
    val center = state.center
    val scale = state.scale
    val selected = players.firstOrNull { it.userId == selectedId }
    val clusters = buildClusters(players.filter { it.userId != selectedId }, center, scale, thresholdPx)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NearbyColors.Background)
            .clipToBounds()
            .onSizeChanged { state.size = it }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    state.transform(centroid, pan, zoom)
                }
            }
            .pointerInput(onSelect) {
                detectTapGestures { onSelect(null) }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) { drawMap(center, scale) }

        RingStepsKm.forEach { km ->
            val radius = km * scale
            val labelY = center.y + radius
            if (radius > 60f && labelY in 0f..state.size.height.toFloat()) {
                RingLabel(
                    text = "$km km",
                    modifier = Modifier.anchoredAt(Offset(center.x, labelY), 0f)
                )
            }
        }

        YouMarker(Modifier.anchoredAt(center, anchorPx))

        clusters.forEach { cluster ->
            val position = cluster.position
            if (state.isVisible(position)) {
                if (cluster.players.size == 1) {
                    val player = cluster.players.first()
                    PlayerMarker(
                        player = player,
                        selected = false,
                        onClick = {
                            onSelect(player.userId)
                            scope.launch { state.focus(position) }
                        },
                        modifier = Modifier.anchoredAt(position, anchorPx)
                    )
                } else {
                    ClusterMarker(
                        count = cluster.players.size,
                        onClick = { scope.launch { state.zoomInto(position) } },
                        modifier = Modifier.anchoredAt(position, anchorPx)
                    )
                }
            }
        }

        selected?.let { player ->
            PlayerMarker(
                player = player,
                selected = true,
                onClick = {},
                modifier = Modifier.anchoredAt(center + playerOffset(player, scale), selectedAnchorPx)
            )
        }
    }
}

private fun DrawScope.drawMap(center: Offset, scale: Float) {
    val ringStroke = Stroke(width = 1.dp.toPx())
    RingStepsKm.forEach { km ->
        val radius = km * scale
        if (radius > 24f) drawCircle(NearbyColors.Ring, radius, center, style = ringStroke)
    }

    val roadStroke = Stroke(width = 1.5.dp.toPx())
    for (k in -6..6) {
        val horizontal = Path()
        val vertical = Path()
        for (i in 0..100) {
            val along = -100f + i * 2f
            val across = k * 8f + sin(along * 0.22f + k) * 1.6f
            val h = Offset(center.x + along * scale, center.y + across * scale)
            val v = Offset(center.x + across * scale, center.y + along * scale)
            if (i == 0) {
                horizontal.moveTo(h.x, h.y)
                vertical.moveTo(v.x, v.y)
            } else {
                horizontal.lineTo(h.x, h.y)
                vertical.lineTo(v.x, v.y)
            }
        }
        drawPath(horizontal, NearbyColors.Road, style = roadStroke)
        drawPath(vertical, NearbyColors.Road, style = roadStroke)
    }
}