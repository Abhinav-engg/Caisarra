package com.abhinav.caisarra.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caisaara.ui.theme.CardSurface
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite

@Composable
fun EyebrowCard(
    eyebrow: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardSurface)
            .glassBorder(shape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(eyebrow, color = StatsCyan, fontSize = 12.sp, letterSpacing = 1.sp)
        Text(title, color = StatsTextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = StatsTextMuted, fontSize = 14.sp)
    }
}
