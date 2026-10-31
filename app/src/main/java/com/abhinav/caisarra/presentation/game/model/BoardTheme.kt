package com.abhinav.caisarra.presentation.game.model

import androidx.compose.ui.graphics.Color
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.ThemeNeonDeep
import com.caisaara.ui.theme.ThemeNeonSky

enum class BoardThemeId(val storageKey: String) {
    MIDNIGHT_CYAN("midnight_cyan"),
    EMERALD_CLASSIC("emerald_classic");

    companion object {
        fun fromStorageKey(key: String?): BoardThemeId {
            return entries.firstOrNull { it.storageKey == key } ?: MIDNIGHT_CYAN
        }
    }
}

data class BoardTheme(
    val id: BoardThemeId,
    val name: String,
    val description: String,
    val lightSquare: Color,
    val darkSquare: Color,
    val selectedColor: Color,
    val lastMoveColor: Color,
    val legalMoveDot: Color,
    val legalMoveRing: Color,
    val legalMoveOverlay: Color,
    val checkColor: Color = Color(0xFFF04452),
    val showLegalMoveDotBorder: Boolean = true,
    val palette: List<Color>
)

object BoardThemes {
    val MidnightCyan = BoardTheme(
        id = BoardThemeId.MIDNIGHT_CYAN,
        name = "Midnight Cyan",
        description = "Dark cyberpunk aesthetic with luminous cyan accents",
        lightSquare = Color(0xFFE8EDF3),
        darkSquare = Color(0xFF253449),
        selectedColor = Color(0xFF10B981),
        lastMoveColor = Color(0xFFB8D879),
        legalMoveDot = Color(0xFF00D6A0),
        legalMoveRing = Color(0xFF00E5A0),
        legalMoveOverlay = Color(0x3300D6A0),
        showLegalMoveDotBorder = true,
        palette = listOf(
            StatsBg,
            ThemeNeonDeep,
            StatsCyan,
            ThemeNeonSky
        )
    )

    val EmeraldClassic = BoardTheme(
        id = BoardThemeId.EMERALD_CLASSIC,
        name = "Emerald Classic",
        description = "Classic tournament green with championship gold accents",
        lightSquare = Color(0xFFC3EDE1),       // #AFF0D7 with 60% opacity (#AFF0D799)
        darkSquare = Color(0xFF1D5757),        // #1D5757
        selectedColor = Color(0x99FF9933),     // #FF9933 at 60% (#FF993399)
        lastMoveColor = Color(0x33FCBF49),     // #FCBF49 at 20% (#FCBF4933)
        legalMoveDot = Color(0xFFA57F41),      // Legal move dot (#FCBF49 / #FF9933 on board)
        legalMoveRing = Color(0xFFA57F41),     // Capture ring
        legalMoveOverlay = Color(0x33FCBF49),  // Capture overlay (#FCBF49 at 20% #FCBF4933)
        showLegalMoveDotBorder = false,
        palette = listOf(
            Color(0xFF1D5757),
            Color(0xFFC3EDE1),
            Color(0xFFFF9933),
            Color(0xFFFCBF49)
        )
    )

    val allThemes: List<BoardTheme> = listOf(MidnightCyan, EmeraldClassic)

    fun getById(id: BoardThemeId): BoardTheme = when (id) {
        BoardThemeId.MIDNIGHT_CYAN -> MidnightCyan
        BoardThemeId.EMERALD_CLASSIC -> EmeraldClassic
    }

    fun getByStorageKey(key: String?): BoardTheme {
        return getById(BoardThemeId.fromStorageKey(key))
    }
}
