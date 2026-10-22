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

private val BackgroundColor = Color(0xFF080D13)
private val CardBg = Color(0xFF0F1722)
private val CardBorderColor = Color(0xFF1B2838)
private val TextWhiteColor = Color(0xFFF1F5F9)
private val TextMutedColor = Color(0xFF82909E)
private val CyanColor = Color(0xFF23CDE8)

@Composable
fun ThemeScreen(
    onBack: () -> Unit
) {
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
                text = "Theme",
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
                text = "Choose your board and interface aesthetic.",
                fontFamily = JetBrainsMono,
                fontSize = 13.sp,
                color = TextMutedColor
            )

            // Active Theme
            ThemeOptionCard(
                name = "Midnight Cyan",
                description = "Dark cyberpunk aesthetic with luminous cyan accents",
                isActive = true,
                palette = listOf(
                    Color(0xFF080D13),
                    Color(0xFF132B3A),
                    Color(0xFF23CDE8),
                    Color(0xFF38BDF8)
                )
            )

            // Upcoming Themes
            ThemeOptionCard(
                name = "Obsidian Dark",
                description = "Monochrome stealth palette with slate highlights",
                isActive = false,
                tag = "Coming Soon",
                palette = listOf(
                    Color(0xFF0B0E14),
                    Color(0xFF1E232A),
                    Color(0xFF94A4B8),
                    Color(0xFFF1F5F9)
                )
            )

            ThemeOptionCard(
                name = "Royal Amethyst",
                description = "Deep violet hues paired with neon lavender glow",
                isActive = false,
                tag = "Coming Soon",
                palette = listOf(
                    Color(0xFF10081C),
                    Color(0xFF2E1948),
                    Color(0xFFC084FC),
                    Color(0xFFE9D5FF)
                )
            )

            ThemeOptionCard(
                name = "Emerald Classic",
                description = "Classic tournament green with championship gold accents",
                isActive = false,
                tag = "Coming Soon",
                palette = listOf(
                    Color(0xFF071A12),
                    Color(0xFF0E3D29),
                    Color(0xFF10B981),
                    Color(0xFFF59E0B)
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
    val borderColor = if (isActive) CyanColor else CardBorderColor
    val cardBackground = if (isActive) Color(0xFF0D1E2A) else CardBg

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
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
                            .background(if (isActive) Color(0xFF183B4E) else Color(0xFF182230)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = if (isActive) CyanColor else TextMutedColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = name,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextWhiteColor
                    )
                }

                if (isActive) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Active",
                        tint = CyanColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (tag != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF192330))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tag,
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                            color = TextMutedColor
                        )
                    }
                }
            }

            Text(
                text = description,
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                color = TextMutedColor,
                lineHeight = 18.sp
            )

            // Color dots preview
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
                            .border(1.dp, Color(0xFF2D3C4E), CircleShape)
                    )
                }
            }
        }
    }
}
