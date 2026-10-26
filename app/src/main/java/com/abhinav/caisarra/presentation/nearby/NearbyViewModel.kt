package com.abhinav.caisarra.presentation.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.location.LocationProvider
import com.abhinav.caisarra.data.repository.NearbyFailure
import com.abhinav.caisarra.data.repository.NearbyRepository
import com.abhinav.caisarra.data.repository.NearbyResult
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

    init {
        loadPlayers()
    }

    fun select(id: Int?) {
        _state.update { it.copy(selectedId = id) }
    }

    fun loadPlayers(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _state.update { it.copy(loading = true, error = null) }
            }
            when (val result = repository.getPlayers()) {
                is NearbyResult.Success -> {
                    _state.update { current ->
                        current.copy(
                            loading = false,
                            visible = true,
                            players = result.data.players,
                            radiusKm = result.data.radiusKm,
                            selectedId = current.selectedId?.takeIf { id ->
                                result.data.players.any { it.userId == id }
                            }
                        )
                    }
                    loadMyLocation()
                }
                is NearbyResult.Failure -> handle(result.failure)
            }
        }
    }

    fun refresh(silent: Boolean = false) {
        if (_state.value.guest) return
        viewModelScope.launch {
            if (!silent) {
                _state.update { it.copy(loading = true, error = null) }
            }
            if (locationProvider.hasPermission()) {
                val location = locationProvider.current()
                if (location != null) {
                    _state.update { it.copy(myLatitude = location.first, myLongitude = location.second) }
                    repository.saveLocation(location.first, location.second)
                }
            }
            when (val result = repository.getPlayers()) {
                is NearbyResult.Success -> {
                    _state.update { current ->
                        current.copy(
                            loading = false,
                            visible = true,
                            players = result.data.players,
                            radiusKm = result.data.radiusKm,
                            selectedId = current.selectedId?.takeIf { id ->
                                result.data.players.any { it.userId == id }
                            }
                        )
                    }
                    if (_state.value.myLatitude == null) {
                        loadMyLocation()
                    }
                }
                is NearbyResult.Failure -> handle(result.failure)
            }
        }
    }

    fun enable() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val location = locationProvider.current()
            if (location == null) {
                _state.update { it.copy(loading = false, error = "Could not get your location") }
                return@launch
            }
            _state.update { it.copy(myLatitude = location.first, myLongitude = location.second) }
            when (val result = repository.saveLocation(location.first, location.second)) {
                is NearbyResult.Success -> loadPlayers()
                is NearbyResult.Failure -> handle(result.failure)
            }
        }
    }

    fun disable() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val result = repository.setVisibility(false)
            if (result is NearbyResult.Failure && result.failure != NearbyFailure.LocationNotSaved) {
                handle(result.failure)
            } else {
                _state.update {
                    it.copy(loading = false, visible = false, players = emptyList(), selectedId = null)
                }
            }
        }
    }

    fun permissionDenied() {
        _state.update {
            it.copy(loading = false, error = "Location permission is needed to find players near you")
        }
    }

    private fun loadMyLocation() {
        if (_state.value.myLatitude != null || !locationProvider.hasPermission()) return
        viewModelScope.launch {
            val location = locationProvider.current() ?: return@launch
            _state.update { it.copy(myLatitude = location.first, myLongitude = location.second) }
        }
    }

    private fun handle(failure: NearbyFailure) {
        when (failure) {
            NearbyFailure.NearbyOff -> _state.update {
                it.copy(loading = false, visible = false, players = emptyList(), selectedId = null)
            }
            NearbyFailure.Guest -> _state.update { it.copy(loading = false, guest = true) }
            else -> _state.update { it.copy(loading = false, error = messageFor(failure)) }
        }
    }

    private fun messageFor(failure: NearbyFailure): String = when (failure) {
        NearbyFailure.Network -> "No internet connection"
        NearbyFailure.Unauthorized -> "Session expired, please log in again"
        NearbyFailure.RateLimited -> "Too many requests, try again in a minute"
        NearbyFailure.LocationNotSaved -> "Turn on Nearby to share your location"
        is NearbyFailure.Other -> failure.message
        else -> "Something went wrong"
    }
}
