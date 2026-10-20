package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.remote.dto.LocationRequest
import com.abhinav.caisarra.data.remote.dto.LocationResponse
import com.abhinav.caisarra.data.remote.dto.NearbyPlayersResponse
import com.abhinav.caisarra.data.remote.dto.VisibilityRequest
import com.abhinav.caisarra.data.remote.dto.VisibilityResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.PUT

interface NearbyService {

    @PUT("api/nearby/location")
    suspend fun saveLocation(
        @Header("Authorization") auth: String,
        @Body body: LocationRequest
    ): Response<LocationResponse>

    @PATCH("api/nearby/visibility")
    suspend fun setVisibility(
        @Header("Authorization") auth: String,
        @Body body: VisibilityRequest
    ): Response<VisibilityResponse>

    @GET("api/nearby/players")
    suspend fun getPlayers(
        @Header("Authorization") auth: String
    ): Response<NearbyPlayersResponse>
}