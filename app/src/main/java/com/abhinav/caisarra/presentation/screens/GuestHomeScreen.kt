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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.presentation.game.components.GameHistorySection
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.LocalTime

private val GuestBg = Color(0xFF080D13)
private val GuestPanel = Color(0xFF121D29)
private val GuestBorder = Color(0xFF263B4A)
private val GuestWhite = Color(0xFFF5F7FA)
private val GuestMuted = Color(0xFF9EAFBD)
private val GuestCyan = Color(0xFF05D5E5)
private val GuestTeal = Color(0xFF168B98)

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
                .height(250.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF102B38).copy(alpha = 0.7f), GuestBg)
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
                .padding(top = 20.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GuestWelcomePanel(guestId = guestId)
            Spacer(Modifier.height(22.dp))

            PassAndPlayCard(onPlayGame = onPlayGame)
            Spacer(Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "YOUR LOCAL GAMES",
                    color = GuestWhite,
                    fontFamily = JetBrainsMono,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "GUEST MODE",
                    color = GuestCyan,
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(12.dp))

            GameHistorySection(
                games = gameHistory,
                onReviewGame = onReviewGame,
                showViewAll = true,
                onViewAll = onViewAllHistory,
                isLoading = isLoadingHistory
            )

            Spacer(Modifier.height(26.dp))
            Text(
                text = "Want to keep your progress across devices?",
                color = GuestMuted,
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            GeneralButton(
                text = "SIGN IN",
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            )
            Spacer(Modifier.height(10.dp))
            GeneralButton(
                text = "CREATE ACCOUNT",
                onClick = onSignUp,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            )
            Spacer(Modifier.height(10.dp))
            GeneralButton(
                text = if (isLoggingOut) "ENDING GUEST SESSION..." else "EXIT GUEST MODE",
                onClick = onLogout,
                enabled = !isLoggingOut,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            )
        }
    }
}

@Composable
private fun GuestWelcomePanel(guestId: String) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(GuestPanel)
            .border(1.dp, GuestBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            text = "MOSAIC CHESS",
            color = GuestCyan,
            fontFamily = JetBrainsMono,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "PLAY AS GUEST",
            color = GuestWhite,
            fontFamily = JetBrainsMono,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "$greeting, CHESS PLAYER",
            color = GuestMuted,
            fontFamily = JetBrainsMono,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Guest ID: ${guestId.ifBlank { "Preparing guest session…" }}",
            color = GuestMuted,
            fontFamily = JetBrainsMono,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun PassAndPlayCard(onPlayGame: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF263D48), Color(0xFF17242F), Color(0xFF101923))
                )
            )
            .border(1.dp, Color(0xFF365765), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            text = "LOCAL MULTIPLAYER",
            color = GuestCyan,
            fontFamily = JetBrainsMono,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "PASS & PLAY",
            color = GuestWhite,
            fontFamily = JetBrainsMono,
            fontSize = 27.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "One device. Two players. Take turns moving the pieces and enjoy a face-to-face chess match.",
            color = GuestMuted,
            fontFamily = JetBrainsMono,
            fontSize = 12.sp,
            lineHeight = 19.sp
        )
        Spacer(Modifier.height(20.dp))
        GeneralButton(
            text = "START PASS & PLAY",
            onClick = onPlayGame,
            modifier = Modifier.fillMaxWidth().height(54.dp)
        )
    }
}
