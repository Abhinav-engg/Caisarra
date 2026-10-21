package com.abhinav.caisarra.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.abhinav.caisarra.data.remote.dto.NearbyPlayer
import com.abhinav.caisarra.presentation.nearby.NearbyColors
import com.abhinav.caisarra.presentation.nearby.NearbyMapState
import com.abhinav.caisarra.presentation.nearby.NearbyViewModel
import com.abhinav.caisarra.presentation.nearby.components.NearbyMap
import com.abhinav.caisarra.presentation.nearby.components.NearbyTopBar
import com.abhinav.caisarra.presentation.nearby.components.PlayerCard
import com.abhinav.caisarra.presentation.nearby.components.SquareIconButton
import kotlinx.coroutines.launch

@Composable
fun NearbyScreen(
    viewModel: NearbyViewModel,
    onBack: () -> Unit = {},
    onChallenge: (NearbyPlayer) -> Unit = {},
    onAddFriend: (NearbyPlayer) -> Unit = {},
    onChat: (NearbyPlayer) -> Unit = {},
    onFilter: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapState = remember { NearbyMapState() }
    val selected = state.players.firstOrNull { it.userId == state.selectedId }
    val farthestKm = state.players.maxOfOrNull { it.distanceKm }?.toFloat() ?: 3f

    LaunchedEffect(mapState.size, farthestKm) {
        mapState.fitIfIdle(farthestKm)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearbyColors.Background)
    ) {
        NearbyMap(
            state = mapState,
            players = if (state.visible) state.players else emptyList(),
            selectedId = state.selectedId,
            onSelect = viewModel::select
        )

        NearbyTopBar(
            visible = state.visible,
            switchEnabled = !state.loading && !state.guest,
            onBack = onBack,
            onVisibleChange = { checked -> if (checked) turnOn() else viewModel.disable() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 80.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SquareIconButton(
                icon = Icons.Filled.MyLocation,
                contentDescription = "Recenter",
                onClick = { scope.launch { mapState.recenter(farthestKm) } }
            )
            SquareIconButton(
                icon = Icons.Filled.Tune,
                contentDescription = "Filter",
                onClick = onFilter
            )
        }

        state.error?.let { message ->
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(top = 80.dp, start = 16.dp, end = 84.dp)
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
                TextButton(onClick = viewModel::loadPlayers) {
                    Text("Retry", color = NearbyColors.Accent)
                }
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
                PlayerCard(
                    player = player,
                    onChallenge = { onChallenge(player) },
                    onAddFriend = { onAddFriend(player) },
                    onChat = { onChat(player) },
                    modifier = Modifier.padding(16.dp)
                )
            }
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
            TextButton(onClick = onAction) {
                Text(actionLabel, color = NearbyColors.Accent)
            }
        }
    }
}