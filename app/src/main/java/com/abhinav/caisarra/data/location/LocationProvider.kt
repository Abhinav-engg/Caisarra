package com.abhinav.caisarra.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class LocationProvider(context: Context) {

    private val appContext = context.applicationContext
    private val client = LocationServices.getFusedLocationProviderClient(appContext)

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    suspend fun current(): Pair<Double, Double>? = withTimeoutOrNull(5000) {
        suspendCancellableCoroutine { continuation ->
            val cts = CancellationTokenSource()
            continuation.invokeOnCancellation { cts.cancel() }

            fun finish(location: Location?) {
                if (continuation.isActive) {
                    continuation.resume(location?.let { it.latitude to it.longitude })
                }
            }

            fun requestCurrentLocation() {
                if (!continuation.isActive) return
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { location -> finish(location) }
                    .addOnFailureListener { finish(null) }
            }

            client.lastLocation
                .addOnSuccessListener { last ->
                    if (last != null) finish(last) else requestCurrentLocation()
                }
                .addOnFailureListener { requestCurrentLocation() }
        }
    }
}
