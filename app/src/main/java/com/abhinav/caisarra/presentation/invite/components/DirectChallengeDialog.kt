package com.abhinav.caisarra.presentation.invite.components

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.data.repository.ChallengeManager
import com.abhinav.caisarra.presentation.game.components.setup.SetupColors
import com.abhinav.caisarra.presentation.game.screens.TimeControlsScreen
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
import com.abhinav.caisarra.presentation.game.viewmodel.GameSetupEvent
import com.abhinav.caisarra.presentation.game.viewmodel.GameSetupUiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val DialogBg = Color(0xFF0F1923)
private val DialogCard = Color(0xFF162534)
private val DialogBorder = Color(0xFF26384D)
private val AccentCyan = Color(0xFF00E5FF)
private val TextWhite = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF8A9AA8)

data class TimePreset(
    val label: String,
    val minutes: Int,
    val increment: Int
)

val DEFAULT_TIME_PRESETS = listOf(
    TimePreset("10 min", 10, 0),
    TimePreset("5 min", 5, 0),
    TimePreset("3 min", 3, 0),
    TimePreset("3+2", 3, 2),
    TimePreset("1 min", 1, 0)
)

fun resolveGameMode(minutes: Float): GameMode = when {
    minutes >= 1440f -> GameMode.DAILY
    minutes < 3f -> GameMode.BULLET
    minutes < 10f -> GameMode.BLITZ
    else -> GameMode.RAPID
}

fun formatTimeControlLabel(minutes: Float, incrementSeconds: Float): String {
    val totalMinutes = minutes.toInt()
    val increment = incrementSeconds.toInt()
    val duration = when (totalMinutes) {
        1440 -> "1 day"
        2880 -> "2 days"
        4320 -> "3 days"
        7200 -> "5 days"
        10080 -> "7 days"
        20160 -> "14 days"
        else -> "$totalMinutes min"
    }
    return if (increment == 0) duration else "$duration + $increment"
}

