package com.abhinav.caisarra.data.repository

import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.api.AnalysisService
import kotlinx.coroutines.CancellationException

class GameAnalysisRepository(private val service: AnalysisService) {

    suspend fun requestAnalysis(gameId: String) = call { service.requestAnalysis(gameId) }

    suspend fun summary(gameId: String) = call { service.getSummary(gameId) }

    suspend fun moves(gameId: String) = call { service.getMoves(gameId) }

    suspend fun move(gameId: String, moveNumber: Int) = call { service.getMove(gameId, moveNumber) }

    suspend fun retry(gameId: String) = call { service.retry(gameId) }

    private suspend fun <T> call(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorInterpreter.toUserMessage(e)))
        }
}