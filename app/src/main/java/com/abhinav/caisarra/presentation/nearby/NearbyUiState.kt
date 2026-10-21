package com.abhinav.caisarra.presentation.nearby

import com.abhinav.caisarra.data.remote.dto.NearbyPlayer

data class NearbyUiState(
    val loading: Boolean = true,
    val visible: Boolean = false,
    val guest: Boolean = false,
    val players: List<NearbyPlayer> = emptyList(),
    val radiusKm: Int = 50,
    val selectedId: Int? = null,
    val myLatitude: Double? = null,
    val myLongitude: Double? = null,
    val error: String? = null
)