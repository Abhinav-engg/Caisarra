package com.abhinav.caisarra.features.puzzles.presentation

import androidx.compose.ui.graphics.Color
import com.caisaara.ui.theme.GoldColor
import com.caisaara.ui.theme.MenuFriendsGreen
import com.caisaara.ui.theme.RedNormal
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTextMuted

private val MetaThemes = setOf("short", "long", "veryLong", "oneMove", "crushing", "advantage", "equality", "master", "masterVsMaster", "superGM")

fun puzzleThemeAccent(theme: String): Color = when {
    theme.contains("mate", ignoreCase = true) -> RedNormal
    theme.contains("endgame", ignoreCase = true) -> GoldColor
    theme == "opening" || theme == "middlegame" -> MenuFriendsGreen
    theme in MetaThemes -> StatsTextMuted
    else -> StatsCyan
}
