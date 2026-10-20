package com.abhinav.caisarra.presentation.game.components.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomClockCard(
    minutes: Float,
    bonus: Float,
    onMinutesChange: (Float) -> Unit,
    onBonusChange: (Float) -> Unit,
    showBonus: Boolean = true
) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(SetupColors.Card).padding(20.dp)) {
        Text("Custom Clock Setup", color = SetupColors.Text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Initial Time", color = SetupColors.Secondary, fontSize = 14.sp)
            Text("${minutes.toInt()} min", color = SetupColors.Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Slider(minutes, onMinutesChange, valueRange = 1f..60f, colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = SetupColors.Accent, inactiveTrackColor = Color(0xFF253241)))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("1m", "15m", "30m", "60m").forEach { Text(it, color = Color(0xFF71839B), fontSize = 12.sp) }
        }
        if (showBonus) {
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Bonus Time (Increment)", color = SetupColors.Secondary, fontSize = 14.sp)
                Text("${bonus.toInt()} sec", color = SetupColors.Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Slider(bonus, onBonusChange, valueRange = 0f..60f, colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = SetupColors.Accent, inactiveTrackColor = Color(0xFF253241)))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("0", "15s", "30s", "60s").forEach { Text(it, color = Color(0xFF71839B), fontSize = 12.sp) }
            }
        }
    }
}
