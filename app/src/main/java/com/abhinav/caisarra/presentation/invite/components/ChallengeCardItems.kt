package com.abhinav.caisarra.presentation.invite.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.FriendsActionRadius
import com.abhinav.caisarra.core.ui.theme.FriendsCardRadius
import com.abhinav.caisarra.data.remote.dto.ChallengeDto
import com.caisaara.ui.theme.Black
import com.caisaara.ui.theme.FriendsButtonDark
import com.caisaara.ui.theme.FriendsButtonDarkBorder
import com.caisaara.ui.theme.FriendsCardBg
import com.caisaara.ui.theme.FriendsCardBorder
import com.caisaara.ui.theme.FriendsCyan
import com.caisaara.ui.theme.FriendsTextMuted
import com.caisaara.ui.theme.FriendsTextWhite

@Composable
fun ReceivedChallengeCardItem(
    challenge: ChallengeDto,
    isBusy: Boolean = false,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(FriendsCardRadius)
    val btnShape = RoundedCornerShape(8.dp)
    val timeLabel = "${challenge.timeControlMinutes}+${challenge.incrementSeconds} · ${if (challenge.rated) "Rated" else "Casual"}"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(FriendsCardBg)
            .border(1.dp, FriendsCardBorder, cardShape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onProfileClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FriendAvatar(name = challenge.challengerUsername, avatarUrl = null, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = challenge.challengerUsername,
                    color = FriendsTextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Challenged you · $timeLabel",
                    color = FriendsCyan,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(btnShape)
                    .background(FriendsCyan)
                    .clickable(enabled = !isBusy, onClick = onAccept)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isBusy) {
                    CircularProgressIndicator(color = Black, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                } else {
                    Text("Accept", color = Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(btnShape)
                    .background(FriendsButtonDark)
                    .border(1.dp, FriendsButtonDarkBorder, btnShape)
                    .clickable(enabled = !isBusy, onClick = onDecline)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Decline", color = FriendsTextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SentChallengeCardItem(
    challenge: ChallengeDto,
    isBusy: Boolean = false,
    onEnterGame: () -> Unit = {},
    onCancel: () -> Unit,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(FriendsCardRadius)
    val btnShape = RoundedCornerShape(8.dp)
    val timeLabel = "${challenge.timeControlMinutes}+${challenge.incrementSeconds} · ${if (challenge.rated) "Rated" else "Casual"}"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(FriendsCardBg)
            .border(1.dp, FriendsCardBorder, cardShape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onProfileClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FriendAvatar(name = challenge.opponentUsername, avatarUrl = null, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = challenge.opponentUsername,
                    color = FriendsTextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Waiting for response · $timeLabel",
                    color = FriendsTextMuted,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(btnShape)
                    .background(FriendsCyan)
                    .clickable(enabled = !isBusy, onClick = onEnterGame)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Board", color = Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(btnShape)
                    .background(FriendsButtonDark)
                    .border(1.dp, FriendsButtonDarkBorder, btnShape)
                    .clickable(enabled = !isBusy, onClick = onCancel)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Cancel", color = FriendsTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
