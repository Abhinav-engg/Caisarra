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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.components.PreferenceToggleRow
import com.abhinav.caisarra.core.ui.components.SettingsCard
import com.abhinav.caisarra.core.ui.components.SettingsNavCard
import com.abhinav.caisarra.core.ui.components.SettingsSectionHeader
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.core.ui.theme.StatsScreenPadding
import com.abhinav.caisarra.core.ui.theme.StatsSectionSpacing
import com.caisaara.ui.theme.RedNormal
import com.caisaara.ui.theme.SettingsDivider
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTabBg
import com.caisaara.ui.theme.StatsTabBorder
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeHeading
import com.abhinav.caisarra.data.local.SOUND_EFFECTS_KEY
import com.abhinav.caisarra.data.local.BOARD_COORDINATES_KEY
import com.abhinav.caisarra.presentation.game.components.isAutoQueenEnabled
import com.abhinav.caisarra.presentation.game.components.setAutoQueenEnabled

@Composable
fun SettingsDetailScreen(
    onBack: () -> Unit,
    onPrivacyClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    isGuest: Boolean = false,
    onDeleteAccountClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hapticsEnabled by remember(context) {
        context.settingsDataStore.data.map { it[HAPTICS_KEY] ?: true }
    }.collectAsState(initial = true)
    val soundEnabled by remember(context) {
        context.settingsDataStore.data.map {
            it[SOUND_EFFECTS_KEY] ?: true
        }
    }.collectAsState(initial = true)
    val coordinatesEnabled by remember(context) {
        context.settingsDataStore.data.map {
            it[BOARD_COORDINATES_KEY] ?: true
        }
    }.collectAsState(initial = true)

    var autoQueenEnabled by remember(context) {
        mutableStateOf(isAutoQueenEnabled(context))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
    ) {
        CaisaaraTopBar(
            title = "PREFERENCES",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(StatsScreenPadding),
            verticalArrangement = Arrangement.spacedBy(StatsSectionSpacing)
        ) {
            SettingsSectionHeader("GAMEPLAY & AUDIO")

            SettingsCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                Column {
                    PreferenceToggleRow(
                        title = "Sound Effects",
                        subtitle = "Play audio on piece moves and captures,and checks",
                        checked = soundEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                context.settingsDataStore.edit { prefs ->
                                    prefs[SOUND_EFFECTS_KEY] = enabled
                                }
                            }
                        }
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
                        onCheckedChange = { enabled ->
                            scope.launch {
                                context.settingsDataStore.edit { prefs ->
                                    prefs[BOARD_COORDINATES_KEY] = enabled
                                }
                            }
                        }
                    )
                    HorizontalDivider(color = SettingsDivider, thickness = 1.dp)
                    PreferenceToggleRow(
                        title = "Auto-Queen Promotion",
                        subtitle = "Always promote pawn to Queen automatically",
                        checked = autoQueenEnabled,
                        onCheckedChange = { enabled ->
                            setAutoQueenEnabled(context, enabled)
                            autoQueenEnabled = enabled
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SettingsSectionHeader("NOTIFICATIONS")
            SettingsNavCard(
                icon = Icons.Default.Notifications,
                title = "Notification Settings",
                subtitle = "Choose which alerts you receive",
                onClick = onNotificationsClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingsSectionHeader("PRIVACY & SECURITY")

            SettingsNavCard(
                icon = Icons.Default.Lock,
                title = "Privacy Settings",
                subtitle = "Control messages, clubs, tournaments & invites",
                onClick = onPrivacyClick
            )

            if (!isGuest) {
                Spacer(modifier = Modifier.height(10.dp))

                SettingsSectionHeader("DANGER ZONE", RedNormal)

                SettingsNavCard(
                    icon = Icons.Default.DeleteForever,
                    title = "Delete Account",
                    subtitle = "Permanently remove your account and all data",
                    onClick = onDeleteAccountClick,
                    iconTint = RedNormal,
                    titleColor = RedNormal
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            SettingsSectionHeader("ABOUT")

            SettingsCard {
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
