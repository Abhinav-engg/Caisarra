package com.abhinav.caisarra.features.dailytactics.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.common.singleClickable
import com.caisaara.ui.theme.CardSurface
import com.caisaara.ui.theme.SilverColor
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeMedium
import com.caisaara.ui.theme.ThemeSlateAccent
import com.caisaara.ui.theme.White

private val TacticCardShape = RoundedCornerShape(16.dp)

@Composable
fun DailyTacticCard(
    tactic: DailyTactic,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(TacticCardShape)
            .background(CardSurface)
            .border(1.dp, White.copy(alpha = 0.1f), TacticCardShape)
            .singleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Image(
            painter = painterResource(tactic.iconRes),
            contentDescription = null,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = tactic.title,
            color = SilverColor,
            fontSize = TextSizeMedium,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 20.sp,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = tactic.subtitle,
            color = ThemeSlateAccent,
            fontSize = TextSizeCaption,
            lineHeight = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
