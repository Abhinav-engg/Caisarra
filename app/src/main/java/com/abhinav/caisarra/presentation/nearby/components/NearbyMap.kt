package com.abhinav.caisarra.presentation.nearby.components

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.abhinav.caisarra.data.remote.dto.NearbyPlayer
import com.abhinav.caisarra.presentation.nearby.NearbyColors
import com.abhinav.caisarra.presentation.nearby.NearbyMapState
import com.abhinav.caisarra.presentation.nearby.buildClusters
import com.abhinav.caisarra.presentation.nearby.playerGeo
import com.abhinav.caisarra.presentation.nearby.ringPoint
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.TilesOverlay
import java.io.File

private var osmdroidConfigured = false

private fun configureOsmdroid(context: android.content.Context) {
    if (osmdroidConfigured) return
    val config = Configuration.getInstance()
    config.userAgentValue = context.packageName
    val basePath = File(context.filesDir, "osmdroid")
    val tileCache = File(basePath, "tiles")
    config.osmdroidBasePath = basePath
    config.osmdroidTileCache = tileCache
    config.tileFileSystemCacheMaxBytes = 800L * 1024L * 1024L
    config.tileFileSystemCacheTrimBytes = 600L * 1024L * 1024L
    osmdroidConfigured = true
}

@Composable
fun NearbyMap(
    state: NearbyMapState,
    myLatitude: Double,
    myLongitude: Double,
    players: List<NearbyPlayer>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { 56.dp.toPx() }
    val anchorPx = with(density) { 22.dp.toPx() }
    val selectedAnchorPx = with(density) { 28.dp.toPx() }

    DisposableEffect(Unit) {
        onDispose {
            state.mapView?.onDetach()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NearbyColors.Background)
            .clipToBounds()
    ) {
        AndroidView(
            factory = { ctx ->
                configureOsmdroid(ctx)
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                    minZoomLevel = 3.0
                    maxZoomLevel = 19.0
                    isHorizontalMapRepetitionEnabled = false
                    isVerticalMapRepetitionEnabled = false

                    val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            onSelect(null)
                            return false
                        }
                        override fun longPressHelper(p: GeoPoint): Boolean = false
                    })
                    overlays.add(eventsOverlay)

                    addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            state.version++
                            return false
                        }
                        override fun onZoom(event: ZoomEvent?): Boolean {
                            state.version++
                            return false
                        }
                    })

                    setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_MOVE) {
                            state.userMoved = true
                        }
                        false
                    }

                    state.mapView = this
                    state.ready = true
                }
            },
            update = { view ->
                val tagValue = "$myLatitude,$myLongitude"
                if (view.tag != tagValue) {
                    view.tag = tagValue
                    view.overlays.removeAll { it is Polygon }
                    val center = GeoPoint(myLatitude, myLongitude)
                    val ringSteps = listOf(1, 2, 5, 10, 25, 50)
                    for (km in ringSteps) {
                        val poly = Polygon(view).apply {
                            points = Polygon.pointsAsCircle(center, km * 1000.0)
                            fillPaint.color = android.graphics.Color.TRANSPARENT
                            outlinePaint.color = 0x1FFFFFFF.toInt()
                            outlinePaint.strokeWidth = 2f
                            outlinePaint.style = android.graphics.Paint.Style.STROKE
                            infoWindow = null
                            setOnClickListener { _, _, _ -> false }
                        }
                        view.overlays.add(poly)
                    }
                    view.invalidate()
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        val userGeo = GeoPoint(myLatitude, myLongitude)
        val ringStepsKm = listOf(1, 2, 5, 10, 25, 50)
        ringStepsKm.forEach { km ->
            val ringGeo = ringPoint(userGeo, km)
            val ringOffset = state.project(ringGeo)
            if (ringOffset != null) {
                RingLabel(
                    text = "$km km",
                    modifier = Modifier.anchoredAt(ringOffset, 0f)
                )
            }
        }

        val youOffset = state.project(userGeo)
        if (youOffset != null) {
            YouMarker(modifier = Modifier.anchoredAt(youOffset, anchorPx))
        }

        val otherPlayers = players.filter { it.userId != selectedId }
        val located = otherPlayers.map { it to playerGeo(it, myLatitude, myLongitude) }
        val clusters = buildClusters(located, state::project, thresholdPx)

        clusters.forEach { cluster ->
            val position = cluster.position
            if (cluster.players.size == 1) {
                val player = cluster.players.first()
                PlayerMarker(
                    player = player,
                    selected = false,
                    onClick = {
                        onSelect(player.userId)
                        state.focus(position)
                    },
                    modifier = Modifier.anchoredAt(position, anchorPx)
                )
            } else {
                ClusterMarker(
                    count = cluster.players.size,
                    onClick = { state.zoomInto(cluster.geoPoint) },
                    modifier = Modifier.anchoredAt(position, anchorPx)
                )
            }
        }

        val selectedPlayer = players.firstOrNull { it.userId == selectedId }
        if (selectedPlayer != null) {
            val selectedGeo = playerGeo(selectedPlayer, myLatitude, myLongitude)
            val selectedOffset = state.project(selectedGeo)
            if (selectedOffset != null) {
                PlayerMarker(
                    player = selectedPlayer,
                    selected = true,
                    onClick = {},
                    modifier = Modifier.anchoredAt(selectedOffset, selectedAnchorPx)
                )
            }
        }
    }
}
