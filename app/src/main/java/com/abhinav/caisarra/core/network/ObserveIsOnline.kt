package com.abhinav.caisarra.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

fun Context.observeIsOnline(): Flow<Boolean> = callbackFlow {
    val manager = getSystemService(ConnectivityManager::class.java)
    fun NetworkCapabilities?.hasInternet() =
        this?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            trySend(capabilities.hasInternet())
        }

        override fun onLost(network: Network) {
            trySend(false)
        }
    }
    trySend(manager.getNetworkCapabilities(manager.activeNetwork).hasInternet())
    manager.registerDefaultNetworkCallback(callback)
    awaitClose { manager.unregisterNetworkCallback(callback) }
}.distinctUntilChanged()
