package com.abhinav.caisarra.data.repository

sealed interface NearbyFailure {
    data object Network : NearbyFailure
    data object Unauthorized : NearbyFailure
    data object Guest : NearbyFailure
    data object NearbyOff : NearbyFailure
    data object LocationNotSaved : NearbyFailure
    data object RateLimited : NearbyFailure
    data class Other(val message: String) : NearbyFailure
}