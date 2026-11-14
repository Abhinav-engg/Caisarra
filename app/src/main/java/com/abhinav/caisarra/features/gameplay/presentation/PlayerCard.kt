package com.abhinav.caisarra.features.gameplay.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.components.CountryUtils
import com.abhinav.caisarra.presentation.game.components.CapturedPieces
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.invite.components.FriendAvatar
import com.caisaara.ui.theme.StatsTextMuted

@Composable
fun PlayerCard(
    username: String,
    avatarUrl: String?,
    rating: Int?,
    modifier: Modifier = Modifier,
    countryCode: String? = null,
    capturedPieces: List<Piece> = emptyList(),
    advantage: Int = 0,
    trailing: @Composable () -> Unit = {}
) {
    val flag = remember(countryCode) { CountryUtils.flag(countryCode) }
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FriendAvatar(name = username, avatarUrl = avatarUrl, size = 46.dp, shape = RoundedCornerShape(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = username,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (flag.isNotEmpty()) Text(text = flag, fontSize = 15.sp)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rating?.let { Text(text = "$it", color = StatsTextMuted, fontSize = 13.sp) }
                if (capturedPieces.isNotEmpty() || advantage > 0) {
                    CapturedPieces(pieces = capturedPieces, advantage = advantage)
                }
            }
        }
        trailing()
    }
}
