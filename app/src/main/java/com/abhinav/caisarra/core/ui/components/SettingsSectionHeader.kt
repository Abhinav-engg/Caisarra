package com.abhinav.caisarra.core.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.TextSizeCaption

@Composable
fun SettingsSectionHeader(text: String, color: Color = StatsTextMuted) {
    Text(
        text = text,
        fontFamily = JetBrainsMono,
        fontSize = TextSizeCaption,
        fontWeight = FontWeight.SemiBold,
        color = color
    )
}
