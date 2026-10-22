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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
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
    onReviewGame: (String) -> Unit = {},
    onLogoutSuccess: () -> Unit = {},
    onViewAllHistory: () -> Unit = {},
    onOpenNearby: () -> Unit = {},
    onPlayBots: () -> Unit = {},
    onOpenProfile: () -> Unit
) {

    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
                    bottom = 32.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HomeTopBar(
                    title = "Mosaic",
                    streak = state.streak,
                    displayName = state.displayName,
                    username = state.username,
                    avatarUrl = state.avatarUrl,
                    onOpenProfile = onOpenProfile
                )
                Spacer(modifier = Modifier.height(16.dp))
                RatingCard(
                    selectedMode = state.selectedMode,
                    rating = state.rating,
                    ratingChange = state.ratingChange,
                    provisional = state.provisional,
                    modeRatings = state.modeRatings,
                    onSelectMode = viewModel::selectMode
                )

                Spacer(modifier = Modifier.height(12.dp))

                TodaysChallenge(onStartChallenge = onStartChallenge)
                Spacer(modifier = Modifier.height(12.dp))

                GameHistorySection(
                    games = state.gameHistory,
                    onReviewGame = onReviewGame,
                    showViewAll = true,
                    onViewAll = onViewAllHistory,
                    isLoading = state.isLoadingHistory
                )
                Spacer(modifier = Modifier.height(20.dp))
                GeneralButton(text = if (state.isLoggingOut) {
                    "Logging Out..."
                } else {
                    "Log Out"
                },

                    onClick = {
                        viewModel.logout(
                            onSuccess = onLogoutSuccess
                        )
                    },

                    enabled = !state.isLoggingOut,
                    modifier = Modifier.width(200.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                GeneralButton(
                    text = "Chess Nearby",
                    onClick = onOpenNearby,
                    modifier = Modifier.width(200.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                GeneralButton(
                    text = "Play with Bot",
                    onClick = onPlayBots,
                    modifier = Modifier.width(200.dp)
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
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Streak
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                androidx.compose.material3.Icon(
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

            // Profile avatar button
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
    onSelectMode: (GameMode) -> Unit = {}
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
                    androidx.compose.material3.Text(
                        text = "${selectedMode.label.uppercase()} RATING",
                        color = SecondaryText,
                        fontFamily = JetBrainsMono,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    androidx.compose.material3.Icon(
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
                                    androidx.compose.material3.Text(
                                        text = mode.label,
                                        color = if (isSelected) Green else White,
                                        fontFamily = JetBrainsMono,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    val mRating = modeRatings[mode]
                                    if (mRating != null) {
                                        Spacer(Modifier.width(16.dp))
                                        androidx.compose.material3.Text(
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

            androidx.compose.material3.Text(
                text = rating?.toString() ?: "—",
                color = White,
                fontFamily = JetBrainsMono,
                fontSize = 48.sp,
                lineHeight = 50.sp,
                fontWeight = FontWeight.Bold
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

            androidx.compose.material3.Text(
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
                id = R.drawable.chess_board
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
private fun TodaysChallenge(
    onStartChallenge: () -> Unit,
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp)
    ) {
        ChallengeCardBackground(
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 11.dp,
                    bottom = 9.dp
                ),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            androidx.compose.material3.Text(
                text = "READY TO PLAY?",
                color = White,
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))

            androidx.compose.material3.Text(
                text = "Make your move",
                color = White,
                fontFamily = JetBrainsMono,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(15.dp))

            GeneralButton(
                text = "Play Game",

                onClick = onStartChallenge,

                modifier = Modifier.width(200.dp)
            )
        }
    }
}