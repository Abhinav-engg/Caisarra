package com.abhinav.caisarra.presentation.invite

import org.junit.Assert.assertEquals
import org.junit.Test

class InviteCodeParserTest {

    @Test
    fun `extractCode handles raw code`() {
        assertEquals("7F89AB12", InviteCodeParser.extractCode("7F89AB12"))
        assertEquals("7F89AB12", InviteCodeParser.extractCode("  7F89AB12  "))
    }

    @Test
    fun `extractCode handles play web link`() {
        assertEquals("7F89AB12", InviteCodeParser.extractCode("https://caisaara.duckdns.org/play/7F89AB12"))
        assertEquals("7F89AB12", InviteCodeParser.extractCode("https://caisaara.duckdns.org/play/7F89AB12/"))
        assertEquals("7F89AB12", InviteCodeParser.extractCode("https://caisaara.duckdns.org/play/7F89AB12?source=qr"))
    }

    @Test
    fun `extractCode handles custom scheme`() {
        assertEquals("7F89AB12", InviteCodeParser.extractCode("caisarra://play/7F89AB12"))
        assertEquals("7F89AB12", InviteCodeParser.extractCode("caisarra://join/7F89AB12"))
    }

    @Test
    fun `extractCode handles query param code`() {
        assertEquals("7F89AB12", InviteCodeParser.extractCode("https://caisaara.duckdns.org/?code=7F89AB12&mode=rapid"))
    }

    @Test
    fun `extractCode handles empty and blank`() {
        assertEquals("", InviteCodeParser.extractCode(""))
        assertEquals("", InviteCodeParser.extractCode("   "))
    }
}
