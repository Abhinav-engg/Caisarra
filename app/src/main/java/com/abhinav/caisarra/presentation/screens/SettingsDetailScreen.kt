package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono

private val BackgroundColor = Color(0xFF080D13)
private val CardBg = Color(0xFF0F1722)
private val CardBorderColor = Color(0xFF1B2838)
private val TextWhiteColor = Color(0xFFF1F5F9)
private val TextMutedColor = Color(0xFF82909E)
private val CyanColor = Color(0xFF23CDE8)

@Composable
fun SettingsDetailScreen(
    onBack: () -> Unit
) {
    var soundEnabled by remember { mutableStateOf(true) }
    var hapticsEnabled by remember { mutableStateOf(true) }
    var coordinatesEnabled by remember { mutableStateOf(true) }
    var autoQueenEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF101924))
                    .border(
                        width = 1.dp,
                        color = Color(0xFF202E3E),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhiteColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Preferences",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextWhiteColor
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "GAMEPLAY & AUDIO",
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMutedColor
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column {
                    PreferenceToggleRow(
                        title = "Sound Effects",
                        subtitle = "Play audio on piece moves, captures, and checks",
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                    HorizontalDivider(color = Color(0xFF162230), thickness = 1.dp)
                    PreferenceToggleRow(
                        title = "Haptic Feedback",
                        subtitle = "Vibrate device on piece drop",
                        checked = hapticsEnabled,
                        onCheckedChange = { hapticsEnabled = it }
                    )
                    HorizontalDivider(color = Color(0xFF162230), thickness = 1.dp)
                    PreferenceToggleRow(
                        title = "Board Coordinates",
                        subtitle = "Show rank and file letters on board edges",
                        checked = coordinatesEnabled,
                        onCheckedChange = { coordinatesEnabled = it }
                    )
                    HorizontalDivider(color = Color(0xFF162230), thickness = 1.dp)
                    PreferenceToggleRow(
                        title = "Auto-Queen Promotion",
                        subtitle = "Always promote pawn to Queen automatically",
                        checked = autoQueenEnabled,
                        onCheckedChange = { autoQueenEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "ABOUT",
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMutedColor
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "App Version",
                        fontFamily = JetBrainsMono,
                        fontSize = 14.sp,
                        color = TextWhiteColor
                    )
                    Text(
                        text = "1.0.0 (Build 1)",
                        fontFamily = JetBrainsMono,
                        fontSize = 13.sp,
                        color = CyanColor
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferenceToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextWhiteColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                color = TextMutedColor,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CyanColor,
                uncheckedThumbColor = Color(0xFF94A4B8),
                uncheckedTrackColor = Color(0xFF1B2838)
            )
        )
    }
}
