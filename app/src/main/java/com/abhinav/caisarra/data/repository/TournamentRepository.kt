package com.abhinav.caisarra.data.repository

import android.content.Context
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.TournamentService
import com.abhinav.caisarra.data.remote.dto.CreateTournamentRequest
import com.abhinav.caisarra.data.remote.dto.EndTournamentResponse
import com.abhinav.caisarra.data.remote.dto.JoinTournamentRequest
import com.abhinav.caisarra.data.remote.dto.JoinTournamentResponse
import com.abhinav.caisarra.data.remote.dto.LeaveTournamentResponse
import com.abhinav.caisarra.data.remote.dto.RoundsResponse
import com.abhinav.caisarra.data.remote.dto.StandingsResponse
import com.abhinav.caisarra.data.remote.dto.StartTournamentResponse
import com.abhinav.caisarra.data.remote.dto.TournamentDto
import com.abhinav.caisarra.data.remote.dto.TournamentGamesResponse
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

class TournamentRepository private constructor(
    private val service: TournamentService
) {

    companion object {
        @Volatile
        private var instance: TournamentRepository? = null

        fun get(context: Context): TournamentRepository =
            instance ?: synchronized(this) {
                instance ?: TournamentRepository(
                    RetrofitInstance.createTournamentService(
                        TokenManager.get(context.applicationContext)
                    )
                ).also { instance = it }
            }
    }

    private suspend fun <T> call(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            Result.failure(Exception(errorMessage(e), e))
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun errorMessage(e: HttpException): String {
        val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val parsed = body?.let {
            runCatching {
                val obj = RetrofitInstance.json.parseToJsonElement(it).jsonObject
                (obj["error"] ?: obj["message"])?.jsonPrimitive?.content
            }.getOrNull()
        }
        return parsed?.takeIf { it.isNotBlank() } ?: e.message()
    }

    suspend fun createTournament(request: CreateTournamentRequest): Result<TournamentDto> =
        call { service.createTournament(request) }

    suspend fun listTournaments(): Result<List<TournamentDto>> =
        call { service.listTournaments() }

    suspend fun getTournament(id: String): Result<TournamentDto> =
        call { service.getTournament(id) }

    suspend fun getTournamentByInvite(code: String): Result<TournamentDto> =
        call { service.getTournamentByInvite(code) }

    suspend fun joinTournament(id: String, inviteCode: String? = null): Result<JoinTournamentResponse> =
        call { service.joinTournament(id, JoinTournamentRequest(inviteCode)) }

    suspend fun joinByInvite(code: String): Result<JoinTournamentResponse> =
        call { service.joinByInvite(code) }

    suspend fun leaveTournament(id: String): Result<LeaveTournamentResponse> =
        call { service.leaveTournament(id) }

    suspend fun startTournament(id: String): Result<StartTournamentResponse> =
        call { service.startTournament(id) }

    suspend fun endTournament(id: String): Result<EndTournamentResponse> =
        call { service.endTournament(id) }

    suspend fun getStandings(id: String): Result<StandingsResponse> =
        call { service.getStandings(id) }

    suspend fun getRounds(id: String): Result<RoundsResponse> =
        call { service.getRounds(id) }

    suspend fun getRound(id: String, round: Int): Result<RoundsResponse> =
        call { service.getRound(id, round) }

    suspend fun getGames(id: String): Result<TournamentGamesResponse> =
        call { service.getGames(id) }

    suspend fun getMyGames(id: String): Result<TournamentGamesResponse> =
        call { service.getMyGames(id) }
}