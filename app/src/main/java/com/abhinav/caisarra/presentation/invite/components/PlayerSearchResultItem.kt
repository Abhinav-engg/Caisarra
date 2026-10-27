package com.abhinav.caisarra.presentation.invite.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.theme.FriendsActionRadius
import com.abhinav.caisarra.core.ui.theme.FriendsCardRadius
import com.abhinav.caisarra.data.remote.dto.PlayerSearchDto
import com.caisaara.ui.theme.Black
import com.caisaara.ui.theme.FriendsActionBg
import com.caisaara.ui.theme.FriendsActionBorder
import com.caisaara.ui.theme.FriendsButtonDark
import com.caisaara.ui.theme.FriendsButtonDarkBorder
import com.caisaara.ui.theme.FriendsCardBg
import com.caisaara.ui.theme.FriendsCardBorder
import com.caisaara.ui.theme.FriendsCyan
import com.caisaara.ui.theme.FriendsTextMuted
import com.caisaara.ui.theme.FriendsTextWhite

@Composable
fun PlayerSearchResultItem(
    player: PlayerSearchDto,
    status: String?,
    isBusy: Boolean,
    onDirectMessage: () -> Unit,
    onChallenge: () -> Unit,
    onAdd: () -> Unit,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = player.displayName ?: player.username
    val rating = player.rating ?: 1200
    val cardShape = RoundedCornerShape(FriendsCardRadius)
    val actionShape = RoundedCornerShape(FriendsActionRadius)
    val btnShape = RoundedCornerShape(8.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(FriendsCardBg)
            .border(1.dp, FriendsCardBorder, cardShape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FriendAvatar(name = name, avatarUrl = player.avatarUrl, size = 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = FriendsTextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("@${player.username} · $rating Rapid", color = FriendsTextMuted, fontSize = 12.sp)
        }
        Spacer(Modifier.width(8.dp))

        when (status) {
            "friends" -> {
                Row {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(actionShape)
                            .background(FriendsActionBg)
                            .border(1.dp, FriendsActionBorder, actionShape)
                            .clickable(onClick = onChallenge),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SportsEsports, "Challenge", tint = FriendsCyan, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Image(
                        painter = painterResource(R.drawable.icon),
                        contentDescription = "Message",
                        modifier = Modifier.size(36.dp).clip(actionShape).clickable(onClick = onDirectMessage)
                    )
                }
            }
            "request_sent" -> {
                Box(
                    modifier = Modifier.clip(btnShape).background(FriendsButtonDark).clickable(enabled = !isBusy, onClick = onCancel).padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancel", color = FriendsTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            "request_received" -> {
                Row {
                    Box(
                        modifier = Modifier.clip(btnShape).background(FriendsCyan).clickable(enabled = !isBusy, onClick = onAccept).padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Accept", color = Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier.clip(btnShape).background(FriendsButtonDark).border(1.dp, FriendsButtonDarkBorder, btnShape).clickable(enabled = !isBusy, onClick = onReject).padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Decline", color = FriendsTextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            "self" -> Text("You", color = FriendsTextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            else -> {
                Box(
                    modifier = Modifier.clip(btnShape).background(FriendsCyan).clickable(enabled = !isBusy, onClick = onAdd).padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonAdd, "Add", tint = Black, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add", color = Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
