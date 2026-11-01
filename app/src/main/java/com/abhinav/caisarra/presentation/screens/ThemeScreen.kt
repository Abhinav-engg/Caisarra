package com.abhinav.caisarra.presentation.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.core.ui.theme.StatsCardCornerRadius
import com.abhinav.caisarra.core.ui.theme.StatsScreenPadding
import com.abhinav.caisarra.core.ui.theme.StatsSectionSpacing
import com.abhinav.caisarra.data.local.BOARD_THEME_KEY
import com.abhinav.caisarra.data.local.settingsDataStore
import com.abhinav.caisarra.presentation.game.model.BoardTheme
import com.abhinav.caisarra.presentation.game.model.BoardThemeId
import com.abhinav.caisarra.presentation.game.model.BoardThemes
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTabBg
import com.caisaara.ui.theme.StatsTabBorder
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeHeading
import com.caisaara.ui.theme.TextSizeSubhead
import com.caisaara.ui.theme.TextSizeTitle
import com.caisaara.ui.theme.ThemeActiveBadge
import com.caisaara.ui.theme.ThemeActiveCard
import com.caisaara.ui.theme.ThemeInactiveBadge
import com.caisaara.ui.theme.ThemePreviewBorder
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun ThemeScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentThemeKey by remember(context) {
        context.settingsDataStore.data.map {
            it[BOARD_THEME_KEY] ?: BoardThemeId.MIDNIGHT_CYAN.storageKey
        }
    }.collectAsState(initial = BoardThemeId.MIDNIGHT_CYAN.storageKey)

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
                text = "Theme",
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
                text = "Choose your board and interface aesthetic.",
                fontFamily = JetBrainsMono,
                fontSize = TextSizeBadge,
                color = StatsTextMuted
            )

            BoardThemes.allThemes.forEach { theme ->
                val isSelected = currentThemeKey == theme.id.storageKey
                ThemeOptionCard(
                    theme = theme,
                    isActive = isSelected,
                    onClick = {
                        scope.launch {
                            context.settingsDataStore.edit { prefs ->
                                prefs[BOARD_THEME_KEY] = theme.id.storageKey
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionCard(
    theme: BoardTheme,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isActive) StatsCyan else StatsCardBorder
    val cardBackground = if (isActive) ThemeActiveCard else StatsCardBg

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StatsCardCornerRadius))
            .background(cardBackground)
            .border(1.dp, borderColor, RoundedCornerShape(StatsCardCornerRadius))
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isActive) ThemeActiveBadge else ThemeInactiveBadge),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = if (isActive) StatsCyan else StatsTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = theme.name,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = TextSizeSubhead,
                        color = StatsTextWhite
                    )
                }

                if (isActive) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Active",
                        tint = StatsCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = theme.description,
                fontFamily = JetBrainsMono,
                fontSize = TextSizeCaption,
                color = StatsTextMuted,
                lineHeight = TextSizeTitle
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    theme.palette.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, ThemePreviewBorder, CircleShape)
                        )
                    }
                }

                MiniBoardPreview(
                    lightSquare = theme.lightSquare,
                    darkSquare = theme.darkSquare,
                    accentColor = theme.selectedColor
                )
            }
        }
    }
}

@Composable
private fun MiniBoardPreview(
    lightSquare: Color,
    darkSquare: Color,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, ThemePreviewBorder, RoundedCornerShape(6.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            repeat(4) { row ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    repeat(4) { col ->
                        val isDark = (row + col) % 2 == 1
                        val isAccent = row == 2 && col == 1
                        val bg = when {
                            isAccent -> accentColor
                            isDark -> darkSquare
                            else -> lightSquare
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(bg)
                        )
                    }
                }
            }
        }
    }
}
