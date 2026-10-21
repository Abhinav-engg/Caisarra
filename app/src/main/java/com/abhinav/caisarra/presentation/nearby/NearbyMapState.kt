package com.abhinav.caisarra.presentation.nearby

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Stable
class NearbyMapState {

    var mapView: MapView? = null
    var version by mutableIntStateOf(0)
    var ready by mutableStateOf(false)
    var userMoved by mutableStateOf(false)

    fun project(geoPoint: GeoPoint): Offset? {
        version
        val mv = mapView ?: return null
        val projection = mv.projection ?: return null
        val point = android.graphics.Point()
        projection.toPixels(geoPoint, point)
        return Offset(point.x.toFloat(), point.y.toFloat())
    }

    fun zoomLevel(): Double = mapView?.zoomLevelDouble ?: 0.0

    fun fitAround(latitude: Double, longitude: Double, radiusKm: Double, animated: Boolean = true) {
        val mv = mapView ?: return
        val effectiveRadius = radiusKm.coerceAtLeast(2.0) * 1.15
        val distanceMeters = effectiveRadius * 1000.0
        val dLat = distanceMeters / METERS_PER_DEGREE
        val dLng = distanceMeters / (METERS_PER_DEGREE * lngScale(latitude))
        val box = BoundingBox(
            latitude + dLat,
            longitude + dLng,
            latitude - dLat,
            longitude - dLng
        )
        if (mv.width == 0 || mv.height == 0) {
            mv.post { mv.zoomToBoundingBox(box, animated, 120) }
        } else {
            mv.zoomToBoundingBox(box, animated, 120)
        }
    }

    fun fitAround(latitude: Double, longitude: Double, radiusKm: Float, animated: Boolean = true) =
        fitAround(latitude, longitude, radiusKm.toDouble(), animated)

    fun focus(position: Offset) {
        val mv = mapView ?: return
        val width = mv.width.toFloat()
        val height = mv.height.toFloat()
        if (width <= 0f || height <= 0f) return
        val target = Offset(width / 2f, height * 0.38f)
        val centerPixelX = (width / 2f + (position.x - target.x)).toInt()
        val centerPixelY = (height / 2f + (position.y - target.y)).toInt()
        val adjustedCenter = mv.projection?.fromPixels(centerPixelX, centerPixelY) ?: return
        userMoved = true
        mv.controller?.animateTo(GeoPoint(adjustedCenter.latitude, adjustedCenter.longitude))
    }

    fun zoomInto(center: GeoPoint) {
        val mv = mapView ?: return
        userMoved = true
        val targetZoom = mv.zoomLevelDouble + 2.2
        mv.controller?.setZoom(targetZoom)
        mv.controller?.animateTo(center)
    }
}
