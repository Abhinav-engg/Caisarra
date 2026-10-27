package com.abhinav.caisarra.presentation.invite.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.abhinav.caisarra.core.ui.theme.FriendsAvatarRadius
import com.abhinav.caisarra.presentation.invite.FriendsTab
import com.caisaara.ui.theme.FriendsTabActiveBg
import com.caisaara.ui.theme.FriendsTabBg
import com.caisaara.ui.theme.FriendsTextMuted
import com.caisaara.ui.theme.FriendsTextWhite

@Composable
fun FriendsTabSelector(
    selectedTab: FriendsTab,
    friendsCount: Int,
    requestsCount: Int,
    onTabSelected: (FriendsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FriendsAvatarRadius))
            .background(FriendsTabBg)
            .padding(4.dp)
    ) {
        TabItem("All friends · $friendsCount", selectedTab == FriendsTab.ALL_FRIENDS, { onTabSelected(FriendsTab.ALL_FRIENDS) }, Modifier.weight(1f))
        TabItem("Requests · $requestsCount", selectedTab == FriendsTab.REQUESTS, { onTabSelected(FriendsTab.REQUESTS) }, Modifier.weight(1f))
    }
}

@Composable
private fun TabItem(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(FriendsActionRadius))
            .background(if (isSelected) FriendsTabActiveBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = if (isSelected) FriendsTextWhite else FriendsTextMuted, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
    }
}
