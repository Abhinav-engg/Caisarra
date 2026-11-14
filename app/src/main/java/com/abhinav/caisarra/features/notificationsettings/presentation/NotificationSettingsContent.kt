package com.abhinav.caisarra.features.notificationsettings.presentation

import com.abhinav.caisarra.core.ui.components.rememberToaster
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.components.PreferenceToggleRow
import com.abhinav.caisarra.core.ui.components.SettingsCard
import com.abhinav.caisarra.core.ui.components.SettingsSectionHeader
import com.abhinav.caisarra.core.ui.theme.StatsScreenPadding
import com.abhinav.caisarra.core.ui.theme.StatsSectionSpacing
import com.caisaara.ui.theme.SettingsDivider
import com.caisaara.ui.theme.StatsBg

@Composable
fun NotificationSettingsContent(
    uiState: NotificationSettingsUiState,
    onBack: () -> Unit,
    onPushEnabledChange: (Boolean) -> Unit,
    onCategoryEnabledChange: (NotificationCategory, Boolean) -> Unit,
    onUpdateErrorShown: () -> Unit
) {
    val cardPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    val toaster = rememberToaster()

    LaunchedEffect(uiState.updateError) {
        uiState.updateError?.let {
            toaster.show(it)
            onUpdateErrorShown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CaisaaraTopBar(title = "NOTIFICATION SETTINGS", onBack = onBack, titleFontSize = 18.sp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(StatsScreenPadding),
                verticalArrangement = Arrangement.spacedBy(StatsSectionSpacing)
            ) {
                SettingsCard(contentPadding = cardPadding) {
                    PreferenceToggleRow(
                        title = "Push Notifications",
                        subtitle = "Receive notifications on this device",
                        checked = uiState.pushEnabled,
                        onCheckedChange = onPushEnabledChange,
                        enabled = !uiState.pushUpdating
                    )
                }

                NotificationCategory.entries.groupBy { it.section }.forEach { (section, categories) ->
                    SettingsSectionHeader(section)
                    SettingsCard(contentPadding = cardPadding) {
                        Column {
                            categories.forEachIndexed { index, category ->
                                if (index > 0) HorizontalDivider(color = SettingsDivider, thickness = 1.dp)
                                PreferenceToggleRow(
                                    title = category.title,
                                    subtitle = category.subtitle,
                                    checked = uiState.isEnabled(category),
                                    onCheckedChange = { onCategoryEnabledChange(category, it) },
                                    enabled = uiState.pushEnabled && category !in uiState.updatingCategories
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
