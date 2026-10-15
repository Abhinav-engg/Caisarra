package com.abhinav.caisarra.presentation.invite

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.presentation.game.components.setup.SetupColors
import com.abhinav.caisarra.presentation.game.components.setup.SetupHeader
import com.abhinav.caisarra.presentation.game.components.setup.SetupPage

private val InviteBorder = Color(0xFF26384D)
private val InviteIcon = Color(0xFF20324C)

@Composable
fun CreateInviteScreen(
    viewModel: CreateInviteViewModel,
    onEnterWaitingBoard: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.link) {
        state.link?.let { shareLink(context, it) }
    }

    SetupPage {
        SetupHeader("Create Invite", onBack)
        Spacer(Modifier.height(24.dp))
        Text("CHOOSE YOUR SIDE", color = SetupColors.Secondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SideOption("white", "White", state.color, viewModel::onColorChange, Modifier.weight(1f))
            SideOption("random", "Random", state.color, viewModel::onColorChange, Modifier.weight(1f))
            SideOption("black", "Black", state.color, viewModel::onColorChange, Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SetupColors.Card, RoundedCornerShape(18.dp))
                .border(1.dp, InviteBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp)
        ) {
            InviteSwitchRow(
                title = "Allow Takebacks",
                subtitle = if (state.allowTakebacks) {
                    "This friendly game will be unrated"
                } else {
                    "Rated by default - turn on for an unrated game"
                },
                icon = Icons.AutoMirrored.Filled.Undo,
                checked = state.allowTakebacks,
                onCheckedChange = viewModel::onTakebacksChanged
            )
            Box(Modifier.fillMaxWidth().height(1.dp).background(InviteBorder))
            InviteSwitchRow(
                "Public Spectators",
                "Friends can watch match live",
                Icons.Default.RemoveRedEye,
                true,
                onCheckedChange = {}
            )
        }
        state.error?.let {
            Spacer(Modifier.height(14.dp))
            Text(it, color = Color(0xFFEF4444), fontSize = 13.sp)
        }
        Spacer(Modifier.weight(1f))
        GeneralButton(
            text = if (state.isLoading) "CREATING..." else "CREATE INVITE",
            onClick = viewModel::createInvite,
            enabled = !state.isLoading
        )
        state.link?.let { link ->
            Spacer(Modifier.height(16.dp))
            InviteLink(link, context)
            Spacer(Modifier.height(16.dp))
            GeneralButton(
                text = "ENTER WAITING BOARD",
                onClick = { state.gameId?.let(onEnterWaitingBoard) }
            )
        }
    }
}

@Composable
private fun SideOption(
    value: String,
    label: String,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val active = value == selected
    Column(
        modifier = modifier
            .height(100.dp)
            .background(if (active) Color(0xFF123638) else SetupColors.Card, RoundedCornerShape(16.dp))
            .border(1.dp, if (active) SetupColors.Accent else InviteBorder, RoundedCornerShape(16.dp))
            .clickable { onSelected(value) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Circle, null, tint = if (active) SetupColors.Accent else SetupColors.Secondary)
        Spacer(Modifier.height(8.dp))
        Text(label, color = SetupColors.Text, fontSize = 14.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun InviteSwitchRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.background(InviteIcon, RoundedCornerShape(8.dp)).padding(7.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = SetupColors.Accent)
        }
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(title, color = SetupColors.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = SetupColors.Secondary, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SetupColors.Accent,
                checkedTrackColor = Color(0xFF176B6B),
                uncheckedThumbColor = SetupColors.Secondary,
                uncheckedTrackColor = Color(0xFF223149)
            )
        )
    }
}

@Composable
private fun InviteLink(link: String, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SetupColors.Card, RoundedCornerShape(16.dp))
            .border(1.dp, InviteBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("INVITE LINK", color = SetupColors.Secondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(link, color = SetupColors.Text, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "COPY LINK",
            color = SetupColors.Accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { copyLink(context, link) }
        )
    }
}

private fun copyLink(context: Context, link: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("invite", link))
    Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
}

private fun shareLink(context: Context, link: String) {
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Play chess with me on Caisaara: $link")
            },
            null
        )
    )
}
