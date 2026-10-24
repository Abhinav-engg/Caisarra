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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
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
import com.abhinav.caisarra.core.ui.components.MainTabTopBar
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.caisaara.ui.theme.AmberNormal
import com.caisaara.ui.theme.BotLockedName
import com.caisaara.ui.theme.DeleteWarningBg
import com.caisaara.ui.theme.GoldColor
import com.caisaara.ui.theme.MenuBadgeBg
import com.caisaara.ui.theme.MenuBadgeBorder
import com.caisaara.ui.theme.MenuDialogBg
import com.caisaara.ui.theme.MenuFriendsGreen
import com.caisaara.ui.theme.MenuFriendsIconBg
import com.caisaara.ui.theme.MenuStatsIconBg
import com.caisaara.ui.theme.MenuThemeIconBg
import com.caisaara.ui.theme.SettingsDivider
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsDivider
import com.caisaara.ui.theme.StatsGreen
import com.caisaara.ui.theme.StatsRed
import com.caisaara.ui.theme.StatsTabBg
import com.caisaara.ui.theme.StatsTabBorder
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeHeading
import com.caisaara.ui.theme.TextSizeSubhead
import com.caisaara.ui.theme.TextSizeTiny
import com.caisaara.ui.theme.ThemeInactiveBadge
import com.caisaara.ui.theme.ThemeNeonDeep
import com.caisaara.ui.theme.ThemeNeonSky
import com.caisaara.ui.theme.ThemeSlateAccent
import androidx.compose.material.icons.filled.Chat

@Composable
fun MenuScreen(
    onBack: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onGameHistoryClick: () -> Unit = {},
    onThemeClick: () -> Unit = {},
    onFriendsClick: () -> Unit = {},
    onClubsClick: () -> Unit = {},
    onLeaderboardClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
    onDirectMessagesClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = MenuDialogBg,
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
                    fontSize = TextSizeBody,
                    color = StatsDivider
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
                        color = StatsRed
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = "CANCEL",
                        fontFamily = JetBrainsMono,
                        color = ThemeSlateAccent
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
    ) {
        MainTabTopBar(
            title = "MENU",
            icon = Icons.Filled.Menu,
            trailingContent = {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Preferences",
                        tint = StatsCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SectionHeader(
                title = "Accounts & Profile",
                badge = "5 ITEMS"
            )
            Spacer(modifier = Modifier.height(8.dp))
            MenuCard {
                MenuItem(
                    icon = Icons.Default.Person,
                    iconTint = StatsCyan,
                    iconBg = ThemeNeonDeep,
                    title = "Profile",
                    onClick = onProfileClick
                )
                ItemDivider()
                MenuItem(
                    icon = Icons.Default.AutoGraph,
                    iconTint = StatsGreen,
                    iconBg = MenuStatsIconBg,
                    title = "Stats",
                    onClick = onStatsClick
                )
                ItemDivider()
                MenuItem(
                    icon = Icons.Default.History,
                    iconTint = StatsCyan,
                    iconBg = ThemeNeonDeep,
                    title = "Game History",
                    onClick = onGameHistoryClick
                )
                ItemDivider()
                MenuItem(
                    icon = Icons.Default.Palette,
                    iconTint = AmberNormal,
                    iconBg = MenuThemeIconBg,
                    title = "Theme",
                    trailingBadge = "Midnight Cyan",
                    onClick = onThemeClick
                )
                ItemDivider()
                MenuItem(
                    icon = Icons.Default.People,
                    iconTint = MenuFriendsGreen,
                    iconBg = MenuFriendsIconBg,
                    title = "Friends",
                    onClick = onFriendsClick
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionHeader(title = "Connect")
            Spacer(modifier = Modifier.height(8.dp))
            MenuCard {
                MenuItem(
                    icon = Icons.Default.Shield,
                    iconTint = ThemeNeonSky,
                    iconBg = ThemeNeonDeep,
                    title = "Clubs",
                    onClick = onClubsClick
                )
                ItemDivider()
                MenuItem(
                    icon = Icons.Default.Chat,
                    iconTint = StatsCyan,
                    iconBg = ThemeNeonDeep,
                    title = "Direct Messages",
                    onClick = onDirectMessagesClick
                )
                ItemDivider()
                MenuItem(
                    icon = Icons.Default.EmojiEvents,
                    iconTint = GoldColor,
                    iconBg = MenuThemeIconBg,
                    title = "Leaderboard",
                    onClick = onLeaderboardClick
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionHeader(title = "Account")
            Spacer(modifier = Modifier.height(8.dp))
            MenuCard {
                MenuItem(
                    icon = Icons.Default.DeleteOutline,
                    iconTint = StatsRed,
                    iconBg = DeleteWarningBg,
                    title = "Delete Account",
                    titleColor = StatsRed,
                    chevronColor = StatsRed.copy(alpha = 0.7f),
                    onClick = onDeleteAccountClick
                )
                ItemDivider()
                MenuItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    iconTint = ThemeSlateAccent,
                    iconBg = ThemeInactiveBadge,
                    title = "Log Out",
                    onClick = { showLogoutDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
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
            fontSize = TextSizeCaption,
            letterSpacing = 0.4.sp,
            color = StatsTextMuted
        )
        if (badge != null) {
            Text(
                text = badge,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = TextSizeTiny,
                letterSpacing = 0.5.sp,
                color = StatsCyan
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
            .background(StatsCardBg)
            .border(
                border = BorderStroke(1.dp, StatsCardBorder),
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
    titleColor: Color = StatsTextWhite,
    trailingBadge: String? = null,
    hasUnreadDot: Boolean = false,
    chevronColor: Color = BotLockedName,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = StatsCyan.copy(alpha = 0.12f)),
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
            fontSize = TextSizeSubhead,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )

        if (trailingBadge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MenuBadgeBg)
                    .border(
                        width = 1.dp,
                        color = MenuBadgeBorder,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = trailingBadge,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = TextSizeTiny,
                    color = ThemeNeonSky
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (hasUnreadDot) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(StatsCyan)
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
        color = SettingsDivider,
        thickness = 1.dp,
        modifier = Modifier.padding(start = 66.dp, end = 14.dp)
    )
}
