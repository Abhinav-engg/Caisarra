package com.abhinav.caisarra.presentation.invite.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.FriendsCardRadius
import com.abhinav.caisarra.core.ui.theme.FriendsScreenPadding
import com.abhinav.caisarra.presentation.invite.FriendsLeaderboardItem
import com.caisaara.ui.theme.BronzeColor
import com.caisaara.ui.theme.FriendsBg
import com.caisaara.ui.theme.FriendsCardBg
import com.caisaara.ui.theme.FriendsCardBorder
import com.caisaara.ui.theme.FriendsCyan
import com.caisaara.ui.theme.FriendsLeaderboardMeBg
import com.caisaara.ui.theme.FriendsTextMuted
import com.caisaara.ui.theme.FriendsTextWhite
import com.caisaara.ui.theme.GoldColor
import com.caisaara.ui.theme.SilverColor

@Composable
fun FriendsLeaderboardView(
    entries: List<FriendsLeaderboardItem>,
    onClose: () -> Unit,
    onPlayerClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FriendsBg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = FriendsTextWhite)
            }
            Spacer(Modifier.width(8.dp))
            Text("FRIENDS LEADERBOARD", color = FriendsTextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.EmojiEvents, null, tint = GoldColor, modifier = Modifier.padding(end = 12.dp).size(24.dp))
        }

        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No players on leaderboard yet.", color = FriendsTextMuted, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = FriendsScreenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                itemsIndexed(entries) { index, entry ->
                    LeaderboardRow(rank = index + 1, entry = entry, onClick = onPlayerClick)
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    entry: FriendsLeaderboardItem,
    onClick: (String) -> Unit = {}
) {
    val rankColor = when (rank) {
        1 -> GoldColor
        2 -> SilverColor
        3 -> BronzeColor
        else -> FriendsTextMuted
    }
    val cardShape = RoundedCornerShape(FriendsCardRadius)
    val name = entry.displayName ?: entry.username

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(if (entry.isMe) FriendsLeaderboardMeBg else FriendsCardBg)
            .border(1.dp, if (entry.isMe) FriendsCyan else FriendsCardBorder, cardShape)
            .clickable { onClick(entry.username) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(rankColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text("#$rank", color = rankColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(Modifier.width(12.dp))
        FriendAvatar(name = name, avatarUrl = entry.avatarUrl, size = 42.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, color = FriendsTextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                if (entry.isMe) {
                    Spacer(Modifier.width(6.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(FriendsCyan.copy(alpha = 0.25f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("YOU", color = FriendsCyan, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }
            Text("@${entry.username}", color = FriendsTextMuted, fontSize = 12.sp)
        }
        Spacer(Modifier.width(8.dp))
        Text("${entry.rating} Rapid", color = FriendsCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
