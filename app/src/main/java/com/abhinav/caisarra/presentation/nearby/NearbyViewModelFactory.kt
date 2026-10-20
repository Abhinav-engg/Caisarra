package com.abhinav.caisarra.presentation.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abhinav.caisarra.data.location.LocationProvider
import com.abhinav.caisarra.data.repository.NearbyRepository

class NearbyViewModelFactory(
    private val repository: NearbyRepository,
    private val locationProvider: LocationProvider,
    private val tokenProvider: suspend () -> String?
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        NearbyViewModel(repository, locationProvider, tokenProvider) as T
}