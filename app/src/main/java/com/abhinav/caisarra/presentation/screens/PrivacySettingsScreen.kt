package com.abhinav.caisarra.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.core.ui.theme.StatsCardCornerRadius
import com.abhinav.caisarra.core.ui.theme.StatsScreenPadding
import com.abhinav.caisarra.core.ui.theme.StatsSectionSpacing
import com.abhinav.caisarra.data.remote.dto.PrivacyOption
import com.abhinav.caisarra.presentation.viewmodel.PrivacySection
import com.abhinav.caisarra.presentation.viewmodel.PrivacySettingsViewModel
import com.caisaara.ui.theme.SettingsDivider
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsGreen
import com.caisaara.ui.theme.StatsRed
import com.caisaara.ui.theme.StatsTabBg
import com.caisaara.ui.theme.StatsTabBorder
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeHeading
import com.caisaara.ui.theme.TextSizeMicro
import com.caisaara.ui.theme.TextSizeTiny
import com.caisaara.ui.theme.ThemeNeonDeep

@Composable
fun PrivacySettingsScreen(
    viewModel: PrivacySettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            CaisaaraTopBar(
                title = "PRIVACY SETTINGS",
                onBack = onBack,
                backgroundColor = StatsBg,
                titleColor = StatsTextWhite,
                iconTint = StatsTextWhite,
                titleFontSize = 18.sp,
                actions = {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = StatsCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.loadSettings() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = StatsCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )

            if (uiState.isLoading && !uiState.hasLoaded) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = StatsCyan)
                }
            } else if (uiState.errorMessage != null && !uiState.hasLoaded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Failed to load privacy settings",
                            fontFamily = JetBrainsMono,
                            fontSize = TextSizeBody,
                            color = StatsRed,
                            textAlign = TextAlign.Center
                        )
                        TextButton(onClick = { viewModel.loadSettings() }) {
                            Text(
                                text = "RETRY",
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                color = StatsCyan
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(StatsScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(StatsSectionSpacing)
                ) {
                    // Header Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(StatsCardCornerRadius))
                            .background(StatsCardBg)
                            .border(1.dp, StatsCardBorder, RoundedCornerShape(StatsCardCornerRadius))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ThemeNeonDeep),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = StatsCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "INTERACTION CONTROLS",
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = TextSizeCaption,
                                    color = StatsCyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Choose who is permitted to send you direct messages or friend requests across Mosaic.",
                                    fontFamily = JetBrainsMono,
                                    fontSize = TextSizeTiny,
                                    color = StatsTextMuted,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Section 1: Direct Messages
                    PrivacySectionCard(
                        section = PrivacySection.DIRECT_MESSAGE,
                        icon = Icons.AutoMirrored.Filled.Chat,
                        subtitle = "Who can send you direct private messages",
                        currentOption = uiState.settings.directMessageOption,
                        isUpdating = uiState.updatingSection == PrivacySection.DIRECT_MESSAGE,
                        onOptionSelected = { option ->
                            viewModel.updateSection(PrivacySection.DIRECT_MESSAGE, option)
                        }
                    )

                    // Section 2: Friend Invites
                    PrivacySectionCard(
                        section = PrivacySection.INVITE_FRIEND,
                        icon = Icons.Default.PersonAdd,
                        subtitle = "Who can send you friend requests",
                        currentOption = uiState.settings.inviteFriendOption,
                        isUpdating = uiState.updatingSection == PrivacySection.INVITE_FRIEND,
                        onOptionSelected = { option ->
                            viewModel.updateSection(PrivacySection.INVITE_FRIEND, option)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun PrivacySectionCard(
    section: PrivacySection,
    icon: ImageVector,
    subtitle: String,
    currentOption: PrivacyOption,
    isUpdating: Boolean,
    onOptionSelected: (PrivacyOption) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StatsCardCornerRadius))
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, RoundedCornerShape(StatsCardCornerRadius))
            .padding(16.dp)
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ThemeNeonDeep),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = StatsCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = section.title,
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = TextSizeBody,
                            color = StatsTextWhite
                        )
                        Text(
                            text = subtitle,
                            fontFamily = JetBrainsMono,
                            fontSize = TextSizeMicro,
                            color = StatsTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = StatsCyan,
                        strokeWidth = 2.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatsTabBg)
                            .border(1.dp, StatsTabBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = currentOption.label.uppercase(),
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = TextSizeMicro,
                            color = StatsCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = SettingsDivider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Options List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PrivacyOption.entries.forEach { option ->
                    val isSelected = currentOption == option
                    PrivacyOptionItem(
                        option = option,
                        isSelected = isSelected,
                        enabled = !isUpdating,
                        onClick = { onOptionSelected(option) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyOptionItem(
    option: PrivacyOption,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) StatsCyan else StatsCardBorder
    val bgColor = if (isSelected) ThemeNeonDeep.copy(alpha = 0.35f) else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.label,
                    fontFamily = JetBrainsMono,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = TextSizeBadge,
                    color = if (isSelected) StatsTextWhite else StatsTextMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = option.description,
                    fontFamily = JetBrainsMono,
                    fontSize = TextSizeTiny,
                    color = StatsTextMuted,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) StatsCyan else Color.Transparent)
                    .border(
                        1.5.dp,
                        if (isSelected) StatsCyan else StatsTabBorder,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = StatsBg,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
