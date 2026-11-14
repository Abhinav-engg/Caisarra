package com.abhinav.caisarra.features.follow.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.FriendsCardRadius
import com.abhinav.caisarra.features.follow.dto.FollowUserDto
import com.abhinav.caisarra.presentation.invite.components.FriendAvatar
import com.caisaara.ui.theme.FriendsCardBg
import com.caisaara.ui.theme.FriendsCardBorder
import com.caisaara.ui.theme.FriendsTextMuted
import com.caisaara.ui.theme.FriendsTextWhite

@Composable
fun FollowUserRow(
    user: FollowUserDto,
    onClick: () -> Unit,
    onToggleFollow: (() -> Unit)?
) {
    val name = user.displayName?.takeIf { it.isNotBlank() } ?: user.username
    val shape = RoundedCornerShape(FriendsCardRadius)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(FriendsCardBg)
            .border(1.dp, FriendsCardBorder, shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FriendAvatar(name = name, avatarUrl = user.avatarUrl, size = 44.dp, username = user.username)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = FriendsTextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("@${user.username}", color = FriendsTextMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        onToggleFollow?.let { FollowButton(user.isFollowing, it, Modifier.padding(start = 8.dp)) }
    }
}
