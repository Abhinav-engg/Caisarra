package com.abhinav.caisarra.presentation.invite.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.FriendsCardRadius
import com.abhinav.caisarra.core.ui.theme.FriendsScreenPadding
import com.caisaara.ui.theme.FriendsCardBg
import com.caisaara.ui.theme.FriendsCardBorder
import com.caisaara.ui.theme.FriendsCyan
import com.caisaara.ui.theme.FriendsTextMuted
import com.caisaara.ui.theme.FriendsTextWhite

@Composable
fun FriendsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(FriendsCardRadius)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(shape)
            .background(FriendsCardBg)
            .border(1.dp, FriendsCardBorder, shape)
            .padding(horizontal = FriendsScreenPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) Text("Search...", color = FriendsTextMuted, fontSize = 15.sp)
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(color = FriendsTextWhite, fontSize = 15.sp),
                    cursorBrush = SolidColor(FriendsCyan),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(8.dp))
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "Clear", tint = FriendsTextMuted, modifier = Modifier.size(18.dp))
                }
            } else {
                Icon(Icons.Default.Search, "Search", tint = FriendsTextMuted, modifier = Modifier.size(20.dp))
            }
        }
    }
}
