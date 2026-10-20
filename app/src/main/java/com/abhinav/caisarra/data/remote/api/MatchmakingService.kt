package com.abhinav.caisarra.data.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

@Serializable
data class MatchmakingJoinRequest(
    @SerialName("time_control_minutes")
    val timeControlMinutes: Int,

    @SerialName("increment_seconds")
    val incrementSeconds: Int,

    val rated: Boolean,

    @SerialName("ignore_tilt")
    val ignoreTilt: Boolean
)

@Serializable
data class MatchmakingResponse(
    val state: String? = null,

    @SerialName("game_id")
    val gameId: String? = null,
    val error: String? = null,

    @SerialName("tilt_warning")
    val tiltWarning: TiltWarning? = null
)

@Serializable
data class TiltWarning(
    val tilted: Boolean = false,
    val reasons: List<String> = emptyList(),
    val suggestions: List<TiltSuggestion> = emptyList()
)

@Serializable
data class TiltSuggestion(
    val type: String,
    val message: String
)

interface MatchmakingService {

    @POST("api/matchmaking/join")
    suspend fun joinQueue(
        @Body request: MatchmakingJoinRequest
    ): MatchmakingResponse

    @POST("api/matchmaking/cancel")
    suspend fun cancelQueue(): MatchmakingResponse

    @GET("api/matchmaking/status")
    suspend fun getStatus(): MatchmakingResponse
}
