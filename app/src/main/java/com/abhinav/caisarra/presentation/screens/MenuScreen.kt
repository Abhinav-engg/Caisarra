package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono

private val ScreenBackground = Color(0xFF080D13)
private val CardBackground = Color(0xFF0F1722)
private val CardBorder = Color(0xFF1B2838)
private val DividerColor = Color(0xFF162230)
private val TextWhite = Color(0xFFF1F5F9)
private val TextMuted = Color(0xFF82909E)
private val CyanAccent = Color(0xFF23CDE8)
private val ChevronColor = Color(0xFF5A6B7E)
private val CoralRed = Color(0xFFF87171)
private val CoralRedBg = Color(0xFF2E171A)

@Composable
fun MenuScreen(
    onBack: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onGameHistoryClick: () -> Unit = {},
    onThemeClick: () -> Unit = {},
    onFriendsClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onClubsClick: () -> Unit = {},
    onLeaderboardClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color(0xFF111A24),
            title = {
                Text(
                    text = "Log Out",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of your account?",
                    fontFamily = JetBrainsMono,
                    fontSize = 14.sp,
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text(
                        text = "LOG OUT",
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        color = CoralRed
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = "CANCEL",
                        fontFamily = JetBrainsMono,
                        color = Color(0xFF94A4B8)
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .statusBarsPadding()
    ) {
        // Top App Bar
        MenuTopBar(
            onBack = onBack,
            onSettingsClick = onSettingsClick
        )

        // Scrollable Menu Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Section 1: Accounts & Profile
            SectionHeader(
                title = "Accounts & Profile",
                badge = "6 ITEMS"
            )
            Spacer(modifier = Modifier.height(8.dp))
            MenuCard {
                MenuItem(
                    icon = Icons.Default.Person,
                    iconTint = CyanAccent,
                    iconBg = Color(0xFF132B3A),
                    title = "Profile",
                    onClick = onProfileClick
                )
                ItemDivider()
                MenuItem(
                    icon = Icons.Default.AutoGraph,
                    iconTint = Color(0xFF2DD4BF),
                    iconBg = Color(0xFF122E2E),
                    title = "Stats",
                    onClick = onStatsClick
                )
                ItemDivider()
                MenuItem(
                    icon = Icons.Default.History,
                    iconTint = CyanAccent,
                    iconBg = Color(0xFF132B3A),
                    title = "Game History",
                    onClick = onGameHistoryClick
                )
                ItemDivider()
                MenuItem(
                    icon = Icons.Default.Palette,
                    iconTint = Color(0xFFF59E0B),
                    iconBg = Color(0xFF2D2417),
                    title = "Theme",
                    trailingBadge = "Midnight Cyan",
                    onClick = onThemeClick
                )
                ItemDivider()
                MenuItem(
                    icon = Icons.Default.People,
                    iconTint = Color(0xFF34D399),
                    iconBg = Color(0xFF0F2E23),
                    title = "Friends",
                    onClick = onFriendsClick
                )
                ItemDivider()
                MenuItem(
                    icon = Icons.Default.ChatBubble,
                    iconTint = Color(0xFF38BDF8),
                    iconBg = Color(0xFF132B3A),
                    title = "Messages",
                    hasUnreadDot = true,
                    onClick = onMessagesClick
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Connect
            SectionHeader(title = "Connect")
            Spacer(modifier = Modifier.height(8.dp))
            MenuCard {
                MenuItem(
                    icon = Icons.Default.Shield,
                    iconTint = Color(0xFF38BDF8),
                    iconBg = Color(0xFF132B3A),
                    title = "Clubs",
                    onClick = onClubsClick
                )
                ItemDivider()
                MenuItem(
                    icon = Icons.Default.EmojiEvents,
                    iconTint = Color(0xFFFBBF24),
                    iconBg = Color(0xFF2D2417),
                    title = "Leaderboard",
                    onClick = onLeaderboardClick
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Account
            SectionHeader(title = "Account")
            Spacer(modifier = Modifier.height(8.dp))
            MenuCard {
                MenuItem(
                    icon = Icons.Default.DeleteOutline,
                    iconTint = CoralRed,
                    iconBg = CoralRedBg,
                    title = "Delete Account",
                    titleColor = CoralRed,
                    chevronColor = CoralRed.copy(alpha = 0.7f),
                    onClick = onDeleteAccountClick
                )
                ItemDivider()
                MenuItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    iconTint = Color(0xFF94A4B8),
                    iconBg = Color(0xFF182330),
                    title = "Log Out",
                    onClick = { showLogoutDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MenuTopBar(
    onBack: () -> Unit,
    onSettingsClick: () -> Unit
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
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = "Settings",
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = TextWhite,
            modifier = Modifier.weight(1f)
        )

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
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Preferences",
                    tint = Color(0xFF94A4B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    badge: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 0.4.sp,
            color = TextMuted
        )
        if (badge != null) {
            Text(
                text = badge,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp,
                color = CyanAccent
            )
        }
    }
}

@Composable
private fun MenuCard(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(
                border = BorderStroke(1.dp, CardBorder),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        content()
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    titleColor: Color = TextWhite,
    trailingBadge: String? = null,
    hasUnreadDot: Boolean = false,
    chevronColor: Color = ChevronColor,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = CyanAccent.copy(alpha = 0.12f)),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )

        if (trailingBadge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF182A3A))
                    .border(
                        width = 1.dp,
                        color = Color(0xFF23445C),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = trailingBadge,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Color(0xFF38BDF8)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (hasUnreadDot) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CyanAccent)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = chevronColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ItemDivider() {
    HorizontalDivider(
        color = DividerColor,
        thickness = 1.dp,
        modifier = Modifier.padding(start = 66.dp, end = 14.dp)
    )
}
