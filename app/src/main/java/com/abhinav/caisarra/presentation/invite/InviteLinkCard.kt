package com.abhinav.caisarra.presentation.invite

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.components.SettingsCard
import com.abhinav.caisarra.core.ui.components.SettingsSectionHeader
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeBody

@Composable
fun InviteLinkCard(
    title: String,
    link: String,
    onShowQr: () -> Unit
) {
    val context = LocalContext.current
    SettingsCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SettingsSectionHeader(title)
            Text(link, color = StatsTextWhite, fontSize = TextSizeBody)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                listOf("COPY LINK" to { copyLink(context, link) }, "SHOW QR" to onShowQr).forEach { (label, action) ->
                    Text(
                        label,
                        color = StatsCyan,
                        fontSize = TextSizeBadge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = action)
                    )
                }
            }
        }
    }
}

private fun copyLink(context: Context, link: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("invite", link))
    Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
}
