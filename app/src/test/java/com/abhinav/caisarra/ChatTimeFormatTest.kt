package com.abhinav.caisarra

import com.abhinav.caisarra.presentation.game.components.formatMessageTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.util.TimeZone

class ChatTimeFormatTest {

    @Test
    fun testUtcTimestampFormatsToLocalTime() {
        val defaultTz = TimeZone.getDefault()
        try {
            // Set timezone to Asia/Kolkata (+05:30)
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"))
            val formatted = formatMessageTime("2026-10-23T04:16:49Z")
            // 04:16 UTC + 5:30 = 09:46 AM
            assertTrue("Expected 9:46 AM, got $formatted", formatted.contains("9:46") && formatted.contains("AM"))
        } finally {
            TimeZone.setDefault(defaultTz)
        }
    }

    @Test
    fun testUtcWithMillisTimestampFormatsToLocalTime() {
        val defaultTz = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"))
            val formatted = formatMessageTime("2026-10-23T04:16:49.123456Z")
            assertTrue("Expected 9:46 AM, got $formatted", formatted.contains("9:46") && formatted.contains("AM"))
        } finally {
            TimeZone.setDefault(defaultTz)
        }
    }

    @Test
    fun testBlankReturnsEmpty() {
        assertTrue(formatMessageTime("").isEmpty())
        assertTrue(formatMessageTime("   ").isEmpty())
    }

    @Test
    fun testFallbackOnCorruptInput() {
        val formatted = formatMessageTime("invalid_time_string")
        assertFalse(formatted.isEmpty())
    }
}
