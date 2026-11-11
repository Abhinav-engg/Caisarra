package com.abhinav.caisarra.presentation.invite

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.components.PreferenceToggleRow
import com.abhinav.caisarra.core.ui.components.SelectableChip
import com.abhinav.caisarra.core.ui.components.SettingsCard
import com.abhinav.caisarra.core.ui.components.SettingsNavCard
import com.abhinav.caisarra.core.ui.components.SettingsSectionHeader
import com.abhinav.caisarra.presentation.game.components.setup.SetupPage
import com.abhinav.caisarra.presentation.game.screens.TimeControlsScreen
import com.abhinav.caisarra.presentation.game.viewmodel.GameSetupEvent
import com.abhinav.caisarra.presentation.game.viewmodel.GameSetupUiState
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeLarge

@Composable
fun CreateInviteScreen(
    viewModel: CreateInviteViewModel,
    onEnterWaitingBoard: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var qrLink by remember { mutableStateOf<String?>(null) }
    var showTimeControls by remember { mutableStateOf(false) }

    LaunchedEffect(state.link) {
        state.link?.let { shareLink(context, it) }
    }

    if (showTimeControls) {
        BackHandler { showTimeControls = false }
        TimeControlsScreen(
            state = GameSetupUiState(
                minutes = state.timeControlMinutes.toFloat(),
                bonusSeconds = state.incrementSeconds.toFloat(),
                selectedControl = state.selectedControl,
                mode = state.mode
            ),
            onEvent = { event ->
                when (event) {
                    is GameSetupEvent.ControlSelected -> viewModel.onControlSelected(
                        event.label, event.minutes, event.incrementSeconds, event.mode
                    )
                    is GameSetupEvent.TimeChanged -> viewModel.onTimeChanged(event.minutes)
                    is GameSetupEvent.BonusChanged -> viewModel.onBonusChanged(event.seconds)
                }
            },
            onConfirm = { showTimeControls = false },
            onBack = { showTimeControls = false }
        )
    } else {
        SetupPage {
            CaisaaraTopBar(
                title = state.opponentUsername?.let { "CHALLENGE ${it.uppercase()}" } ?: "CREATE INVITE",
                onBack = onBack
            )
            Spacer(Modifier.height(32.dp))
            Text("Bring your people.", color = StatsTextWhite, fontSize = TextSizeLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Search for a username to find your next rival.", color = StatsTextMuted, fontSize = TextSizeBody)
            Spacer(Modifier.height(24.dp))
            SettingsNavCard(
                icon = Icons.Default.Schedule,
                title = "${state.mode.label} · ${state.selectedControl}",
                subtitle = "${state.selectedControl.substringBefore(" +")} per player · " +
                    if (state.incrementSeconds == 0) "No increment" else "${state.incrementSeconds}s increment",
                onClick = { showTimeControls = true }
            )
            Spacer(Modifier.height(24.dp))
            SettingsSectionHeader("PLAY AS")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("white" to "White", "random" to "Random", "black" to "Black").forEach { (value, label) ->
                    SelectableChip(label, state.color == value, { viewModel.onColorChange(value) }, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(24.dp))
            SettingsCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                Column {
                    PreferenceToggleRow(
                        title = "Allow takebacks",
                        subtitle = "Friendly practice · Makes this game unrated",
                        checked = state.allowTakebacks,
                        onCheckedChange = viewModel::onTakebacksChanged
                    )
                    HorizontalDivider(color = StatsCardBorder)
                    PreferenceToggleRow(
                        title = "Public spectators",
                        subtitle = "Anyone with the link can watch",
                        checked = state.publicSpectators,
                        onCheckedChange = viewModel::onPublicSpectatorsChanged
                    )
                }
            }
            state.error?.let {
                Spacer(Modifier.height(14.dp))
                Text(it, color = Color(0xFFEF4444), fontSize = TextSizeBadge)
            }
            Spacer(Modifier.height(40.dp))
            GeneralButton(
                text = when {
                    state.isLoading -> "Creating..."
                    state.opponentUsername != null -> "Challenge ${state.opponentUsername}"
                    else -> "Create Invite"
                },
                onClick = viewModel::createInvite,
                enabled = !state.isLoading
            )
            state.link?.let { link ->
                Spacer(Modifier.height(16.dp))
                InviteLinkCard("INVITE LINK", link) { qrLink = link }
                state.watchLink?.let { watchLink ->
                    Spacer(Modifier.height(12.dp))
                    InviteLinkCard("SPECTATOR LINK", watchLink) { qrLink = watchLink }
                }
                Spacer(Modifier.height(16.dp))
                GeneralButton(
                    text = "Enter Waiting Board",
                    onClick = { state.gameId?.let(onEnterWaitingBoard) }
                )
            }
            qrLink?.let { InviteQrDialog(link = it, onDismiss = { qrLink = null }) }
        }
    }
}

private fun shareLink(context: Context, link: String) {
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Play chess with me on Mosaic: $link")
            },
            null
        )
    )
}
