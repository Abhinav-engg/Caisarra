
package com.abhinav.caisarra.presentation.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.location.LocationProvider
import com.abhinav.caisarra.data.repository.NearbyFailure
import com.abhinav.caisarra.data.repository.NearbyRepository
import com.abhinav.caisarra.data.repository.NearbyResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NearbyViewModel(
    private val repository: NearbyRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _state = MutableStateFlow(NearbyUiState())
    val state: StateFlow<NearbyUiState> = _state.asStateFlow()

    private var operationJob: Job? = null

    init {
        loadPlayers()
    }

    fun select(id: Int?) {
        _state.update {
            it.copy(selectedId = id)
        }
    }

    fun loadPlayers(silent: Boolean = false) {
        operationJob?.cancel()

        operationJob = viewModelScope.launch {
            fetchPlayers(silent)
        }
    }

    private suspend fun fetchPlayers(silent: Boolean = false) {
        if (!silent) {
            _state.update {
                it.copy(
                    loading = true,
                    error = null
                )
            }
        }

        when (val result = repository.getPlayers()) {
            is NearbyResult.Success -> {
                _state.update { current ->
                    current.copy(
                        loading = false,
                        visible = true,
                        guest = false,
                        error = null,
                        players = result.data.players,
                        radiusKm = result.data.radiusKm,
                        selectedId = current.selectedId?.takeIf { id ->
                            result.data.players.any {
                                it.userId == id
                            }
                        }
                    )
                }

                loadMyLocation()
            }

            is NearbyResult.Failure -> {
                handle(result.failure)
            }
        }
    }

    fun refresh(silent: Boolean = false) {
        if (_state.value.guest) return
        if (!locationProvider.hasPermission()) return
        if (_state.value.visible) {
            loadPlayers(silent)
        } else {
            enable()
        }
    }

    fun enable() {
        if (!locationProvider.hasPermission()) {
            permissionDenied()
            return
        }

        operationJob?.cancel()

        operationJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    loading = true,
                    error = null
                )
            }

            try {
                val location = locationProvider.current()

                if (location == null) {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = "Could not get your location"
                        )
                    }
                    return@launch
                }

                val latitude = location.first
                val longitude = location.second

                _state.update {
                    it.copy(
                        myLatitude = latitude,
                        myLongitude = longitude
                    )
                }

                when (
                    val locationResult = repository.saveLocation(
                        latitude,
                        longitude
                    )
                ) {
                    is NearbyResult.Success -> {
                    }

                    is NearbyResult.Failure -> {
                        handle(locationResult.failure)
                        return@launch
                    }
                }

                when (
                    val visibilityResult =
                        repository.setVisibility(true)
                ) {
                    is NearbyResult.Success -> {
                        _state.update {
                            it.copy(
                                visible = true,
                                error = null
                            )
                        }
                    }

                    is NearbyResult.Failure -> {
                        handle(visibilityResult.failure)
                        return@launch
                    }
                }

                fetchPlayers()

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loading = false,
                        error = "Could not enable Nearby"
                    )
                }
            }
        }
    }

    fun disable() {
        operationJob?.cancel()

        operationJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    loading = true,
                    error = null
                )
            }

            when (
                val result = repository.setVisibility(false)
            ) {
                is NearbyResult.Success -> {
                    clearNearby()
                }

                is NearbyResult.Failure -> {
                    if (
                        result.failure ==
                        NearbyFailure.LocationNotSaved
                    ) {
                        clearNearby()
                    } else {
                        handle(result.failure)
                    }
                }
            }
        }
    }

    private fun clearNearby() {
        _state.update {
            it.copy(
                loading = false,
                visible = false,
                players = emptyList(),
                selectedId = null,
                error = null
            )
        }
    }

    fun permissionDenied() {
        _state.update {
            it.copy(
                loading = false,
                error = "Location permission is needed to find players near you"
            )
        }
    }

    private fun loadMyLocation() {
        if (
            _state.value.myLatitude != null ||
            !locationProvider.hasPermission()
        ) {
            return
        }

        viewModelScope.launch {
            val location =
                locationProvider.current() ?: return@launch

            _state.update {
                it.copy(
                    myLatitude = location.first,
                    myLongitude = location.second
                )
            }
        }
    }

    private fun handle(failure: NearbyFailure) {
        when (failure) {
            NearbyFailure.NearbyOff -> {
                _state.update {
                    it.copy(
                        loading = false,
                        visible = false,
                        players = emptyList(),
                        selectedId = null
                    )
                }
            }

            NearbyFailure.Guest -> {
                _state.update {
                    it.copy(
                        loading = false,
                        guest = true,
                        visible = false
                    )
                }
            }

            else -> {
                _state.update {
                    it.copy(
                        loading = false,
                        error = messageFor(failure)
                    )
                }
            }
        }
    }

    private fun messageFor(
        failure: NearbyFailure
    ): String = when (failure) {
        NearbyFailure.Network ->
            "No internet connection"

        NearbyFailure.Unauthorized ->
            "Session expired, please log in again"

        NearbyFailure.RateLimited ->
            "Too many requests, try again in a minute"

        NearbyFailure.LocationNotSaved ->
            "Turn on Nearby to share your location"

        is NearbyFailure.Other ->
            failure.message

        else ->
            "Something went wrong"
    }
}
