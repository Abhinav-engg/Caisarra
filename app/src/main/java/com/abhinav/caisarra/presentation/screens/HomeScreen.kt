package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.abhinav.caisarra.data.repository.NotificationRepository
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.backgrounds.ChallengeCardBackground
import com.abhinav.caisarra.core.ui.backgrounds.DailyRatingBackground
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.backgrounds.HomeBackground
import com.abhinav.caisarra.presentation.game.components.GameHistorySection
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextOverflow
import com.abhinav.caisarra.presentation.common.singleClick
import com.caisaara.ui.theme.ButtonGradientEnd
import com.caisaara.ui.theme.ButtonGradientMid
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.core.ui.theme.SignUpButtonDefaultCornerRadius
import com.caisaara.ui.theme.ButtonGradientStart
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
import com.abhinav.caisarra.presentation.viewmodel.ActiveDailyGame
import com.abhinav.caisarra.presentation.viewmodel.ActiveGame
import com.abhinav.caisarra.presentation.viewmodel.HomeViewModel

private val BackgroundColor = Color(0xFF080D13)

private val White = Color(0xFFF5F5F5)
private val SecondaryText = Color(0xFFB8C2CC)

private val Green = Color(0xFF10B981)

private val DashboardColor = Color(0xFF151F2B)
private val DashboardBorder = Color(0xFF304154)


