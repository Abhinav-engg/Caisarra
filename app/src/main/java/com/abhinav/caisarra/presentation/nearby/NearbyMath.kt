package com.abhinav.caisarra.presentation.nearby

import androidx.compose.ui.geometry.Offset
import com.abhinav.caisarra.data.remote.dto.NearbyPlayer
import kotlin.math.cos
import kotlin.math.sin

private const val GOLDEN_ANGLE = 2.3999632f

data class MapCluster(
    val players: List<NearbyPlayer>,
    val position: Offset
)

fun playerOffset(player: NearbyPlayer, scale: Float): Offset {
    val angle = player.userId * GOLDEN_ANGLE
    val radius = player.distanceKm * scale
    return Offset(cos(angle) * radius, sin(angle) * radius)
}

fun buildClusters(
    players: List<NearbyPlayer>,
    center: Offset,
    scale: Float,
    thresholdPx: Float
): List<MapCluster> {
    val groups = mutableListOf<MutableList<NearbyPlayer>>()
    val anchors = mutableListOf<Offset>()
    players.forEach { player ->
        val position = center + playerOffset(player, scale)
        val index = anchors.indexOfFirst { (it - position).getDistance() < thresholdPx }
        if (index >= 0) {
            groups[index].add(player)
        } else {
            groups.add(mutableListOf(player))
            anchors.add(position)
        }
    }
    return groups.map { group ->
        val points = group.map { center + playerOffset(it, scale) }
        MapCluster(
            players = group,
            position = Offset(
                points.map { it.x }.average().toFloat(),
                points.map { it.y }.average().toFloat()
            )
        )
    }
}