package com.abhinav.caisarra.core.ui.components

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.FriendsActionRadius
import com.abhinav.caisarra.core.ui.theme.FriendsAvatarRadius
import com.caisaara.ui.theme.FriendsTabActiveBg
import com.caisaara.ui.theme.FriendsTabBg
import com.caisaara.ui.theme.FriendsTextMuted
import com.caisaara.ui.theme.FriendsTextWhite

@Composable
fun SegmentedTabs(
    titles: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FriendsAvatarRadius))
            .background(FriendsTabBg)
            .padding(4.dp)
    ) {
        titles.forEachIndexed { index, title ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(FriendsActionRadius))
                    .background(if (selected) FriendsTabActiveBg else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(title, color = if (selected) FriendsTextWhite else FriendsTextMuted, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
