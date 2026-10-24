package com.abhinav.caisarra

import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
import com.abhinav.caisarra.presentation.viewmodel.ActiveDailyGame
import com.abhinav.caisarra.presentation.viewmodel.ActiveGame
import com.abhinav.caisarra.presentation.viewmodel.HomeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveGameModesTest {

    @Test
    fun testActiveGameTypeAliasAndModes() {
        val dailyGame: ActiveDailyGame = ActiveGame(
            id = "game_1",
            opponentUsername = "GrandmasterX",
            isMyTurn = true,
            timeControlMinutes = 1440,
            mode = GameMode.DAILY,
            daysPerMove = 1
        )

        assertEquals("game_1", dailyGame.id)
        assertEquals(GameMode.DAILY, dailyGame.mode)
        assertEquals(1, dailyGame.daysPerMove)
        assertTrue(dailyGame.isMyTurn)

        val blitzGame = ActiveGame(
            id = "game_2",
            opponentUsername = "SpeedyKnight",
            isMyTurn = false,
            timeControlMinutes = 3,
            mode = GameMode.BLITZ,
            expiresAt = System.currentTimeMillis() + 120_000L
        )

        assertEquals(GameMode.BLITZ, blitzGame.mode)
        assertFalse(blitzGame.isMyTurn)
    }

    @Test
    fun testFilteringActiveGamesBySelectedMode() {
        val rapidGame = ActiveGame(
            id = "rapid_1",
            opponentUsername = "UserA",
            isMyTurn = true,
            timeControlMinutes = 10,
            mode = GameMode.RAPID
        )
        val blitzGame = ActiveGame(
            id = "blitz_1",
            opponentUsername = "UserB",
            isMyTurn = false,
            timeControlMinutes = 3,
            mode = GameMode.BLITZ
        )
        val bulletGame = ActiveGame(
            id = "bullet_1",
            opponentUsername = "UserC",
            isMyTurn = true,
            timeControlMinutes = 1,
            mode = GameMode.BULLET
        )
        val dailyGame = ActiveGame(
            id = "daily_1",
            opponentUsername = "UserD",
            isMyTurn = false,
            timeControlMinutes = 2880,
            mode = GameMode.DAILY,
            daysPerMove = 2
        )

        val allActive = listOf(rapidGame, blitzGame, bulletGame, dailyGame)

        // Selected mode = RAPID
        val rapidState = HomeUiState(selectedMode = GameMode.RAPID, activeGames = allActive)
        val rapidActive = rapidState.activeGames.filter { it.mode == rapidState.selectedMode }
        assertEquals(1, rapidActive.size)
        assertEquals("rapid_1", rapidActive.first().id)

        // Selected mode = BLITZ
        val blitzState = HomeUiState(selectedMode = GameMode.BLITZ, activeGames = allActive)
        val blitzActive = blitzState.activeGames.filter { it.mode == blitzState.selectedMode }
        assertEquals(1, blitzActive.size)
        assertEquals("blitz_1", blitzActive.first().id)

        // Selected mode = BULLET
        val bulletState = HomeUiState(selectedMode = GameMode.BULLET, activeGames = allActive)
        val bulletActive = bulletState.activeGames.filter { it.mode == bulletState.selectedMode }
        assertEquals(1, bulletActive.size)
        assertEquals("bullet_1", bulletActive.first().id)

        // Selected mode = DAILY
        val dailyState = HomeUiState(selectedMode = GameMode.DAILY, activeGames = allActive)
        val dailyActive = dailyState.activeGames.filter { it.mode == dailyState.selectedMode }
        assertEquals(1, dailyActive.size)
        assertEquals("daily_1", dailyActive.first().id)
    }

    @Test
    fun testExpiredGamesDisappear() {
        val now = System.currentTimeMillis()
        val expiredGame = ActiveGame(
            id = "exp_1",
            opponentUsername = "PastPlayer",
            isMyTurn = false,
            timeControlMinutes = 3,
            mode = GameMode.BLITZ,
            expiresAt = now - 5000L // 5 seconds ago
        )
        val activeGame = ActiveGame(
            id = "act_1",
            opponentUsername = "CurrentPlayer",
            isMyTurn = true,
            timeControlMinutes = 3,
            mode = GameMode.BLITZ,
            expiresAt = now + 60_000L // 1 minute in future
        )

        val games = listOf(expiredGame, activeGame)
        val nonExpired = games.filter { it.expiresAt == null || it.expiresAt > now }

        assertEquals(1, nonExpired.size)
        assertEquals("act_1", nonExpired.first().id)
    }

    @Test
    fun testHomeUiStateStreakDefaultsAndUpdates() {
        val initialState = HomeUiState()
        assertEquals(0, initialState.streak)

        val updatedState = initialState.copy(streak = 14)
        assertEquals(14, updatedState.streak)
    }
}
