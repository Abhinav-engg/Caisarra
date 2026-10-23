package com.abhinav.caisarra

import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.dto.ApiResponse
import com.abhinav.caisarra.data.remote.dto.ModeRating
import com.abhinav.caisarra.data.remote.dto.ModeStatsData
import com.abhinav.caisarra.data.remote.dto.PlayerRatingProfile
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
import com.abhinav.caisarra.presentation.viewmodel.StatsRange
import com.abhinav.caisarra.presentation.viewmodel.StatsViewModel
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsModelAndEdgeCasesTest {

    private val json = RetrofitInstance.json

    @Test
    fun testModeStatsDataDeserializationWithFullPayload() {
        val jsonPayload = """
        {
          "success": true,
          "message": "mode stats fetched successfully",
          "data": {
            "mode": "blitz",
            "range": "30d",
            "rating": 1242,
            "provisional": false,
            "best_rating": 1260,
            "best_rating_at": "2026-10-12T18:22:05Z",
            "lowest_rating": 1105,
            "lowest_rating_at": "2026-08-03T09:41:52Z",
            "start_rating": 1200,
            "games_played": 33,
            "wins": 18,
            "losses": 12,
            "draws": 3,
            "win_percent": 54,
            "as_white": { "wins": 10, "losses": 5, "draws": 2 },
            "as_black": { "wins": 8, "losses": 7, "draws": 1 },
            "current_streak": 3,
            "best_streak": 7,
            "rating_history": [
              { "date": "2026-10-01", "rating": 1190 },
              { "date": "2026-10-04", "rating": 1215 },
              { "date": "2026-10-20", "rating": 1242 }
            ],
            "recent_games": [
              {
                "game_id": "8c1f2a4e-1234",
                "opponent_username": "rahul",
                "color": "white",
                "outcome": "win",
                "end_reason": "checkmate",
                "ended_at": "2026-10-20T14:03:11Z"
              }
            ]
          }
        }
        """.trimIndent()

        val response = json.decodeFromString<ApiResponse<ModeStatsData>>(jsonPayload)
        assertTrue(response.success)
        val data = response.data
        assertNotNull(data)

        assertEquals("blitz", data!!.mode)
        assertEquals("30d", data.range)
        assertEquals(1242, data.rating)
        assertFalse(data.provisional)
        assertEquals(1260, data.bestRating)
        assertEquals("2026-10-12T18:22:05Z", data.bestRatingAt)
        assertEquals(1105, data.lowestRating)
        assertEquals("2026-08-03T09:41:52Z", data.lowestRatingAt)
        assertEquals(1200, data.startRating)
        assertEquals(33, data.gamesPlayed)
        assertEquals(18, data.wins)
        assertEquals(12, data.losses)
        assertEquals(3, data.draws)
        assertEquals(54, data.winPercent)

        // Streak check
        assertEquals(3, data.currentStreak)
        assertEquals(7, data.bestStreak)

        // Color stats check
        assertNotNull(data.asWhite)
        assertEquals(10, data.asWhite!!.wins)
        assertEquals(5, data.asWhite!!.losses)
        assertEquals(2, data.asWhite!!.draws)

        assertNotNull(data.asBlack)
        assertEquals(8, data.asBlack!!.wins)
        assertEquals(7, data.asBlack!!.losses)
        assertEquals(1, data.asBlack!!.draws)

        // History check
        assertEquals(3, data.ratingHistory.size)
        assertEquals("2026-10-01", data.ratingHistory[0].date)
        assertEquals(1190, data.ratingHistory[0].rating)

        // Recent games check
        assertEquals(1, data.recentGames.size)
        assertEquals("rahul", data.recentGames[0].opponentUsername)
        assertEquals("checkmate", data.recentGames[0].endReason)
    }

    @Test
    fun testModeStatsDataDeserializationForPlayerWithZeroGames() {
        val zeroGamesJson = """
        {
          "success": true,
          "message": "mode stats fetched successfully",
          "data": {
            "mode": "rapid",
            "range": "30d",
            "rating": 1200,
            "provisional": true,
            "best_rating": null,
            "best_rating_at": null,
            "lowest_rating": null,
            "lowest_rating_at": null,
            "start_rating": 1200,
            "games_played": 0,
            "wins": 0,
            "losses": 0,
            "draws": 0,
            "win_percent": 0,
            "as_white": { "wins": 0, "losses": 0, "draws": 0 },
            "as_black": { "wins": 0, "losses": 0, "draws": 0 },
            "current_streak": 0,
            "best_streak": 0,
            "rating_history": [],
            "recent_games": []
          }
        }
        """.trimIndent()

        val response = json.decodeFromString<ApiResponse<ModeStatsData>>(zeroGamesJson)
        assertTrue(response.success)
        val data = response.data
        assertNotNull(data)

        assertEquals("rapid", data!!.mode)
        assertEquals(1200, data.rating)
        assertTrue(data.provisional)
        assertNull(data.bestRating)
        assertNull(data.lowestRating)
        assertEquals(1200, data.startRating)
        assertEquals(0, data.gamesPlayed)
        assertEquals(0, data.wins)
        assertEquals(0, data.losses)
        assertEquals(0, data.draws)
        assertEquals(0, data.winPercent)
        assertTrue(data.ratingHistory.isEmpty())
        assertTrue(data.recentGames.isEmpty())

        // Period delta calculation
        val periodDelta = data.rating - data.startRating
        assertEquals(0, periodDelta)
    }

    @Test
    fun testModeStatsDataWithUnknownFields() {
        val jsonWithUnknown = """
        {
          "success": true,
          "data": {
            "mode": "daily",
            "rating": 1350,
            "provisional": false,
            "future_field_xyz": "value",
            "another_nested": { "foo": 123 }
          }
        }
        """.trimIndent()

        val response = json.decodeFromString<ApiResponse<ModeStatsData>>(jsonWithUnknown)
        assertTrue(response.success)
        assertEquals("daily", response.data?.mode)
        assertEquals(1350, response.data?.rating)
    }

    @Test
    fun testProfileRatingsWithFourModesAndWld() {
        val profileJson = """
        {
          "user_id": 42,
          "username": "caisarrachess",
          "ratings": {
            "bullet": { "rating": 1180, "provisional": false, "wins": 40, "losses": 35, "draws": 4 },
            "blitz": { "rating": 1242, "provisional": false, "wins": 18, "losses": 12, "draws": 3 },
            "rapid": { "rating": 1200, "provisional": true, "wins": 0, "losses": 0, "draws": 0 },
            "daily": { "rating": 1200, "provisional": true, "wins": 0, "losses": 0, "draws": 0 }
          }
        }
        """.trimIndent()

        val profile = json.decodeFromString<PlayerRatingProfile>(profileJson)
        assertEquals("caisarrachess", profile.username)
        val ratings = profile.ratings
        assertNotNull(ratings)
        assertEquals(4, ratings!!.size)

        val bullet = ratings["bullet"]!!
        assertEquals(1180, bullet.rating)
        assertFalse(bullet.provisional)
        assertEquals(40, bullet.wins)
        assertEquals(35, bullet.losses)
        assertEquals(4, bullet.draws)

        val rapid = ratings["rapid"]!!
        assertEquals(1200, rapid.rating)
        assertTrue(rapid.provisional)
        assertEquals(0, rapid.wins)
    }

    @Test
    fun testStatsRangeKeysAndLabels() {
        assertEquals("7d", StatsRange.SEVEN_DAYS.key)
        assertEquals("30d", StatsRange.THIRTY_DAYS.key)
        assertEquals("90d", StatsRange.NINETY_DAYS.key)
        assertEquals("1y", StatsRange.ONE_YEAR.key)
        assertEquals("all", StatsRange.ALL.key)

        assertEquals(StatsRange.SEVEN_DAYS, StatsRange.fromKey("7d"))
        assertEquals(StatsRange.THIRTY_DAYS, StatsRange.fromKey("30D"))
        assertEquals(StatsRange.ALL, StatsRange.fromKey("all"))
        // fallback to default 30d
        assertEquals(StatsRange.THIRTY_DAYS, StatsRange.fromKey(null))
        assertEquals(StatsRange.THIRTY_DAYS, StatsRange.fromKey("unknown"))
    }

    @Test
    fun testGameModeParsing() {
        assertEquals(GameMode.BLITZ, StatsViewModel.parseGameMode("blitz"))
        assertEquals(GameMode.BULLET, StatsViewModel.parseGameMode("bullet"))
        assertEquals(GameMode.RAPID, StatsViewModel.parseGameMode("rapid"))
        assertEquals(GameMode.DAILY, StatsViewModel.parseGameMode("daily"))
        // case insensitivity
        assertEquals(GameMode.BLITZ, StatsViewModel.parseGameMode("BLITZ"))
        // fallback
        assertEquals(GameMode.BLITZ, StatsViewModel.parseGameMode(null))
        assertEquals(GameMode.BLITZ, StatsViewModel.parseGameMode("non_existent"))
    }

    @Test
    fun testPeriodRatingDeltaCalculations() {
        fun computeDelta(rating: Int, startRating: Int): String {
            val delta = rating - startRating
            return when {
                delta > 0 -> "+$delta"
                delta < 0 -> "$delta"
                else -> "+0"
            }
        }

        assertEquals("+64", computeDelta(1420, 1356))
        assertEquals("-15", computeDelta(1185, 1200))
        assertEquals("+0", computeDelta(1200, 1200))
    }
}
