package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.presentation.game.components.GameHistorySection
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.LocalTime

private val GuestBg = Color(0xFF080D13)
private val GuestPanel = Color(0xFF111D27)
private val GuestBorder = Color(0xFF263B4A)
private val GuestWhite = Color(0xFFF5F7FA)
private val GuestMuted = Color(0xFF9EAFBD)
private val GuestCyan = Color(0xFF05D5E5)

@Composable
fun GuestHomeScreen(
    guestId: String,
    gameHistory: List<GameEntity> = emptyList(),
    isLoadingHistory: Boolean = true,
    onPlayGame: () -> Unit = {},
    onReviewGame: (String) -> Unit = {},
    onSignIn: () -> Unit = {},
    onSignUp: () -> Unit = {},
    onLogout: () -> Unit = {},
    onViewAllHistory: () -> Unit = {},
    isLoggingOut: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GuestBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF102B38),
                            GuestBg
                        )
                    )
                )
                .align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 18.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            GuestBrandHeader()
            Spacer(modifier = Modifier.height(24.dp))

            GuestWelcomePanel(
                guestId = guestId
            )
            Spacer(modifier = Modifier.height(18.dp))

            PassAndPlayCard(
                onPlayGame = onPlayGame
            )

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "YOUR LOCAL GAMES",
                    color = GuestWhite,
                    fontFamily = JetBrainsMono,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF102A34))
                        .border(
                            1.dp,
                            GuestBorder,
                            RoundedCornerShape(50)
                        )
                        .padding(
                            horizontal = 11.dp,
                            vertical = 6.dp
                        )
                ) {
                    Text(
                        text = "GUEST MODE",
                        color = GuestCyan,
                        fontFamily = JetBrainsMono,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GameHistorySection(
                games = gameHistory,
                onReviewGame = onReviewGame,
                showViewAll = true,
                onViewAll = onViewAllHistory,
                isLoading = isLoadingHistory
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(GuestBorder)
                )

                Text(
                    text = "  ♛  ",
                    color = GuestMuted,
                    fontSize = 20.sp
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(GuestBorder)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Want to keep your progress across devices?",
                color = GuestMuted,
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            GeneralButton(
                text = "SIGN IN",
                onClick = onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            GeneralButton(
                text = "CREATE ACCOUNT",
                onClick = onSignUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            GeneralButton(
                text = if (isLoggingOut) {
                    "ENDING GUEST SESSION..."
                } else {
                    "EXIT GUEST MODE"
                },
                onClick = onLogout,
                enabled = !isLoggingOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
    }
}

@Composable
private fun GuestBrandHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "♛",
            color = GuestWhite,
            fontSize = 28.sp
        )

        Text(
            text = "MOSAIC",
            color = GuestWhite,
            fontSize = 27.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(30.dp)
                    .height(2.dp)
                    .background(GuestCyan)
            )

            Text(
                text = "  C H E S S  ",
                color = GuestCyan,
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .width(30.dp)
                    .height(2.dp)
                    .background(GuestCyan)
            )
        }
    }
}

@Composable
private fun GuestWelcomePanel(
    guestId: String
) {
    var currentTime by remember {
        mutableStateOf(LocalTime.now())
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            currentTime = LocalTime.now()
            delay(60_000L)
        }
    }

    val greeting = when (currentTime.hour) {
        in 5..11 -> "GOOD MORNING"
        in 12..16 -> "GOOD AFTERNOON"
        in 17..20 -> "GOOD EVENING"
        else -> "GOOD NIGHT"
    }

    val displayGuestId = guestId.trim()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF142B36),
                        GuestPanel,
                        Color(0xFF0D1821)
                    )
                )
            )
            .border(
                1.dp,
                GuestBorder,
                RoundedCornerShape(22.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "PLAY AS GUEST",
                color = GuestCyan,
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = greeting,
                color = GuestMuted,
                fontFamily = JetBrainsMono,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "CHESS PLAYER",
                color = GuestWhite,
                fontFamily = JetBrainsMono,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "GUEST ID",
                color = GuestMuted,
                fontFamily = JetBrainsMono,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = displayGuestId.ifBlank {
                    "Guest ID unavailable"
                },
                color = GuestCyan,
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                softWrap = true
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Image(
            painter = painterResource(
                id = R.drawable.challenge_pieces
            ),
            contentDescription = "Chess pieces",
            modifier = Modifier.size(112.dp),
            contentScale = ContentScale.Fit
        )
    }
}


@Composable
private fun PassAndPlayCard(
    onPlayGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF19323D),
                        Color(0xFF111F29),
                        Color(0xFF0D1720)
                    )
                )
            )
            .border(
                1.dp,
                Color(0xFF17606A),
                RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
    ) {

        Text(
            text = "LOCAL MULTIPLAYER",
            color = GuestCyan,
            fontFamily = JetBrainsMono,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "PASS & PLAY",
            color = GuestWhite,
            fontFamily = JetBrainsMono,
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "One device. Two players.\nTake turns moving the pieces\nand enjoy a face-to-face match.",
                modifier = Modifier.weight(1f),
                color = GuestMuted,
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Image(
                painter = painterResource(
                    id = R.drawable.chess_board1
                ),
                contentDescription = "Chess board",
                modifier = Modifier
                    .width(112.dp)
                    .height(78.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        GeneralButton(
            text = "START PASS & PLAY",
            onClick = onPlayGame,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        )
    }
}
