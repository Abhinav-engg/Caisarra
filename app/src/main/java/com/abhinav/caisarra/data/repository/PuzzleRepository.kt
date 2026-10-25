package com.abhinav.caisarra.data.repository


import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.api.PuzzleApiService
import com.abhinav.caisarra.data.remote.dto.DailyPuzzleAttemptRequest
import com.abhinav.caisarra.data.remote.dto.DailyPuzzleAttemptResponse
import com.abhinav.caisarra.data.remote.dto.DailyPuzzleHintRequest
import com.abhinav.caisarra.data.remote.dto.DailyPuzzleHintResponse
import com.abhinav.caisarra.data.remote.dto.DailyPuzzleResponse
import com.abhinav.caisarra.data.remote.dto.DailyPuzzleSolutionRequest
import com.abhinav.caisarra.data.remote.dto.DailyPuzzleSolutionResponse
import com.abhinav.caisarra.data.remote.dto.PuzzleDetailsResponse
import com.abhinav.caisarra.data.remote.dto.PuzzleHistoryResponse
import com.abhinav.caisarra.data.remote.dto.PuzzleStatsResponse
import com.abhinav.caisarra.data.remote.dto.RushEndRequest
import com.abhinav.caisarra.data.remote.dto.RushEndResponse
import com.abhinav.caisarra.data.remote.dto.RushMoveRequest
import com.abhinav.caisarra.data.remote.dto.RushMoveResponse
import com.abhinav.caisarra.data.remote.dto.RushStartRequest
import com.abhinav.caisarra.data.remote.dto.RushStartResponse
import com.abhinav.caisarra.data.remote.dto.RushStatsResponse
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class PuzzleRepository(
    private val service: PuzzleApiService
) {

    suspend fun getDailyPuzzle(): Result<DailyPuzzleResponse> = call {
        service.getDailyPuzzle()
    }

    suspend fun submitAttempt(
        puzzleId: String,
        move: String,
        timeTakenMs: Long
    ): Result<DailyPuzzleAttemptResponse> = call {
        service.submitAttempt(
            DailyPuzzleAttemptRequest(
                puzzleId = puzzleId,
                move = move,
                timeTakenMs = timeTakenMs
            )
        )
    }

    suspend fun getHint(puzzleId: String): Result<DailyPuzzleHintResponse> = call {
        service.getHint(DailyPuzzleHintRequest(puzzleId = puzzleId))
    }

    suspend fun getSolution(puzzleId: String): Result<DailyPuzzleSolutionResponse> = call {
        service.getSolution(DailyPuzzleSolutionRequest(puzzleId = puzzleId))
    }

    suspend fun getPuzzleById(puzzleId: String): Result<PuzzleDetailsResponse> = call {
        service.getPuzzleById(puzzleId)
    }

    suspend fun getStats(): Result<PuzzleStatsResponse> = call {
        service.getStats()
    }

    suspend fun getHistory(
        limit: Int = 20,
        offset: Int = 0
    ): Result<PuzzleHistoryResponse> = call {
        service.getHistory(limit = limit, offset = offset)
    }

    suspend fun startRush(mode: String): Result<RushStartResponse> = call {
        service.startRush(RushStartRequest(mode = mode))
    }

    suspend fun submitRushMove(
        sessionId: String,
        puzzleId: String,
        move: String
    ): Result<RushMoveResponse> = call {
        service.submitRushMove(
            RushMoveRequest(
                sessionId = sessionId,
                puzzleId = puzzleId,
                move = move
            )
        )
    }

    suspend fun skipRushPuzzle(
        sessionId: String,
        puzzleId: String
    ): Result<RushMoveResponse> = call {
        service.skipRushPuzzle(
            RushMoveRequest(
                sessionId = sessionId,
                puzzleId = puzzleId,
                move = "skip"
            )
        )
    }

    suspend fun endRush(sessionId: String): Result<RushEndResponse> = call {
        service.endRush(RushEndRequest(sessionId = sessionId))
    }

    suspend fun getRushStats(): Result<RushStatsResponse> = call {
        service.getRushStats()
    }

    private suspend fun <T> call(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            Result.failure(Exception(ApiErrorInterpreter.toUserMessage(e), e))
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorInterpreter.toUserMessage(e), e))
        }
}
