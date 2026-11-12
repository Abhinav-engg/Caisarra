package com.abhinav.caisarra.data.repository

import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.api.PuzzleApiService
import com.abhinav.caisarra.data.remote.dto.CustomPuzzleAttemptRequest
import com.abhinav.caisarra.data.remote.dto.CustomPuzzleAttemptResponse
import com.abhinav.caisarra.data.remote.dto.CustomPuzzleResponse
import com.abhinav.caisarra.data.remote.dto.DailyPuzzleAttemptRequest
import com.abhinav.caisarra.data.remote.dto.DailyPuzzleAttemptResponse
import com.abhinav.caisarra.data.remote.dto.DailyPuzzleHintRequest
import com.abhinav.caisarra.data.remote.dto.DailyPuzzleHintResponse
import com.abhinav.caisarra.data.remote.dto.DailyPuzzleResponse
import com.abhinav.caisarra.data.remote.dto.DailyPuzzleSolutionRequest
import com.abhinav.caisarra.data.remote.dto.DailyPuzzleSolutionResponse
import com.abhinav.caisarra.data.remote.dto.PuzzleDetailsResponse
import com.abhinav.caisarra.data.remote.dto.PuzzleHistoryItemDto
import com.abhinav.caisarra.data.remote.dto.PuzzleHistoryResponse
import com.abhinav.caisarra.data.remote.dto.PuzzleStatsResponse
import com.abhinav.caisarra.data.remote.dto.PuzzleThemesResponse
import com.abhinav.caisarra.data.remote.dto.RushEndRequest
import com.abhinav.caisarra.data.remote.dto.RushEndResponse
import com.abhinav.caisarra.data.remote.dto.RushMoveRequest
import com.abhinav.caisarra.data.remote.dto.RushMoveResponse
import com.abhinav.caisarra.data.remote.dto.RushStartRequest
import com.abhinav.caisarra.data.remote.dto.RushStartResponse
import com.abhinav.caisarra.data.remote.dto.RushStatsResponse

import java.time.LocalDate
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

    suspend fun getHint(
        puzzleId: String
    ): Result<DailyPuzzleHintResponse> = call {
        service.getHint(
            DailyPuzzleHintRequest(
                puzzleId = puzzleId
            )
        )
    }

    suspend fun getSolution(
        puzzleId: String
    ): Result<DailyPuzzleSolutionResponse> = call {
        service.getSolution(
            DailyPuzzleSolutionRequest(
                puzzleId = puzzleId
            )
        )
    }

    suspend fun getPuzzleById(
        puzzleId: String
    ): Result<PuzzleDetailsResponse> = call {
        service.getPuzzleById(puzzleId)
    }

    suspend fun getStats(): Result<PuzzleStatsResponse> = call {
        service.getStats()
    }

    suspend fun getHistory(
        limit: Int = 20,
        offset: Int = 0
    ): Result<PuzzleHistoryResponse> = call {
        service.getHistory(
            limit = limit,
            offset = offset
        )
    }

    suspend fun getHistorySince(since: LocalDate?): Result<List<PuzzleHistoryItemDto>> = call {
        val isInRange = { item: PuzzleHistoryItemDto ->
            since == null || item.createdAt.orEmpty().take(10) >= since.toString()
        }
        buildList {
            do {
                val page = service.getHistory(limit = 100, offset = size).allAttempts
                addAll(page.filter(isInRange))
            } while (page.isNotEmpty() && page.all(isInRange) && size < 2000)
        }
    }

    suspend fun startRush(
        mode: String
    ): Result<RushStartResponse> = call {
        service.startRush(
            RushStartRequest(mode = mode)
        )
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

    suspend fun endRush(
        sessionId: String
    ): Result<RushEndResponse> = call {
        service.endRush(
            RushEndRequest(sessionId = sessionId)
        )
    }

    suspend fun getRushStats(): Result<RushStatsResponse> = call {
        service.getRushStats()
    }

    suspend fun getPuzzleThemes(): Result<PuzzleThemesResponse> = call {
        service.getPuzzleThemes()
    }

    suspend fun getCustomPuzzle(
        theme: String? = null,
        themes: List<String> = emptyList(),
        minRating: Int? = null,
        maxRating: Int? = null,
        excludeSolved: Boolean = true,
        reviewMistakes: Boolean = false
    ): Result<CustomPuzzleResponse> = call {

        val selectedThemes = themes
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(",")

        service.getCustomPuzzle(
            theme = theme,
            themes = selectedThemes.takeIf { it.isNotBlank() },
            minRating = minRating,
            maxRating = maxRating,
            excludeSolved = excludeSolved,
            reviewMistakes = reviewMistakes
        )
    }

    suspend fun submitCustomPuzzleAttempt(
        puzzleId: String,
        move: String,
        timeTakenMs: Long,
        moveIndex: Int? = null,
        rated: Boolean = false
    ): Result<CustomPuzzleAttemptResponse> = call {

        service.submitCustomPuzzleAttempt(
            CustomPuzzleAttemptRequest(
                puzzleId = puzzleId,
                move = move,
                timeTakenMs = timeTakenMs,
                moveIndex = moveIndex,
                rated = rated
            )
        )
    }

    private suspend fun <T> call(
        block: suspend () -> T
    ): Result<T> {

        return try {
            Result.success(block())

        } catch (e: CancellationException) {
            throw e

        } catch (e: HttpException) {
            Result.failure(
                Exception(
                    ApiErrorInterpreter.toUserMessage(e),
                    e
                )
            )

        } catch (e: Exception) {
            Result.failure(
                Exception(
                    ApiErrorInterpreter.toUserMessage(e),
                    e
                )
            )
        }
    }
}