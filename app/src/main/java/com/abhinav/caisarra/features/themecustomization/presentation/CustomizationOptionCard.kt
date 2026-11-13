package com.abhinav.caisarra.features.themecustomization.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.theme.StatsCardCornerRadius
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeSubhead

@Composable
fun CustomizationOptionCard(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconBorder: Color = StatsCardBorder,
    footer: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(StatsCardCornerRadius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(StatsCardBg)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) StatsCyan else StatsCardBorder, shape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .background(StatsBg, RoundedCornerShape(10.dp))
                    .border(1.dp, iconBorder, RoundedCornerShape(10.dp))
                    .padding(4.dp)
            )
            Icon(
                imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                contentDescription = null,
                tint = if (selected) StatsCyan else StatsTextMuted,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(title, color = StatsTextWhite, fontSize = TextSizeSubhead, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(description, color = StatsTextMuted, fontSize = TextSizeCaption, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(12.dp))
        footer()
    }
}
