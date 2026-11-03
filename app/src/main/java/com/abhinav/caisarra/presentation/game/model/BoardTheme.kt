package com.abhinav.caisarra.presentation.game.model

import androidx.compose.ui.graphics.Color
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.ThemeNeonDeep
import com.caisaara.ui.theme.ThemeNeonSky

enum class BoardThemeId(val storageKey: String) {
    MIDNIGHT_CYAN("midnight_cyan"),
    EMERALD_CLASSIC("emerald_classic"),
    CLASSIC_WOOD("classic_wood"),
    OCEAN_SLATE("ocean_slate"),
    CHARCOAL_DARK("charcoal_dark"),
    SYNTHWAVE_NEON("synthwave_neon"),
    DESERT_SAND("desert_sand");

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
        lightSquare = Color(0xFFC3EDE1),
        darkSquare = Color(0xFF1D5757),
        selectedColor = Color(0x99FF9933),
        lastMoveColor = Color(0x33FCBF49),
        legalMoveDot = Color(0xFFA57F41),
        legalMoveRing = Color(0xFFA57F41),
        legalMoveOverlay = Color(0x33FCBF49),
        showLegalMoveDotBorder = false,
        palette = listOf(
            Color(0xFF1D5757),
            Color(0xFFC3EDE1),
            Color(0xFFFF9933),
            Color(0xFFFCBF49)
        )
    )

    val ClassicWood = BoardTheme(
        id = BoardThemeId.CLASSIC_WOOD,
        name = "Classic Wood",
        description = "Rich walnut timber with warm natural maple squares",
        lightSquare = Color(0xFFF0D9B5),
        darkSquare = Color(0xFFB58863),
        selectedColor = Color(0x99F6D060),
        lastMoveColor = Color(0x4DD4A373),
        legalMoveDot = Color(0xFF9E7047),
        legalMoveRing = Color(0xFF9E7047),
        legalMoveOverlay = Color(0x33D4A373),
        showLegalMoveDotBorder = true,
        palette = listOf(
            Color(0xFFB58863),
            Color(0xFFF0D9B5),
            Color(0xFF9E7047),
            Color(0xFFF6D060)
        )
    )

    val OceanSlate = BoardTheme(
        id = BoardThemeId.OCEAN_SLATE,
        name = "Ocean Slate",
        description = "Crisp tournament slate blue with seafoam ice squares",
        lightSquare = Color(0xFFDEE3E6),
        darkSquare = Color(0xFF6B8799),
        selectedColor = Color(0x9938BDF8),
        lastMoveColor = Color(0x4D5B99B8),
        legalMoveDot = Color(0xFF4A748C),
        legalMoveRing = Color(0xFF38BDF8),
        legalMoveOverlay = Color(0x3338BDF8),
        showLegalMoveDotBorder = true,
        palette = listOf(
            Color(0xFF6B8799),
            Color(0xFFDEE3E6),
            Color(0xFF38BDF8),
            Color(0xFF4A748C)
        )
    )

    val CharcoalDark = BoardTheme(
        id = BoardThemeId.CHARCOAL_DARK,
        name = "Charcoal Studio",
        description = "Minimalist slate charcoal with electric cobalt highlights",
        lightSquare = Color(0xFFD1D5DB),
        darkSquare = Color(0xFF4B5563),
        selectedColor = Color(0x9960A5FA),
        lastMoveColor = Color(0x4D9CA3AF),
        legalMoveDot = Color(0xFF374151),
        legalMoveRing = Color(0xFF60A5FA),
        legalMoveOverlay = Color(0x3360A5FA),
        showLegalMoveDotBorder = true,
        palette = listOf(
            Color(0xFF4B5563),
            Color(0xFFD1D5DB),
            Color(0xFF60A5FA),
            Color(0xFF374151)
        )
    )

    val SynthwaveNeon = BoardTheme(
        id = BoardThemeId.SYNTHWAVE_NEON,
        name = "Synthwave Neon",
        description = "Deep cyberpunk violet with radiant hot magenta accents",
        lightSquare = Color(0xFFF5E6FF),
        darkSquare = Color(0xFF581C87),
        selectedColor = Color(0x99F43F5E),
        lastMoveColor = Color(0x55A855F7),
        legalMoveDot = Color(0xFFEC4899),
        legalMoveRing = Color(0xFFEC4899),
        legalMoveOverlay = Color(0x33EC4899),
        showLegalMoveDotBorder = true,
        palette = listOf(
            Color(0xFF581C87),
            Color(0xFFF5E6FF),
            Color(0xFFEC4899),
            Color(0xFFA855F7)
        )
    )

    val DesertSand = BoardTheme(
        id = BoardThemeId.DESERT_SAND,
        name = "Desert Sand",
        description = "Warm desert dunes with terracotta and amber accents",
        lightSquare = Color(0xFFF3E7D3),
        darkSquare = Color(0xFFB88B58),
        selectedColor = Color(0x99E07A5F),
        lastMoveColor = Color(0x4DE2A662),
        legalMoveDot = Color(0xFF946231),
        legalMoveRing = Color(0xFFE07A5F),
        legalMoveOverlay = Color(0x33E07A5F),
        showLegalMoveDotBorder = true,
        palette = listOf(
            Color(0xFFB88B58),
            Color(0xFFF3E7D3),
            Color(0xFFE07A5F),
            Color(0xFF946231)
        )
    )

    val allThemes: List<BoardTheme> = listOf(
        MidnightCyan,
        EmeraldClassic,
        ClassicWood,
        OceanSlate,
        CharcoalDark,
        SynthwaveNeon,
        DesertSand
    )

    fun getById(id: BoardThemeId): BoardTheme = when (id) {
        BoardThemeId.MIDNIGHT_CYAN -> MidnightCyan
        BoardThemeId.EMERALD_CLASSIC -> EmeraldClassic
        BoardThemeId.CLASSIC_WOOD -> ClassicWood
        BoardThemeId.OCEAN_SLATE -> OceanSlate
        BoardThemeId.CHARCOAL_DARK -> CharcoalDark
        BoardThemeId.SYNTHWAVE_NEON -> SynthwaveNeon
        BoardThemeId.DESERT_SAND -> DesertSand
    }

    fun getByStorageKey(key: String?): BoardTheme {
        return getById(BoardThemeId.fromStorageKey(key))
    }
}
