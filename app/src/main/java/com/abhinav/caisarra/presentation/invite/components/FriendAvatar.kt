package com.abhinav.caisarra.presentation.invite.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.abhinav.caisarra.core.ui.theme.FriendsAvatarRadius
import com.abhinav.caisarra.core.ui.theme.FriendsAvatarSize
import com.caisaara.ui.theme.FriendsAvatarBg
import com.caisaara.ui.theme.FriendsCardBg
import com.caisaara.ui.theme.FriendsTextWhite

@Composable
fun FriendAvatar(
    name: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = FriendsAvatarSize,
    isOnline: Boolean? = null
) {
    val shape = RoundedCornerShape(FriendsAvatarRadius)

    Box(
        modifier = modifier.size(size)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(FriendsAvatarBg),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(size)
                        .clip(shape)
                )
            } else {
                Text(
                    text = getInitials(name),
                    color = FriendsTextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (size < FriendsAvatarSize) 12.sp else 15.sp
                )
            }
        }

        if (isOnline != null) {
            val badgeSize = (size * 0.28f).coerceIn(10.dp, 14.dp)
            val badgeColor = if (isOnline) {
                Color(0xFF22C55E)
            } else {
                Color(0xFF64748B)
            }

            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
                    .border(
                        width = 2.dp,
                        color = FriendsCardBg,
                        shape = CircleShape
                    )
            )
        }
    }
}

private fun getInitials(name: String): String {
    val clean = name.trim()
    if (clean.isEmpty()) return "?"
    val parts = clean.split(" ").filter { it.isNotBlank() }
    return if (parts.size >= 2) {
        "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
    } else {
        clean.take(2).uppercase()
    }
}
