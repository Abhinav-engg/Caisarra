package com.abhinav.caisarra.data.remote

sealed interface RefreshResult {
    data class Success(val accessToken: String) : RefreshResult
    data object Rejected : RefreshResult
    data object Failed : RefreshResult
}
