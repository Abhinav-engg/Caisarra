package com.abhinav.caisarra.data.repository

import com.abhinav.caisarra.data.remote.NearbyService
import com.abhinav.caisarra.data.remote.dto.ErrorResponse
import com.abhinav.caisarra.data.remote.dto.LocationRequest
import com.abhinav.caisarra.data.remote.dto.LocationResponse
import com.abhinav.caisarra.data.remote.dto.NearbyPlayersResponse
import com.abhinav.caisarra.data.remote.dto.VisibilityRequest
import com.abhinav.caisarra.data.remote.dto.VisibilityResponse
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.IOException

class NearbyRepository(private val service: NearbyService) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun saveLocation(
        token: String,
        latitude: Double,
        longitude: Double
    ): NearbyResult<LocationResponse> =
        call { service.saveLocation(bearer(token), LocationRequest(latitude, longitude)) }

    suspend fun setVisibility(token: String, enabled: Boolean): NearbyResult<VisibilityResponse> =
        call { service.setVisibility(bearer(token), VisibilityRequest(enabled)) }

    suspend fun getPlayers(token: String): NearbyResult<NearbyPlayersResponse> =
        call { service.getPlayers(bearer(token)) }

    private fun bearer(token: String) = "Bearer $token"

    private suspend fun <T> call(block: suspend () -> Response<T>): NearbyResult<T> =
        try {
            val response = block()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                NearbyResult.Success(body)
            } else {
                NearbyResult.Failure(mapError(response.code(), response.errorBody()?.string()))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            NearbyResult.Failure(NearbyFailure.Network)
        } catch (e: Exception) {
            NearbyResult.Failure(NearbyFailure.Other("Something went wrong"))
        }

    private fun mapError(code: Int, raw: String?): NearbyFailure {
        val message = raw?.let {
            runCatching { json.decodeFromString<ErrorResponse>(it).error }.getOrNull()
        }
        return when {
            code == 401 -> NearbyFailure.Unauthorized
            code == 403 && message == GUEST_MESSAGE -> NearbyFailure.Guest
            code == 403 -> NearbyFailure.NearbyOff
            code == 400 && message == LOCATION_NOT_SAVED -> NearbyFailure.LocationNotSaved
            code == 429 -> NearbyFailure.RateLimited
            else -> NearbyFailure.Other(message ?: "Something went wrong")
        }
    }

    private companion object {
        const val GUEST_MESSAGE = "feature requires a registered account"
        const val LOCATION_NOT_SAVED = "save your location first"
    }
}