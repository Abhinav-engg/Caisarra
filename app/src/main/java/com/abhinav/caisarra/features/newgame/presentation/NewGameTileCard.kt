package com.abhinav.caisarra.features.newgame.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeSubhead

@Composable
fun NewGameTileCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NewGameCard(onClick = onClick, modifier = modifier) {
        Column {
            Icon(icon, contentDescription = null, tint = StatsCyan, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(20.dp))
            Text(title, color = StatsTextWhite, fontSize = TextSizeSubhead, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            Text(subtitle, color = StatsTextMuted, fontSize = TextSizeCaption, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
