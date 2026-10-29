package com.abhinav.caisarra.presentation.invite.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.FriendsActionRadius
import com.abhinav.caisarra.core.ui.theme.FriendsAvatarSize
import com.abhinav.caisarra.core.ui.theme.FriendsButtonHeight
import com.abhinav.caisarra.core.ui.theme.FriendsCardRadius
import com.abhinav.caisarra.core.ui.theme.FriendsScreenPadding
import com.abhinav.caisarra.data.remote.dto.FriendRequestDto
import com.caisaara.ui.theme.Black
import com.caisaara.ui.theme.FriendsButtonDark
import com.caisaara.ui.theme.FriendsButtonDarkBorder
import com.caisaara.ui.theme.FriendsCardBg
import com.caisaara.ui.theme.FriendsCardBorder
import com.caisaara.ui.theme.FriendsCyan
import com.caisaara.ui.theme.FriendsTextMuted
import com.caisaara.ui.theme.FriendsTextWhite

@Composable
fun FriendRequestCardItem(
    request: FriendRequestDto,
    onProfileClick: () -> Unit = {},
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = request.senderDisplayName ?: request.senderUsername ?: "Player ${request.senderId ?: ""}"
    val username = request.senderUsername ?: "user"
    val rating = request.senderRating ?: 1200
    val cardShape = RoundedCornerShape(FriendsCardRadius)
    val buttonShape = RoundedCornerShape(FriendsActionRadius)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(FriendsCardBg)
            .border(1.dp, FriendsCardBorder, cardShape)
            .padding(FriendsScreenPadding)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Friend request", color = FriendsTextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text("NEW", color = FriendsCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onProfileClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FriendAvatar(name = name, avatarUrl = request.senderAvatarUrl, size = FriendsAvatarSize)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = FriendsTextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("$username · $rating Rapid", color = FriendsTextMuted, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(FriendsScreenPadding))
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(FriendsButtonHeight)
                    .clip(buttonShape)
                    .background(FriendsCyan)
                    .clickable(onClick = onAccept),
                contentAlignment = Alignment.Center
            ) {
                Text("Accept", color = Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(FriendsButtonHeight)
                    .clip(buttonShape)
                    .background(FriendsButtonDark)
                    .border(1.dp, FriendsButtonDarkBorder, buttonShape)
                    .clickable(onClick = onDecline),
                contentAlignment = Alignment.Center
            ) {
                Text("Decline", color = FriendsTextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
