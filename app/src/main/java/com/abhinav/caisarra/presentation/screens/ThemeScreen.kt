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
import com.caisaara.ui.theme.AmberNormal
import com.caisaara.ui.theme.EmeraldNormal
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
import com.caisaara.ui.theme.TextSizeMicro
import com.caisaara.ui.theme.TextSizeSubhead
import com.caisaara.ui.theme.TextSizeTitle
import com.caisaara.ui.theme.ThemeActiveBadge
import com.caisaara.ui.theme.ThemeActiveCard
import com.caisaara.ui.theme.ThemeEmeraldCard
import com.caisaara.ui.theme.ThemeEmeraldDeep
import com.caisaara.ui.theme.ThemeInactiveBadge
import com.caisaara.ui.theme.ThemeNeonDeep
import com.caisaara.ui.theme.ThemeNeonSky
import com.caisaara.ui.theme.ThemePreviewBg
import com.caisaara.ui.theme.ThemePreviewBorder
import com.caisaara.ui.theme.ThemePurpleAccent
import com.caisaara.ui.theme.ThemePurpleCard
import com.caisaara.ui.theme.ThemePurpleBg
import com.caisaara.ui.theme.ThemePurpleLight
import com.caisaara.ui.theme.ThemeSlateAccent
import com.caisaara.ui.theme.ThemeSlateCard
import com.caisaara.ui.theme.ThemeSlateBg

@Composable
fun ThemeScreen(
    onBack: () -> Unit
) {
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

            ThemeOptionCard(
                name = "Midnight Cyan",
                description = "Dark cyberpunk aesthetic with luminous cyan accents",
                isActive = true,
                palette = listOf(
                    StatsBg,
                    ThemeNeonDeep,
                    StatsCyan,
                    ThemeNeonSky
                )
            )

            ThemeOptionCard(
                name = "Obsidian Dark",
                description = "Monochrome stealth palette with slate highlights",
                isActive = false,
                tag = "Coming Soon",
                palette = listOf(
                    ThemeSlateBg,
                    ThemeSlateCard,
                    ThemeSlateAccent,
                    StatsTextWhite
                )
            )

            ThemeOptionCard(
                name = "Royal Amethyst",
                description = "Deep violet hues paired with neon lavender glow",
                isActive = false,
                tag = "Coming Soon",
                palette = listOf(
                    ThemePurpleBg,
                    ThemePurpleCard,
                    ThemePurpleAccent,
                    ThemePurpleLight
                )
            )

            ThemeOptionCard(
                name = "Emerald Classic",
                description = "Classic tournament green with championship gold accents",
                isActive = false,
                tag = "Coming Soon",
                palette = listOf(
                    ThemeEmeraldDeep,
                    ThemeEmeraldCard,
                    EmeraldNormal,
                    AmberNormal
                )
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    name: String,
    description: String,
    isActive: Boolean,
    tag: String? = null,
    palette: List<Color>
) {
    val borderColor = if (isActive) StatsCyan else StatsCardBorder
    val cardBackground = if (isActive) ThemeActiveCard else StatsCardBg

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StatsCardCornerRadius))
            .background(cardBackground)
            .border(1.dp, borderColor, RoundedCornerShape(StatsCardCornerRadius))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        text = name,
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
                } else if (tag != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ThemePreviewBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tag,
                            fontFamily = JetBrainsMono,
                            fontSize = TextSizeMicro,
                            color = StatsTextMuted
                        )
                    }
                }
            }

            Text(
                text = description,
                fontFamily = JetBrainsMono,
                fontSize = TextSizeCaption,
                color = StatsTextMuted,
                lineHeight = TextSizeTitle
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                palette.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, ThemePreviewBorder, CircleShape)
                    )
                }
            }
        }
    }
}