@Composable
fun DirectChallengeDialog(
    opponentId: Long,
    opponentUsername: String,
    opponentAvatarUrl: String? = null,
    opponentRating: Int? = null,
    onDismiss: () -> Unit,
    onGameStarted: (gameId: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val challengeManager = remember { ChallengeManager.get(context) }

    val outgoingChallenge by challengeManager.outgoingChallenge.collectAsState()

    var selectedMinutes by remember { mutableIntStateOf(10) }
    var selectedIncrement by remember { mutableIntStateOf(0) }
    var selectedControlLabel by remember { mutableStateOf("10 min") }
    var selectedMode by remember { mutableStateOf(GameMode.RAPID) }
    var showTimeControls by remember { mutableStateOf(false) }

    var timeControlState by remember {
        mutableStateOf(
            GameSetupUiState(
                minutes = selectedMinutes.toFloat(),
                bonusSeconds = selectedIncrement.toFloat(),
                selectedControl = selectedControlLabel,
                mode = selectedMode
            )
        )
    }

    var selectedColor by remember { mutableStateOf("random") }
    var isRated by remember { mutableStateOf(true) }

    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var sentCode by remember { mutableStateOf<String?>(null) }
    var sentGameId by remember { mutableStateOf<String?>(null) }
    var isDeclined by remember { mutableStateOf(false) }

    LaunchedEffect(opponentId, sentCode, sentGameId) {
        challengeManager.acceptedEvent.collectLatest { event ->
            if (sentCode != null && (event.code == sentCode || event.gameId == sentGameId ||
                        event.opponentId == opponentId ||
                        event.opponentUsername.equals(opponentUsername, ignoreCase = true))) {
                challengeManager.clearOutgoingChallenge()
                onGameStarted(if (event.gameId.isNotBlank()) event.gameId else (sentGameId ?: ""))
            }
        }
    }

    LaunchedEffect(opponentId, sentCode) {
        challengeManager.declinedEvent.collectLatest { event ->
            if (event.opponentId == null || event.opponentId == opponentId ||
                event.opponentUsername.equals(opponentUsername, ignoreCase = true)) {
                isDeclined = true
                isSending = false
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (showTimeControls) {
                showTimeControls = false
            } else {
                challengeManager.clearOutgoingChallenge()
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = !showTimeControls
        )
    ) {
        if (showTimeControls) {
            BackHandler(enabled = true) {
                showTimeControls = false
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SetupColors.Background)
            ) {
                TimeControlsScreen(
                    state = timeControlState,
                    onEvent = { event ->
                        when (event) {
                            is GameSetupEvent.ControlSelected -> {
                                timeControlState = timeControlState.copy(
                                    selectedControl = event.label,
                                    minutes = event.minutes,
                                    bonusSeconds = event.incrementSeconds,
                                    mode = event.mode
                                )
                            }
                            is GameSetupEvent.TimeChanged -> {
                                timeControlState = timeControlState.copy(
                                    minutes = event.minutes,
                                    selectedControl = formatTimeControlLabel(
                                        event.minutes,
                                        timeControlState.bonusSeconds
                                    ),
                                    mode = resolveGameMode(event.minutes)
                                )
                            }
                            is GameSetupEvent.BonusChanged -> {
                                timeControlState = timeControlState.copy(
                                    bonusSeconds = event.seconds,
                                    selectedControl = formatTimeControlLabel(
                                        timeControlState.minutes,
                                        event.seconds
                                    )
                                )
                            }
                        }
                    },
                    onConfirm = {
                        selectedMinutes = timeControlState.minutes.toInt()
                        selectedIncrement = timeControlState.bonusSeconds.toInt()
                        selectedControlLabel = timeControlState.selectedControl
                        selectedMode = timeControlState.mode
                        showTimeControls = false
                    },
                    onBack = {
                        showTimeControls = false
                    }
                )
            }
        } else {
            val shape = RoundedCornerShape(24.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(shape)
                    .background(DialogBg)
                    .border(1.dp, DialogBorder, shape)
                    .padding(20.dp)
            ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Direct Challenge",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    IconButton(
                        onClick = {
                            challengeManager.clearOutgoingChallenge()
                            onDismiss()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DialogCard)
                        .border(1.dp, DialogBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FriendAvatar(name = opponentUsername, avatarUrl = opponentAvatarUrl, size = 48.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = opponentUsername,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (opponentRating != null) {
                            Text(
                                text = "$opponentRating Rapid",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        } else {
                            Text(
                                text = "Ready to play",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                if (sentCode != null && !isDeclined) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = AccentCyan,
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = "Challenge sent to $opponentUsername!",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Waiting for response...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(24.dp))
                        GeneralButton(
                            text = "ENTER WAITING BOARD",
                            onClick = {
                                sentGameId?.let { gid ->
                                    challengeManager.clearOutgoingChallenge()
                                    onGameStarted(gid)
                                }
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DialogCard)
                                .border(1.dp, DialogBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    sentCode?.let { code ->
                                        scope.launch {
                                            challengeManager.cancelChallenge(code)
                                            challengeManager.clearOutgoingChallenge()
                                            onDismiss()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CANCEL CHALLENGE",
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else if (isDeclined) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$opponentUsername declined the challenge",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(18.dp))
                        GeneralButton(
                            text = "OK",
                            onClick = {
                                challengeManager.clearOutgoingChallenge()
                                onDismiss()
                            }
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TIME CONTROL",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$selectedControlLabel • ${selectedMode.label}",
                            color = AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickPresets = DEFAULT_TIME_PRESETS.take(4)
                        val isPresetSelected = quickPresets.any { preset ->
                            selectedMinutes == preset.minutes && selectedIncrement == preset.increment
                        }

                        quickPresets.forEach { preset ->
                            val selected = selectedMinutes == preset.minutes && selectedIncrement == preset.increment
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) Color(0xFF123638) else DialogCard)
                                    .border(1.dp, if (selected) AccentCyan else DialogBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedMinutes = preset.minutes
                                        selectedIncrement = preset.increment
                                        selectedControlLabel = preset.label
                                        selectedMode = resolveGameMode(preset.minutes.toFloat())
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset.label,
                                    color = if (selected) AccentCyan else TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        val isMoreSelected = !isPresetSelected
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isMoreSelected) Color(0xFF123638) else DialogCard)
                                .border(1.dp, if (isMoreSelected) AccentCyan else DialogBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    timeControlState = GameSetupUiState(
                                        minutes = selectedMinutes.toFloat(),
                                        bonusSeconds = selectedIncrement.toFloat(),
                                        selectedControl = selectedControlLabel,
                                        mode = selectedMode
                                    )
                                    showTimeControls = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "More",
                                color = if (isMoreSelected) AccentCyan else TextWhite,
                                fontSize = 12.sp,
                                fontWeight = if (isMoreSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "YOUR PIECES",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("white" to "White", "random" to "Random", "black" to "Black").forEach { (valKey, label) ->
                            val selected = selectedColor == valKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) Color(0xFF123638) else DialogCard)
                                    .border(1.dp, if (selected) AccentCyan else DialogBorder, RoundedCornerShape(12.dp))
                                    .clickable { selectedColor = valKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val circleColor = when (valKey) {
                                        "white" -> Color.White
                                        "black" -> Color.Black
                                        else -> if (selected) AccentCyan else TextMuted
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(circleColor, CircleShape)
                                            .border(1.dp, if (valKey == "black") TextMuted else Color.Transparent, CircleShape)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        color = if (selected) AccentCyan else TextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DialogCard)
                            .border(1.dp, DialogBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isRated) "Rated Game" else "Casual Game",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isRated) "Affects rating" else "Friendly match, rating unaffected",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = isRated,
                            onCheckedChange = { isRated = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF082028),
                                checkedTrackColor = AccentCyan,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = DialogBg
                            )
                        )
                    }

                    errorMessage?.let { err ->
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = err,
                            color = Color(0xFFEF4444),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    GeneralButton(
                        text = if (isSending) "SENDING CHALLENGE..." else "CHALLENGE $opponentUsername",
                        enabled = !isSending,
                        onClick = {
                            isSending = true
                            errorMessage = null
                            scope.launch {
                                challengeManager.sendChallenge(
                                    timeControlMinutes = selectedMinutes,
                                    incrementSeconds = selectedIncrement,
                                    color = selectedColor,
                                    rated = isRated,
                                    opponentId = opponentId,
                                    opponentUsername = opponentUsername
                                )
                                    .onSuccess { response ->
                                        isSending = false
                                        sentCode = response.inviteCode
                                        sentGameId = response.gameId
                                    }
                                    .onFailure { error ->
                                        isSending = false
                                        errorMessage = error.message ?: "Failed to send challenge"
                                    }
                            }
                        }
                    )
                }
            }
        }
    }
}
}
