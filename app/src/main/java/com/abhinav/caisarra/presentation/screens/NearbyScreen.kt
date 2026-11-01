package com.abhinav.caisarra.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.abhinav.caisarra.data.remote.dto.NearbyPlayer
import com.abhinav.caisarra.presentation.common.singleClick
import com.abhinav.caisarra.presentation.invite.components.DirectChallengeDialog
import com.abhinav.caisarra.presentation.nearby.NearbyColors
import com.abhinav.caisarra.presentation.nearby.NearbyMapState
import com.abhinav.caisarra.presentation.nearby.NearbyViewModel
import com.abhinav.caisarra.presentation.nearby.components.NearbyMap
import com.abhinav.caisarra.presentation.nearby.components.PlayerCard
import com.abhinav.caisarra.presentation.nearby.components.SquareIconButton
import com.caisaara.ui.theme.CyanLight
import com.caisaara.ui.theme.StatsBarSlate
import com.caisaara.ui.theme.White

@Composable
fun NearbyScreen(
    viewModel: NearbyViewModel,
    onBack: () -> Unit = {},
    onAddFriend: (NearbyPlayer) -> Unit = {},
    onChat: (NearbyPlayer) -> Unit = {},
    onRefresh: (() -> Unit)? = null,
    onGameStarted: (String) -> Unit = {}
) {
    BackHandler(onBack = onBack)

    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isFirstResume by remember { mutableStateOf(true) }
    var challengingPlayer by remember { mutableStateOf<NearbyPlayer?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "refreshSpin")
    val refreshRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refreshRotation"
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (isFirstResume) {
                    isFirstResume = false
                } else {
                    viewModel.refresh(silent = true)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val mapState = remember { NearbyMapState() }
    val selected = state.players.firstOrNull { it.userId == state.selectedId }
    val farthestKm = state.players.maxOfOrNull { it.distanceKm }?.toDouble() ?: 2.0
    val myLat = state.myLatitude
    val myLng = state.myLongitude

    LaunchedEffect(myLat, myLng, farthestKm, mapState.userMoved) {
        if (myLat != null && myLng != null && !mapState.userMoved) {
            mapState.fitAround(myLat, myLng, farthestKm, animated = true)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.enable() else viewModel.permissionDenied()
    }

    val turnOn = {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.enable()
        else permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    val refreshAction = onRefresh ?: turnOn

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearbyColors.Background)
    ) {
        if (myLat != null && myLng != null) {
            NearbyMap(
                state = mapState,
                myLatitude = myLat,
                myLongitude = myLng,
                players = if (state.visible) state.players else emptyList(),
                selectedId = state.selectedId,
                onSelect = viewModel::select
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NEARBY",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = White
            )

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(NearbyColors.Surface)
                    .border(1.dp, NearbyColors.Border, RoundedCornerShape(20.dp))
                    .clickable(
                        enabled = !state.loading && !state.guest,
                        onClick = singleClick {
                            if (state.visible) viewModel.disable() else turnOn()
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = if (state.visible) CyanLight else NearbyColors.TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(14.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Appear on Map",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                    Text(
                        text = if (state.visible) "On · Your approximate location is visible" else "Off · Your approximate location is hidden",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NearbyColors.TextSecondary
                    )
                }
                Spacer(Modifier.width(10.dp))
                Switch(
                    checked = state.visible,
                    enabled = !state.loading && !state.guest,
                    onCheckedChange = { checked -> if (checked) turnOn() else viewModel.disable() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NearbyColors.Background,
                        checkedTrackColor = CyanLight,
                        checkedBorderColor = Color.Transparent,
                        uncheckedThumbColor = NearbyColors.TextSecondary,
                        uncheckedTrackColor = StatsBarSlate,
                        uncheckedBorderColor = NearbyColors.Border
                    )
                )
            }

            state.error?.let { message ->
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NearbyColors.Surface)
                        .border(1.dp, NearbyColors.Border, RoundedCornerShape(16.dp))
                        .padding(start = 14.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = singleClick(refreshAction)) {
                        Text("Retry", color = NearbyColors.Accent)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SquareIconButton(
                    icon = Icons.Filled.MyLocation,
                    contentDescription = "Recenter",
                    onClick = {
                        mapState.userMoved = false
                        if (myLat != null && myLng != null) {
                            mapState.fitAround(myLat, myLng, farthestKm, animated = true)
                        }
                    }
                )
                SquareIconButton(
                    icon = Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    enabled = !state.loading,
                    iconModifier = if (state.loading) Modifier.rotate(refreshRotation) else Modifier,
                    onClick = refreshAction
                )
            }
        }

        when {
            state.guest -> MapMessage(
                text = "Create an account to find players near you",
                modifier = Modifier.align(Alignment.Center)
            )
            !state.visible && !state.loading -> MapMessage(
                text = "Turn on Appear on Map to see players around you",
                actionLabel = "Turn on",
                onAction = { turnOn() },
                modifier = Modifier.align(Alignment.Center)
            )
            state.visible && !state.loading && (myLat == null || myLng == null) -> MapMessage(
                text = "Location needed to find players near you",
                actionLabel = "Turn on",
                onAction = { turnOn() },
                modifier = Modifier.align(Alignment.Center)
            )
            state.visible && !state.loading && state.players.isEmpty() -> MapMessage(
                text = "No players nearby right now",
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (state.loading) {
            CircularProgressIndicator(
                color = NearbyColors.Accent,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        AnimatedVisibility(
            visible = selected != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            selected?.let { player ->
                key(player.userId) {
                    PlayerCard(
                        player = player,
                        onChallenge = {
                            challengingPlayer = player
                        },
                        onAddFriend = { onAddFriend(player) },
                        onChat = { onChat(player) },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        challengingPlayer?.let { player ->
            DirectChallengeDialog(
                opponentId = player.userId.toLong(),
                opponentUsername = player.username,
                opponentRating = player.rating,
                onDismiss = { challengingPlayer = null },
                onGameStarted = { gameId ->
                    challengingPlayer = null
                    onGameStarted(gameId)
                }
            )
        }
    }
}

@Composable
private fun MapMessage(
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .padding(32.dp)
            .clip(shape)
            .background(NearbyColors.Surface)
            .border(1.dp, NearbyColors.Border, shape)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = NearbyColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null) {
            TextButton(onClick = singleClick(onAction)) {
                Text(actionLabel, color = NearbyColors.Accent)
            }
        }
    }
}
