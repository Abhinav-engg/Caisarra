package com.abhinav.caisarra.presentation.screens

import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.preferences.core.edit
import com.abhinav.caisarra.data.local.HAPTICS_KEY
import com.abhinav.caisarra.data.local.settingsDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
import com.abhinav.caisarra.core.ui.theme.StatsCardCornerRadius
import com.abhinav.caisarra.core.ui.theme.StatsScreenPadding
import com.abhinav.caisarra.core.ui.theme.StatsSectionSpacing
import com.caisaara.ui.theme.SettingsDivider
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTabBg
import com.caisaara.ui.theme.StatsTabBorder
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeHeading
import com.caisaara.ui.theme.TextSizeTiny
import com.caisaara.ui.theme.ThemeSlateAccent

@Composable
fun SettingsDetailScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hapticsEnabled by remember(context) {
        context.settingsDataStore.data.map { it[HAPTICS_KEY] ?: true }
    }.collectAsState(initial = true)
    var soundEnabled by remember { mutableStateOf(true) }
    var coordinatesEnabled by remember { mutableStateOf(true) }
    var autoQueenEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
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
                    .background(StatsTabBg)
                    .border(
                        width = 1.dp,
                        color = StatsTabBorder,
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
                        tint = StatsTextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Preferences",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = TextSizeHeading,
                color = StatsTextWhite
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(StatsScreenPadding),
            verticalArrangement = Arrangement.spacedBy(StatsSectionSpacing)
        ) {
            Text(
                text = "GAMEPLAY & AUDIO",
                fontFamily = JetBrainsMono,
                fontSize = TextSizeCaption,
                fontWeight = FontWeight.SemiBold,
                color = StatsTextMuted
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(StatsCardCornerRadius))
                    .background(StatsCardBg)
                    .border(1.dp, StatsCardBorder, RoundedCornerShape(StatsCardCornerRadius))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column {
                    PreferenceToggleRow(
                        title = "Sound Effects",
                        subtitle = "Play audio on piece moves, captures, and checks",
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                    HorizontalDivider(color = SettingsDivider, thickness = 1.dp)
                    PreferenceToggleRow(
                        title = "Haptic Feedback",
                        subtitle = "Vibrate device on piece drop",
                        checked = hapticsEnabled,
                        onCheckedChange = {
                            scope.launch {
                                context.settingsDataStore.edit { prefs -> prefs[HAPTICS_KEY] = it }
                            }
                        }
                    )
                    HorizontalDivider(color = SettingsDivider, thickness = 1.dp)
                    PreferenceToggleRow(
                        title = "Board Coordinates",
                        subtitle = "Show rank and file letters on board edges",
                        checked = coordinatesEnabled,
                        onCheckedChange = { coordinatesEnabled = it }
                    )
                    HorizontalDivider(color = SettingsDivider, thickness = 1.dp)
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
                fontSize = TextSizeCaption,
                fontWeight = FontWeight.SemiBold,
                color = StatsTextMuted
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(StatsCardCornerRadius))
                    .background(StatsCardBg)
                    .border(1.dp, StatsCardBorder, RoundedCornerShape(StatsCardCornerRadius))
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
                        fontSize = TextSizeBody,
                        color = StatsTextWhite
                    )
                    Text(
                        text = "1.0.0 (Build 1)",
                        fontFamily = JetBrainsMono,
                        fontSize = TextSizeBadge,
                        color = StatsCyan
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
                fontSize = TextSizeBody,
                color = StatsTextWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = JetBrainsMono,
                fontSize = TextSizeTiny,
                color = StatsTextMuted,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

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
}
