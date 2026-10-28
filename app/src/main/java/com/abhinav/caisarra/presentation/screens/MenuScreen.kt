package com.abhinav.caisarra.presentation.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.caisaara.ui.theme.BotLockedName
import com.caisaara.ui.theme.GoldColor
import com.caisaara.ui.theme.MenuFriendsGreen
import com.caisaara.ui.theme.MenuFriendsIconBg
import com.caisaara.ui.theme.MenuStatsIconBg
import com.caisaara.ui.theme.MenuThemeIconBg
import com.caisaara.ui.theme.SettingsDivider
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeSubhead
import com.caisaara.ui.theme.ThemeNeonDeep
import com.caisaara.ui.theme.ThemeSlateAccent

private val MenuSurface = Color(0xFF101923)
private val MenuOutline = Color(0xFF20313E)
private val MenuMuted = Color(0xFF8D9BAC)
private val MenuCyan = Color(0xFF00D5E8)

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
    onPrivacyClick: () -> Unit = {},
    onDirectMessagesClick: () -> Unit = {},
    onWatchClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    profileName: String? = null,
    username: String? = null,
    avatarUrl: String? = null,
    blitzRating: Int? = null,
    activeThemeName: String = "Midnight Cyan",
    isGuest: Boolean = false,
    hasUnreadMessages: Boolean = false
) {
    BackHandler { onBack() }

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogoutClick()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MENU",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 1.5.sp,
                    color = StatsTextWhite
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(StatsBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = MenuCyan.copy(alpha = 0.12f)),
                        onClick = onProfileClick
                    )
                    .padding(vertical = 12.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(MenuSurface)
                        .border(1.dp, MenuOutline, RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "$profileName profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(15.dp))
                        )
                    } else {
                        val initials = (profileName ?: username ?: if (isGuest) "GP" else "MP")
                            .trim()
                            .split(Regex("\\s+"))
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .joinToString("")
                            .ifBlank { if (isGuest) "GP" else "MP" }
                            .uppercase()

                        Text(
                            text = initials,
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFFE2EAF2)
                        )
                    }
                }
                Spacer(Modifier.width(13.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val displayName = profileName
                        ?.takeIf { it.isNotBlank() }
                        ?: username?.removePrefix("@")?.takeIf { it.isNotBlank() }
                        ?: if (isGuest) "Guest Player" else "Mosaic Player"

                    val displayUsername = if (isGuest) {
                        "Guest"
                    } else {
                        username?.takeIf { it.isNotBlank() }?.let { if (it.startsWith("@")) it else "@$it" } ?: ""
                    }

                    Text(
                        text = displayName,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = StatsTextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (displayUsername.isNotBlank()) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = displayUsername,
                            fontFamily = JetBrainsMono,
                            fontSize = 12.sp,
                            color = MenuMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = StatsCyan,
                        modifier = Modifier.size(27.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader(title = "Accounts & Profile")
            Spacer(Modifier.height(8.dp))
            MenuCard {
                MenuItem(Icons.Default.PersonOutline, StatsCyan, ThemeNeonDeep, "Profile", onClick = onProfileClick)
                ItemDivider()
                MenuItem(Icons.Default.History, StatsCyan, ThemeNeonDeep, "Game History", onClick = onGameHistoryClick)
                ItemDivider()
                MenuItem(Icons.Default.AutoGraph, StatsCyan, MenuStatsIconBg, "Stats", onClick = onStatsClick)
                ItemDivider()
                MenuItem(
                    icon = Icons.Default.Palette,
                    iconTint = MenuCyan,
                    iconBg = ThemeNeonDeep,
                    title = "Theme",
                    trailingLabel = activeThemeName,
                    onClick = onThemeClick
                )
                ItemDivider()
                MenuItem(Icons.Default.People, MenuFriendsGreen, MenuFriendsIconBg, "Friends", onClick = onFriendsClick)
                ItemDivider()
                MenuItem(
                    icon = Icons.Default.ChatBubbleOutline,
                    iconTint = MenuCyan,
                    iconBg = ThemeNeonDeep,
                    title = "Messages",
                    hasUnreadDot = hasUnreadMessages,
                    onClick = onDirectMessagesClick
                )
            }

            Spacer(Modifier.height(22.dp))
            SectionHeader(title = "Connect")
            Spacer(Modifier.height(8.dp))
            MenuCard {
                MenuItem(Icons.Default.Shield, MenuCyan, ThemeNeonDeep, "Clubs", onClick = onClubsClick)
                ItemDivider()
                MenuItem(Icons.Default.EmojiEvents, GoldColor, MenuThemeIconBg, "Leaderboard", onClick = onLeaderboardClick)
                ItemDivider()
                MenuItem(Icons.Default.Visibility, StatsCyan, ThemeNeonDeep, "Watch", onClick = onWatchClick)
            }

            Spacer(Modifier.height(22.dp))
            SectionHeader(title = "Account")
            Spacer(Modifier.height(8.dp))
            MenuCard {
                MenuItem(
                    icon = Icons.Default.Lock,
                    iconTint = StatsCyan,
                    iconBg = ThemeNeonDeep,
                    title = "Privacy Settings",
                    onClick = onPrivacyClick
                )
                ItemDivider()
                MenuItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    iconTint = ThemeSlateAccent,
                    iconBg = MenuSurface,
                    title = "Log out",
                    onClick = { showLogoutDialog = true }
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MenuSurface)
                .border(1.dp, Color(0xFF263B49), RoundedCornerShape(20.dp))
                .padding(horizontal = 22.dp, vertical = 21.dp)
        ) {
            Text(
                text = "Log out of Mosaic?",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFFE8EDF4)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "You can sign back in to continue your chess journey. Your account and progress will be kept.",
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MenuMuted
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MenuSurface)
                        .border(1.dp, MenuOutline, RoundedCornerShape(12.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancel", fontFamily = JetBrainsMono, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFFD4DCE5))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF00B8C8), Color(0xFF05D5DF))))
                        .clickable(onClick = onConfirm),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Log out", fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF04151B))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.SemiBold,
        fontSize = TextSizeCaption,
        letterSpacing = 0.2.sp,
        color = StatsTextWhite
    )
}

@Composable
private fun MenuCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MenuSurface)
            .border(BorderStroke(1.dp, StatsCardBorder), RoundedCornerShape(18.dp))
    ) { content() }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    trailingLabel: String? = null,
    hasUnreadDot: Boolean = false,
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
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(11.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = TextSizeSubhead,
            color = StatsTextWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (trailingLabel != null) {
            Text(
                text = trailingLabel,
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                color = MenuMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(7.dp))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = BotLockedName,
            modifier = Modifier.size(20.dp)
        )
        if (hasUnreadDot) {
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(7.dp).clip(CircleShape).background(MenuCyan))
        }
    }
}

@Composable
private fun ItemDivider() {
    HorizontalDivider(color = SettingsDivider, thickness = 1.dp, modifier = Modifier.padding(start = 67.dp, end = 14.dp))
}
