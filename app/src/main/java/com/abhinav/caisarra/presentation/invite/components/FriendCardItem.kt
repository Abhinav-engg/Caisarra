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
import androidx.compose.material.icons.filled.PersonRemove
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
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
    onProfileClick: () -> Unit = {},
    onDirectMessage: () -> Unit,
    onChallenge: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = friend.displayName ?: friend.username
    val isOnline = friend.isOnline

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 14.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    onClick = onProfileClick
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FriendAvatar(
                name = displayName,
                avatarUrl = friend.avatarUrl,
                size = FriendsAvatarSize,
                isOnline = isOnline
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayName,
                    color = FriendsTextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${friend.rating ?: 1200} Rapid",
                        color = FriendsTextMuted,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "•",
                        color = FriendsTextMuted.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOnline) {
                                    Color(0xFF22C55E)
                                } else {
                                    Color(0xFF64748B)
                                }
                            )
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = if (isOnline) "Online" else "Offline",
                        color = if (isOnline) {
                            Color(0xFF34D399)
                        } else {
                            FriendsTextMuted
                        },
                        fontSize = 12.sp,
                        fontWeight = if (isOnline) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        val actionShape = RoundedCornerShape(FriendsActionRadius)

        Box(
            modifier = Modifier
                .size(FriendsActionSize)
                .clip(actionShape)
                .background(FriendsActionBg)
                .border(
                    width = 1.dp,
                    color = FriendsActionBorder,
                    shape = actionShape
                )
                .clickable(
                    onClick = onChallenge
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = "Challenge",
                tint = FriendsCyan,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Image(
            painter = painterResource(R.drawable.icon),
            contentDescription = "Message",
            modifier = Modifier
                .size(FriendsActionSize)
                .clip(actionShape)
                .clickable(
                    onClick = onDirectMessage
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(FriendsActionSize)
                .clip(actionShape)
                .background(FriendsActionBg)
                .border(
                    width = 1.dp,
                    color = FriendsActionBorder,
                    shape = actionShape
                )
                .clickable(
                    onClick = onRemove
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonRemove,
                contentDescription = "Remove friend",
                tint = Color(0xFFF87171),
                modifier = Modifier.size(19.dp)
            )
        }
    }
}
