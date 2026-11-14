package com.abhinav.caisarra.features.gameplay.presentation

import androidx.compose.ui.graphics.Color
import com.abhinav.caisarra.features.gameplay.domain.PingQuality
import com.caisaara.ui.theme.AmberNormal
import com.caisaara.ui.theme.EmeraldNormal
import com.caisaara.ui.theme.RedNormal

fun PingQuality.color(): Color = when (this) {
    PingQuality.Excellent -> EmeraldNormal
    PingQuality.Fair -> AmberNormal
    PingQuality.Poor, PingQuality.Offline -> RedNormal
}
