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

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class PuzzleRepository(
    private val service: PuzzleApiService
) {

    // ---------------------------------------------------------
    // DAILY PUZZLE
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // PUZZLE DETAILS
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // PUZZLE RUSH
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // CUSTOM TACTICS
    // ---------------------------------------------------------

    /**
     * Loads the available tactical themes.
     */
    suspend fun getPuzzleThemes(): Result<PuzzleThemesResponse> = call {
        service.getPuzzleThemes()
    }

    /**
     * Fetches a custom puzzle.
     *
     * @param theme Single theme ID, if one theme is selected.
     * @param themes Multiple selected theme IDs.
     *              They are joined into a comma-separated string.
     * @param minRating Minimum puzzle rating.
     * @param maxRating Maximum puzzle rating.
     * @param excludeSolved Whether already solved puzzles are skipped.
     * @param reviewMistakes Whether to serve previously failed puzzles.
     */
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

    /**
     * Submits a move for a custom tactics puzzle.
     */
    suspend fun submitCustomPuzzleAttempt(
        puzzleId: String,
        move: String,
        timeTakenMs: Long,
        rated: Boolean = false
    ): Result<CustomPuzzleAttemptResponse> = call {

        service.submitCustomPuzzleAttempt(
            CustomPuzzleAttemptRequest(
                puzzleId = puzzleId,
                move = move,
                timeTakenMs = timeTakenMs,
                rated = rated
            )
        )
    }

    // ---------------------------------------------------------
    // COMMON API RESULT HANDLER
    // ---------------------------------------------------------

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