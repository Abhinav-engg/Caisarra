
package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.components.EyebrowCard
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.components.SearchPulse
import com.abhinav.caisarra.core.ui.components.SecondaryButton
import com.abhinav.caisarra.core.ui.components.responsiveContentWidth
import com.abhinav.caisarra.core.ui.theme.StatsScreenPadding
import com.abhinav.caisarra.presentation.game.viewmodel.MatchmakingViewModel
import com.abhinav.caisarra.presentation.game.viewmodel.gameModeFor
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsRed
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import kotlinx.coroutines.delay

private fun formatTimeControl(
    minutes: Int,
    incrementSeconds: Int
): String {
    val duration = when (minutes) {
        1440 -> "1 day"
        2880 -> "2 days"
        4320 -> "3 days"
        7200 -> "5 days"
        10080 -> "7 days"
        20160 -> "14 days"
        else -> return "$minutes + $incrementSeconds"
    }
    return if (incrementSeconds > 0) {
        "$duration + ${incrementSeconds}s"
    } else {
        duration
    }
}

@Composable
fun MatchmakingScreen(
    viewModel: MatchmakingViewModel,
    minutes: Int,
    incrementSeconds: Int,
    onMatched: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.matchedGameId) {
        state.matchedGameId?.let { gameId ->
            viewModel.clearMatchedGame()
            onMatched(gameId)
        }
    }

    var elapsed by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.isSearching) {
        elapsed = 0
        while (state.isSearching) {
            delay(1000)
            elapsed++
        }
    }

    Column(Modifier.fillMaxSize().background(StatsBg).statusBarsPadding()) {
        CaisaaraTopBar(title = "FINDING YOUR MATCH", onBack = onBack, titleFontSize = 18.sp)
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            val contentWidth = responsiveContentWidth(maxWidth)
            Column(
                modifier = Modifier
                    .width(contentWidth)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = StatsScreenPadding)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EyebrowCard(
                    eyebrow = "RATED ${gameModeFor(minutes.toFloat()).label.uppercase()} · ${formatTimeControl(minutes, incrementSeconds)}",
                    title = "A worthy opponent awaits.",
                    subtitle = "Matching you with a player near your rating."
                )
                if (state.isSearching) {
                    SearchPulse(
                        icon = painterResource(R.drawable.ic_swords),
                        size = (contentWidth * 0.5f).coerceIn(140.dp, 220.dp),
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                    Text(
                        text = "Searching · %02d:%02d".format(elapsed / 60, elapsed % 60),
                        color = StatsTextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Usually connects in under 15 seconds",
                        color = StatsTextMuted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    SecondaryButton(
                        text = if (state.isCancelling) "Cancelling..." else "Cancel search",
                        onClick = viewModel::cancelMatch,
                        enabled = !state.isCancelling
                    )
                } else {
                    Text(
                        text = state.error ?: "Search ended",
                        color = if (state.error != null) StatsRed else StatsTextWhite,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                    GeneralButton(
                        text = "TRY AGAIN",
                        onClick = { viewModel.startMatch(minutes = minutes, incrementSeconds = incrementSeconds) }
                    )
                    SecondaryButton(text = "Back", onClick = onBack)
                }
            }
        }
    }

    state.tiltWarning?.let {
        AlertDialog(
            onDismissRequest = viewModel::dismissTiltWarning,
            title = {
                Text("Take a short break?")
            },
            text = {
                Text(
                    "The server detected a possible tilt pattern. " +
                            "You can take a break or continue with a rated game."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.retryAfterTiltWarning(
                            minutes,
                            incrementSeconds
                        )
                    }
                ) {
                    Text("CONTINUE")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::dismissTiltWarning
                ) {
                    Text("CANCEL")
                }
            }
        )
    }
}
