package com.abhinav.caisarra.data.remote.api

import com.abhinav.caisarra.data.remote.dto.LocationRequest
import com.abhinav.caisarra.data.remote.dto.LocationResponse
import com.abhinav.caisarra.data.remote.dto.NearbyPlayersResponse
import com.abhinav.caisarra.data.remote.dto.VisibilityRequest
import com.abhinav.caisarra.data.remote.dto.VisibilityResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT

interface NearbyService {

    @PUT("api/nearby/location")
    suspend fun saveLocation(@Body body: LocationRequest): Response<LocationResponse>

    @PATCH("api/nearby/visibility")
    suspend fun setVisibility(@Body body: VisibilityRequest): Response<VisibilityResponse>

    @GET("api/nearby/players")
    suspend fun getPlayers(): Response<NearbyPlayersResponse>
}