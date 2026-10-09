package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.abhinav.caisarra.R
import com.abhinav.caisarra.presentation.components.ChallengeCardBackground
import com.abhinav.caisarra.presentation.components.DailyRatingBackground
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.components.HomeBackground
import com.abhinav.caisarra.presentation.theme.JetBrainsMono
import com.abhinav.caisarra.presentation.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.abhinav.caisarra.presentation.game.components.GameHistorySection

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
    onResumeGame: () -> Unit = {},
    onReviewGame: (String) -> Unit = {},
    onLogoutSuccess: () -> Unit = {}
) {

    val state by viewModel.state.collectAsState()

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
                    .padding(top = 28.dp,
                        bottom = 32.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DashboardHeader(username = state.username)
                Spacer(modifier = Modifier.height(24.dp))
                DailyRating()

                Spacer(modifier = Modifier.height(12.dp))

                TodaysChallenge(onStartChallenge = onStartChallenge)
                Spacer(modifier = Modifier.height(12.dp))

//                GeneralButton(
//                    text = "RESUME GAME",
//                    onClick = onResumeGame,
//                    modifier = Modifier.width(200.dp)
//                )
//                Spacer(modifier = Modifier.height(20.dp))

                GameHistorySection(
                    games = state.gameHistory,
                    onReviewGame = onReviewGame
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
            }
        }
    }
}

@Composable
private fun DashboardHeader(username: String) {
    val currentDate = LocalDate.now()
    var currentTime by remember {
        mutableStateOf(LocalTime.now())
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            currentTime = LocalTime.now()
            delay(60_000L)
        }
    }
    val day = currentDate.dayOfMonth
        .toString()
        .padStart(2, '0')
    val month = currentDate
        .format(
            DateTimeFormatter.ofPattern(
                "MMM",
                Locale.ENGLISH
            )
        )
        .uppercase(Locale.ENGLISH)

    val dayName = currentDate
        .format(
            DateTimeFormatter.ofPattern(
                "EEEE",
                Locale.ENGLISH
            )
        )

    val greeting = when {
        currentTime.hour in 5..11 -> {
            "Good Morning"
        }

        currentTime.hour in 12..16 -> {
            "Good Afternoon"
        }

        currentTime.hour in 17..20 -> {
            "Good Evening"
        }

        else -> {
            "Good Night"
        }
    }

    Box(modifier = Modifier
            .fillMaxWidth()
            .height(102.dp)
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(DashboardColor)
            .border(
                width = 1.dp,
                color = DashboardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp
            )
    ) {

        Row(modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                androidx.compose.material3.Text(
                    text = "DASHBOARD",
                    color = White,
                    fontFamily = JetBrainsMono,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.material3.Text(
                    text = "$greeting $username",
                    color = White,
                    fontFamily = JetBrainsMono,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Text(
                    text = day,
                    color = White,
                    fontFamily = JetBrainsMono,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
                androidx.compose.material3.Text(
                    text = month,
                    color = White,
                    fontFamily = JetBrainsMono,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                androidx.compose.material3.Text(
                    text = dayName,
                    color = SecondaryText,
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp
                )
            }
        }
    }
}


@Composable
private fun DailyRating() {
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

            androidx.compose.material3.Text(
                text = "DAILY RATING",
                color = White,
                fontFamily = JetBrainsMono,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            androidx.compose.material3.Text(
                text = "1350",
                color = White,
                fontFamily = JetBrainsMono,
                fontSize = 48.sp,
                lineHeight = 50.sp,
                fontWeight = FontWeight.Bold
            )

            androidx.compose.material3.Text(
                text = "+16",
                color = Green,
                fontFamily = JetBrainsMono,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(
                    start = 78.dp
                )
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
    onStartChallenge: () -> Unit
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
