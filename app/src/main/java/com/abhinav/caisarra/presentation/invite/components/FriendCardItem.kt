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
import com.abhinav.caisarra.core.ui.theme.FriendsActionSize
import com.abhinav.caisarra.core.ui.theme.FriendsAvatarSize
import com.abhinav.caisarra.data.remote.dto.FriendDto
import com.caisaara.ui.theme.FriendsActionBg
import com.caisaara.ui.theme.FriendsActionBorder
import com.caisaara.ui.theme.FriendsCyan
import com.caisaara.ui.theme.FriendsTextMuted
import com.caisaara.ui.theme.FriendsTextWhite

@Composable
fun FriendCardItem(
    friend: FriendDto,
    onDirectMessage: () -> Unit,
    onChallenge: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FriendAvatar(name = friend.displayName ?: friend.username, avatarUrl = friend.avatarUrl, size = FriendsAvatarSize)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(friend.displayName ?: friend.username, color = FriendsTextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("${friend.rating ?: 1200} Rapid", color = FriendsTextMuted, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        val actionShape = RoundedCornerShape(FriendsActionRadius)
        Box(
            modifier = Modifier
                .size(FriendsActionSize)
                .clip(actionShape)
                .background(FriendsActionBg)
                .border(1.dp, FriendsActionBorder, actionShape)
                .clickable(onClick = onChallenge),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SportsEsports, "Challenge", tint = FriendsCyan, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(8.dp))
        Image(
            painter = painterResource(R.drawable.icon),
            contentDescription = "Message",
            modifier = Modifier.size(FriendsActionSize).clip(actionShape).clickable(onClick = onDirectMessage)
        )
    }
}