@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartChallenge: () -> Unit = {},
    onStartQuickPlay: () -> Unit = {},
    onOpenActiveGame: (String) -> Unit = {},
    onReviewGame: (String) -> Unit = {},
    onLogoutSuccess: () -> Unit = {},
    onViewAllHistory: () -> Unit = {},
    onOpenNearby: () -> Unit = {},
    onPlayBots: () -> Unit = {},
    onOpenProfile: () -> Unit,
    onOpenCommunity: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onStatsClick: (GameMode) -> Unit = {},
    onStreakClick: () -> Unit = {}
) {

    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val notificationRepo = remember { NotificationRepository.get(context) }
    val unreadCount by notificationRepo.unreadCount.collectAsState()

    LaunchedEffect(Unit) {
        notificationRepo.fetchUnreadCount()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        HomeBackground(modifier = Modifier.fillMaxSize())
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val contentWidth = minOf(
                maxWidth - 32.dp,
                420.dp
            )

            Column(modifier = Modifier
                .width(contentWidth)
                .align(Alignment.TopCenter)
                .safeDrawingPadding()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    top = 16.dp,
                    bottom = 100.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HomeTopBar(
                    title = "Mosaic",
                    streak = state.streak,
                    displayName = state.displayName,
                    username = state.username,
                    avatarUrl = state.avatarUrl,
                    unreadNotificationsCount = unreadCount,
                    onNotificationsClick = onOpenNotifications,
                    onOpenProfile = onOpenProfile,
                    onStreakClick = onStreakClick
                )
                Spacer(modifier = Modifier.height(16.dp))
                RatingCard(
                    selectedMode = state.selectedMode,
                    rating = state.rating,
                    ratingChange = state.ratingChange,
                    provisional = state.provisional,
                    modeRatings = state.modeRatings,
                    onSelectMode = viewModel::selectMode,
                    onStatsClick = { onStatsClick(state.selectedMode) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                QuickPlayAndActiveGamesSection(
                    activeGames = state.activeGames,
                    contentWidth = contentWidth,
                    timeControlLabel = when (state.selectedMode) {
                        GameMode.BULLET -> "1 min • Bullet"
                        GameMode.BLITZ -> "3 min • Blitz"
                        GameMode.RAPID -> "10 min • Rapid"
                        GameMode.DAILY -> "1 day • Daily"
                    },
                    onStartQuickPlay = onStartQuickPlay,
                    onOpenActiveGame = onOpenActiveGame,
                    onGameExpired = viewModel::removeExpiredGame
                )
                Spacer(modifier = Modifier.height(16.dp))

                GameHistorySection(
                    games = state.gameHistory,
                    onReviewGame = onReviewGame,
                    showViewAll = true,
                    onViewAll = onViewAllHistory,
                    isLoading = state.isLoadingHistory
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                BackgroundColor.copy(alpha = 0.88f),
                                BackgroundColor
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                GeneralButton(
                    text = "PLAY GAME",
                    onClick = onStartChallenge,
                    modifier = Modifier.width(contentWidth)
                )
            }
        }
    }
}

private fun extractInitials(displayName: String?, username: String): String {
    val name = displayName?.takeIf { it.isNotBlank() } ?: username
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "AB"
    val parts = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
    return if (parts.size >= 2) {
        "${parts[0].first()}${parts[1].first()}".uppercase()
    } else {
        trimmed.take(2).uppercase()
    }
}

@Composable
private fun HomeTopBar(
    title: String = "Mosaic",
    streak: Int,
    displayName: String?,
    username: String,
    avatarUrl: String?,
    unreadNotificationsCount: Int = 0,
    onNotificationsClick: () -> Unit = {},
    onStreakClick: () -> Unit = {},
    onOpenProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Text(
            text = title,
            color = White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = singleClick(onStreakClick)
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = Color(0xFFE28339),
                    modifier = Modifier.size(24.dp)
                )
                androidx.compose.material3.Text(
                    text = streak.toString(),
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = singleClick(onNotificationsClick)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = if (unreadNotificationsCount > 0) Color(0xFF00D8E8) else White.copy(alpha = 0.85f),
                    modifier = Modifier.size(22.dp)
                )
                if (unreadNotificationsCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 4.dp)
                            .size(if (unreadNotificationsCount > 9) 16.dp else 14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = if (unreadNotificationsCount > 99) "99+" else unreadNotificationsCount.toString(),
                            color = White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMono
                        )
                    }
                }
            }

            val initials = remember(displayName, username) {
                extractInitials(displayName, username)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF283448))
                    .clickable(onClick = onOpenProfile),
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUrl.isNullOrBlank() && avatarUrl.startsWith("https://", ignoreCase = true)) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    androidx.compose.material3.Text(
                        text = initials,
                        color = Color(0xFFCBD5E1),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


@Composable
private fun RatingCard(
    selectedMode: GameMode,
    rating: Int?,
    ratingChange: Int?,
    provisional: Boolean,
    modeRatings: Map<GameMode, Int?> = emptyMap(),
    onSelectMode: (GameMode) -> Unit = {},
    onStatsClick: () -> Unit = {}
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        DailyRatingBackground(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 8.dp,
                    top = 12.dp
                )
        ) {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { dropdownExpanded = true }
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "${selectedMode.label.uppercase()} RATING",
                        color = SecondaryText,
                        fontFamily = JetBrainsMono,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select rating mode",
                        tint = SecondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(DashboardColor)
                        .border(
                            width = 1.dp,
                            color = DashboardBorder,
                            shape = RoundedCornerShape(14.dp)
                        ),
                    containerColor = DashboardColor,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 8.dp,
                    tonalElevation = 0.dp
                ) {
                    val modes = listOf(
                        GameMode.RAPID,
                        GameMode.BLITZ,
                        GameMode.BULLET,
                        GameMode.DAILY
                    )
                    modes.forEach { mode ->
                        val isSelected = mode == selectedMode
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = mode.label,
                                        color = if (isSelected) Green else White,
                                        fontFamily = JetBrainsMono,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    val mRating = modeRatings[mode]
                                    if (mRating != null) {
                                        Spacer(Modifier.width(16.dp))
                                        Text(
                                            text = mRating.toString(),
                                            color = SecondaryText,
                                            fontFamily = JetBrainsMono,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onSelectMode(mode)
                                dropdownExpanded = false
                            },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = rating?.toString() ?: "—",
                color = White,
                fontFamily = JetBrainsMono,
                fontSize = 48.sp,
                lineHeight = 50.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onStatsClick)
            )

            val changeText = when {
                ratingChange != null -> {
                    when {
                        ratingChange > 0 -> "+$ratingChange"
                        ratingChange < 0 -> "$ratingChange"
                        else -> "+0"
                    }
                }
                provisional -> "PROVISIONAL"
                else -> "ESTABLISHED"
            }
            val changeColor = when {
                ratingChange != null -> {
                    when {
                        ratingChange > 0 -> Green
                        ratingChange < 0 -> Color(0xFFEF4444)
                        else -> SecondaryText
                    }
                }
                provisional -> Green
                else -> SecondaryText
            }

            Text(
                text = changeText,
                color = changeColor,
                fontFamily = JetBrainsMono,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        val boardWidth = maxWidth * 0.60f
        Image(
            painter = painterResource(
                id = R.drawable.chess_board1
            ),

            contentDescription = "Chess board",

            modifier = Modifier
                .width(boardWidth)
                .height(130.dp)
                .align(Alignment.TopEnd)
                .offset(
                    x = 5.dp,
                    y = 43.dp
                ),

            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun QuickPlayCard(
    onStartQuickPlay: () -> Unit,
    modifier: Modifier = Modifier,
    timeControlLabel: String = "10 min • Rapid",
    onSettingClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(172.dp)
    ) {
        ChallengeCardBackground(
            modifier = Modifier.fillMaxSize(),
            piecesAlignment = Alignment.BottomStart,
            alpha = 0.70f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "READY TO PLAY?",
                        color = Color(0xFF00E5FF),
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(SignUpButtonDefaultCornerRadius))
                            .background(Color(0xFF08202A))
                            .border(
                                width = 1.dp,
                                color = Color(0xFF154C59),
                                shape = RoundedCornerShape(SignUpButtonDefaultCornerRadius)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(),
                                onClick = singleClick(onSettingClick)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = timeControlLabel,
                            color = Color(0xFF5CE1E6),
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                androidx.compose.material3.Text(
                    text = "Quick Play",
                    color = White,
                    fontFamily = JetBrainsMono,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                androidx.compose.material3.Text(
                    text = "Find an evenly matched opponent instantly",
                    color = SecondaryText,
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            GeneralButton(
                text = "START QUICK GAME",
                onClick = onStartQuickPlay,
                modifier = Modifier
                    .fillMaxWidth(0.66f)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun QuickPlayAndActiveGamesSection(
    activeGames: List<ActiveGame>,
    contentWidth: androidx.compose.ui.unit.Dp,
    onStartQuickPlay: () -> Unit,
    onOpenActiveGame: (String) -> Unit,
    modifier: Modifier = Modifier,
    timeControlLabel: String = "10 min • Rapid",
    onGameExpired: (String) -> Unit = {}
) {
    if (activeGames.isEmpty()) {
        QuickPlayCard(
            onStartQuickPlay = onStartQuickPlay,
            timeControlLabel = timeControlLabel,
            modifier = modifier.fillMaxWidth()
        )
    } else {
        val listState = rememberLazyListState()
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
            ) {
                item(key = "quick_play") {
                    QuickPlayCard(
                        onStartQuickPlay = onStartQuickPlay,
                        timeControlLabel = timeControlLabel,
                        modifier = Modifier.width(contentWidth)
                    )
                }

                items(
                    items = activeGames,
                    key = { it.id }
                ) { game ->
                    ActiveGameCard(
                        game = game,
                        onClick = { onOpenActiveGame(game.id) },
                        onExpired = { onGameExpired(game.id) },
                        modifier = Modifier.width(contentWidth)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val totalItems = 1 + activeGames.size
            val currentIndex = listState.firstVisibleItemIndex.coerceIn(0, totalItems - 1)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalItems) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (index == currentIndex) 7.dp else 4.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex) Color(0xFF00E5FF)
                                else Color(0xFF304154)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveGameCard(
    game: ActiveGame,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onExpired: () -> Unit = {}
) {
    val isDaily = game.mode == GameMode.DAILY
    val daysText = if (game.daysPerMove == 1) "1 day/move" else "${game.daysPerMove} days/move"

    var remainingMs by remember(game.id, game.expiresAt) {
        mutableLongStateOf(
            game.expiresAt?.let { (it - System.currentTimeMillis()).coerceAtLeast(0L) } ?: 0L
        )
    }

    if (game.expiresAt != null) {
        LaunchedEffect(game.id, game.expiresAt) {
            while (true) {
                val now = System.currentTimeMillis()
                val left = (game.expiresAt - now).coerceAtLeast(0L)
                remainingMs = left
                if (left <= 0L) {
                    onExpired()
                    break
                }
                delay(1000)
            }
        }
    }

    val timeSubtitle = if (isDaily) {
        daysText
    } else {
        val totalSec = remainingMs / 1000L
        val clock = "%02d:%02d".format(totalSec / 60L, totalSec % 60L)
        "$clock left"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(172.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = singleClick(onClick)
            )
    ) {
        ChallengeCardBackground(
            modifier = Modifier.fillMaxSize(),
            piecesAlignment = Alignment.BottomStart,
            alpha = 0.70f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        text = "${game.mode.label.uppercase()} MATCH",
                        color = Color(0xFF00E5FF),
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(SignUpButtonDefaultCornerRadius))
                            .background(
                                if (game.isMyTurn) Color(0xFF064E3B) else Color(0xFF08202A)
                            )
                            .border(
                                width = 1.dp,
                                color = if (game.isMyTurn) Color(0xFF059669) else Color(0xFF154C59),
                                shape = RoundedCornerShape(SignUpButtonDefaultCornerRadius)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = if (game.isMyTurn) "YOUR TURN" else "OPPONENT'S TURN",
                            color = if (game.isMyTurn) Color(0xFF34D399) else Color(0xFF5CE1E6),
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                androidx.compose.material3.Text(
                    text = "vs ${game.opponentUsername}",
                    color = White,
                    fontFamily = JetBrainsMono,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                androidx.compose.material3.Text(
                    text = if (game.isMyTurn) {
                        "It's your turn to play • $timeSubtitle"
                    } else {
                        "Waiting for opponent to move • $timeSubtitle"
                    },
                    color = SecondaryText,
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            GeneralButton(
                text = if (game.isMyTurn) "PLAY MOVE" else "RESUME GAME",
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth(0.66f)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun ActiveDailyGameCard(
    game: ActiveDailyGame,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ActiveGameCard(
        game = game,
        onClick = onClick,
        modifier = modifier
    )
}