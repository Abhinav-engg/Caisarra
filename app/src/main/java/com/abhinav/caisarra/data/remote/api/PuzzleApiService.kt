package com.abhinav.caisarra.data.remote.api

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

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PuzzleApiService {

    // ---------------------------------------------------------
    // DAILY PUZZLE
    // ---------------------------------------------------------

    @GET("puzzles/daily")
    suspend fun getDailyPuzzle(): DailyPuzzleResponse

    @POST("puzzles/daily/attempt")
    suspend fun submitAttempt(
        @Body request: DailyPuzzleAttemptRequest
    ): DailyPuzzleAttemptResponse

    @POST("puzzles/daily/hint")
    suspend fun getHint(
        @Body request: DailyPuzzleHintRequest
    ): DailyPuzzleHintResponse

    @POST("puzzles/daily/solution")
    suspend fun getSolution(
        @Body request: DailyPuzzleSolutionRequest
    ): DailyPuzzleSolutionResponse

    // ---------------------------------------------------------
    // PUZZLE DETAILS
    // ---------------------------------------------------------

    @GET("puzzles/{puzzle_id}")
    suspend fun getPuzzleById(
        @Path("puzzle_id") puzzleId: String
    ): PuzzleDetailsResponse

    @GET("puzzles/stats")
    suspend fun getStats(): PuzzleStatsResponse

    @GET("puzzles/history")
    suspend fun getHistory(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): PuzzleHistoryResponse

    // ---------------------------------------------------------
    // PUZZLE RUSH
    // ---------------------------------------------------------

    @POST("puzzles/rush/start")
    suspend fun startRush(
        @Body request: RushStartRequest
    ): RushStartResponse

    @POST("puzzles/rush/move")
    suspend fun submitRushMove(
        @Body request: RushMoveRequest
    ): RushMoveResponse

    @POST("puzzles/rush/skip")
    suspend fun skipRushPuzzle(
        @Body request: RushMoveRequest
    ): RushMoveResponse

    @POST("puzzles/rush/end")
    suspend fun endRush(
        @Body request: RushEndRequest
    ): RushEndResponse

    @GET("puzzles/rush/stats")
    suspend fun getRushStats(): RushStatsResponse

    // ---------------------------------------------------------
    // CUSTOM TACTICS
    // ---------------------------------------------------------

    /**
     * Returns the available tactical themes.
     *
     * GET /puzzles/themes
     */
    @GET("puzzles/themes")
    suspend fun getPuzzleThemes(): PuzzleThemesResponse

    /**
     * Fetches a custom puzzle using the selected filters.
     *
     * GET /puzzles/custom
     *
     * The backend expects `themes` as a comma-separated string,
     * for example: themes=fork,pin
     */
    @GET("puzzles/custom")
    suspend fun getCustomPuzzle(
        @Query("theme")
        theme: String? = null,

        @Query("themes")
        themes: String? = null,

        @Query("min_rating")
        minRating: Int? = null,

        @Query("max_rating")
        maxRating: Int? = null,

        @Query("exclude_solved")
        excludeSolved: Boolean = true,

        @Query("review_mistakes")
        reviewMistakes: Boolean = false
    ): CustomPuzzleResponse

    /**
     * Submits a move for a custom tactics puzzle.
     *
     * POST /puzzles/custom/attempt
     */
    @POST("puzzles/custom/attempt")
    suspend fun submitCustomPuzzleAttempt(
        @Body request: CustomPuzzleAttemptRequest
    ): CustomPuzzleAttemptResponse
}