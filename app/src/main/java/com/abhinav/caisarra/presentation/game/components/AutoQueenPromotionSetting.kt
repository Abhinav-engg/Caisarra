
package com.abhinav.caisarra.presentation.game.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val PREFS_NAME = "caisarra_game_preferences"
private const val AUTO_QUEEN_KEY = "auto_promote_to_queen"
private val SettingCard = Color(0xFF111A24)
private val SettingBorder = Color(0xFF263440)
private val SettingAccent = Color(0xFF10B981)
private val SettingText = Color(0xFFF1F5F9)
private val SettingMuted = Color(0xFF94A3B8)

fun isAutoQueenEnabled(context: Context): Boolean {
    return context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(AUTO_QUEEN_KEY, false)
}

fun setAutoQueenEnabled(context: Context, enabled: Boolean) {
    context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(AUTO_QUEEN_KEY, enabled)
        .apply()
}

@Composable
fun AutoQueenPromotionSetting(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var enabled by remember {
        mutableStateOf(isAutoQueenEnabled(context))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SettingCard, RoundedCornerShape(16.dp))
            .border(1.dp, SettingBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Auto-promote to Queen",
                color = SettingText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = if (enabled) {
                    "Your pawn automatically becomes a queen."
                } else {
                    "Choose Queen, Rook, Bishop or Knight."
                },
                color = SettingMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }

        Switch(
            checked = enabled,
            onCheckedChange = { checked ->
                enabled = checked
                setAutoQueenEnabled(context, checked)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SettingAccent,
                uncheckedThumbColor = SettingMuted,
                uncheckedTrackColor = SettingCard
            )
        )
    }
}
