package com.abhinav.caisarra.presentation.invite

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private object InviteColors {
    val Background = Color(0xFF080D13)
    val Card = Color(0xFF111A24)
    val Border = Color(0xFF1F2B38)
    val Accent = Color(0xFF10B981)
    val TextSecondary = Color(0xFF9CA3AF)
    val Error = Color(0xFFEF4444)
}

private val TimePresets = listOf(1, 3, 5, 10, 15, 30)
private val IncrementPresets = listOf(0, 2, 3, 5, 10)
private val ColorOptions = listOf("white" to "White", "black" to "Black", "random" to "Random")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateInviteScreen(
    viewModel: CreateInviteViewModel,
    onGameReady: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InviteColors.Background)
            .systemBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "PLAY WITH A FRIEND",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (state.link == null) "Create an invite link" else "Share this link with your friend",
                    color = InviteColors.TextSecondary,
                    fontSize = 14.sp
                )
            }

            val link = state.link
            val gameId = state.gameId

            if (link == null || gameId == null) {
                InviteCard(title = "Time") {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimePresets.forEach { minutes ->
                            Chip(
                                label = "${minutes}m",
                                selected = state.timeControlMinutes == minutes,
                                onClick = { viewModel.onTimeControlChange(minutes) }
                            )
                        }
                    }
                }

                InviteCard(title = "Increment") {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IncrementPresets.forEach { seconds ->
                            Chip(
                                label = "+${seconds}s",
                                selected = state.incrementSeconds == seconds,
                                onClick = { viewModel.onIncrementChange(seconds) }
                            )
                        }
                    }
                }

                InviteCard(title = "Your color") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ColorOptions.forEach { (value, label) ->
                            Chip(
                                label = label,
                                selected = state.color == value,
                                onClick = { viewModel.onColorChange(value) }
                            )
                        }
                    }
                }

                state.error?.let { error ->
                    Text(text = error, color = InviteColors.Error, fontSize = 13.sp)
                }

                Button(
                    onClick = viewModel::createInvite,
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = InviteColors.Accent)
                ) {
                    Text(
                        text = if (state.isLoading) "CREATING..." else "CREATE INVITE",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                InviteCard(title = "Invite link") {
                    Text(
                        text = link,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    state.inviteCode?.let { code ->
                        Text(
                            text = "Code: $code",
                            color = InviteColors.TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { copyLink(context, link) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, InviteColors.Border)
                    ) {
                        Text(text = "COPY", color = Color.White)
                    }
                    OutlinedButton(
                        onClick = { shareLink(context, link) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, InviteColors.Border)
                    ) {
                        Text(text = "SHARE", color = Color.White)
                    }
                }

                Button(
                    onClick = { onGameReady(gameId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = InviteColors.Accent)
                ) {
                    Text(
                        text = "ENTER GAME ROOM",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, InviteColors.Border)
            ) {
                Text(text = "BACK", color = Color.White)
            }
        }
    }
}

private fun copyLink(context: Context, link: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("invite", link))
    Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
}

private fun shareLink(context: Context, link: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Play chess with me on Caisaara: $link")
    }
    context.startActivity(Intent.createChooser(intent, null))
}

@Composable
private fun InviteCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = InviteColors.Card,
        border = BorderStroke(1.dp, InviteColors.Border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title.uppercase(),
                color = InviteColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            content()
        }
    }
}

@Composable
private fun Chip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .defaultMinSize(minWidth = 64.dp)
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) InviteColors.Accent else Color.Transparent,
        border = if (selected) null else BorderStroke(1.dp, InviteColors.Border)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) Color.Black else Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}