package com.abhinav.caisarra.presentation.nearby

import androidx.compose.ui.geometry.Offset
import com.abhinav.caisarra.data.remote.dto.NearbyPlayer
import org.osmdroid.util.GeoPoint
import kotlin.math.cos
import kotlin.math.sin

const val METERS_PER_DEGREE = 111_320.0
private const val GOLDEN_ANGLE = 2.3999632

data class MapCluster(
    val players: List<NearbyPlayer>,
    val position: Offset,
    val geoPoint: GeoPoint
)

fun lngScale(latitude: Double): Double =
    cos(Math.toRadians(latitude)).coerceAtLeast(0.00001)

fun playerGeo(player: NearbyPlayer, myLatitude: Double, myLongitude: Double): GeoPoint {
    if (player.latitude != null && player.longitude != null) {
        return GeoPoint(player.latitude, player.longitude)
    }
    val distanceMeters = player.distanceKm * 1000.0
    val bearing = player.userId * GOLDEN_ANGLE
    val dLat = (distanceMeters * cos(bearing)) / METERS_PER_DEGREE
    val dLng = (distanceMeters * sin(bearing)) / (METERS_PER_DEGREE * lngScale(myLatitude))
    return GeoPoint(myLatitude + dLat, myLongitude + dLng)
}

fun ringPoint(center: GeoPoint, km: Int): GeoPoint {
    val dLat = (km * 1000.0) / METERS_PER_DEGREE
    return GeoPoint(center.latitude - dLat, center.longitude)
}

fun ringPoint(center: GeoPoint, km: Double): GeoPoint {
    val dLat = (km * 1000.0) / METERS_PER_DEGREE
    return GeoPoint(center.latitude - dLat, center.longitude)
}

fun buildClusters(
    located: List<Pair<NearbyPlayer, GeoPoint>>,
    project: (GeoPoint) -> Offset?,
    thresholdPx: Float
): List<MapCluster> {
    val groups = mutableListOf<MutableList<Pair<NearbyPlayer, GeoPoint>>>()
    val anchors = mutableListOf<Offset>()

    for (item in located) {
        val pos = project(item.second) ?: continue
        val index = anchors.indexOfFirst { (it - pos).getDistance() < thresholdPx }
        if (index >= 0) {
            groups[index].add(item)
        } else {
            groups.add(mutableListOf(item))
            anchors.add(pos)
        }
    }

    return groups.map { group ->
        val avgLat = group.map { it.second.latitude }.average()
        val avgLng = group.map { it.second.longitude }.average()
        val geoPoint = GeoPoint(avgLat, avgLng)
        val position = project(geoPoint) ?: Offset(
            group.mapNotNull { project(it.second)?.x }.average().toFloat(),
            group.mapNotNull { project(it.second)?.y }.average().toFloat()
        )
        MapCluster(
            players = group.map { it.first },
            position = position,
            geoPoint = geoPoint
        )
    }
}
