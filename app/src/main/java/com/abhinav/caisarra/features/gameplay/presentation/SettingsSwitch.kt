package com.abhinav.caisarra.features.gameplay.presentation

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.ThemeSlateAccent

@Composable
fun SettingsSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = StatsCyan,
            uncheckedThumbColor = ThemeSlateAccent,
            uncheckedTrackColor = StatsCardBorder
        )
    )
}
