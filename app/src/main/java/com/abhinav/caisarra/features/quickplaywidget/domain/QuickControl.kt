package com.abhinav.caisarra.features.quickplaywidget.domain

import androidx.compose.ui.graphics.Color
import com.caisaara.ui.theme.EmeraldNormal
import com.caisaara.ui.theme.StatsPeakBadgeText
import com.caisaara.ui.theme.ThemeNeonSky

data class QuickControl(val label: String, val minutes: Int, val increment: Int, val accent: Color) {
    val clock get() = if (increment == 0) "$minutes min" else "$minutes | $increment"
}

val quickControls = listOf(
    QuickControl("Bullet", 1, 0, StatsPeakBadgeText),
    QuickControl("Blitz", 3, 2, ThemeNeonSky),
    QuickControl("Rapid", 10, 0, EmeraldNormal)
)
