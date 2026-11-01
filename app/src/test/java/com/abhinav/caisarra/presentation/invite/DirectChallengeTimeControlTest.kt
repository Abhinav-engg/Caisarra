package com.abhinav.caisarra.presentation.invite

import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
import com.abhinav.caisarra.presentation.invite.components.DEFAULT_TIME_PRESETS
import com.abhinav.caisarra.presentation.invite.components.formatTimeControlLabel
import com.abhinav.caisarra.presentation.invite.components.resolveGameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectChallengeTimeControlTest {

    @Test
    fun `resolveGameMode correctly categorizes bullet, blitz, rapid, daily`() {
        assertEquals(GameMode.BULLET, resolveGameMode(1f))
        assertEquals(GameMode.BULLET, resolveGameMode(2f))
        assertEquals(GameMode.BLITZ, resolveGameMode(3f))
        assertEquals(GameMode.BLITZ, resolveGameMode(5f))
        assertEquals(GameMode.RAPID, resolveGameMode(10f))
        assertEquals(GameMode.RAPID, resolveGameMode(15f))
        assertEquals(GameMode.RAPID, resolveGameMode(60f))
        assertEquals(GameMode.DAILY, resolveGameMode(1440f))
        assertEquals(GameMode.DAILY, resolveGameMode(2880f))
    }

    @Test
    fun `formatTimeControlLabel formats standard and daily durations with and without increment`() {
        assertEquals("10 min", formatTimeControlLabel(10f, 0f))
        assertEquals("3 min + 2", formatTimeControlLabel(3f, 2f))
        assertEquals("15 min + 10", formatTimeControlLabel(15f, 10f))
        assertEquals("1 day", formatTimeControlLabel(1440f, 0f))
        assertEquals("2 days", formatTimeControlLabel(2880f, 0f))
    }

    @Test
    fun `quick presets match default expectations`() {
        val quickPresets = DEFAULT_TIME_PRESETS.take(4)
        assertEquals(4, quickPresets.size)
        assertTrue(quickPresets.any { it.minutes == 10 && it.increment == 0 })
        assertTrue(quickPresets.any { it.minutes == 5 && it.increment == 0 })
        assertTrue(quickPresets.any { it.minutes == 3 && it.increment == 0 })
        assertTrue(quickPresets.any { it.minutes == 3 && it.increment == 2 })
        assertFalse(quickPresets.any { it.minutes == 15 && it.increment == 10 })
    }
}
