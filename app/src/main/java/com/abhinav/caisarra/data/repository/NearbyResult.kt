package com.abhinav.caisarra.data.repository

sealed interface NearbyResult<out T> {
    data class Success<T>(val data: T) : NearbyResult<T>
    data class Failure(val failure: NearbyFailure) : NearbyResult<Nothing>
}