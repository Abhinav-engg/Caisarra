package com.abhinav.caisarra.presentation.screens.components.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.core.ui.theme.StatsCardCornerRadius
import com.abhinav.caisarra.core.ui.theme.StatsCardItemSpacing
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsRed
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeTitle
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RecentGameDateFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.US)

@Composable
internal fun GuestStatsCard(
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StatsCardCornerRadius))
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, RoundedCornerShape(StatsCardCornerRadius))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(StatsCardItemSpacing)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = StatsCyan,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "Player Statistics",
                fontFamily = JetBrainsMono,
                fontSize = TextSizeTitle,
                fontWeight = FontWeight.Bold,
                color = StatsTextWhite
            )
            Text(
                text = "Detailed rating progression charts, win rates, and game histories are available for registered accounts.",
                fontFamily = JetBrainsMono,
                fontSize = TextSizeBadge,
                color = StatsTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            GeneralButton(
                text = "SIGN IN TO VIEW STATS",
                onClick = onSignIn
            )
        }
    }
}
@Composable
internal fun ErrorStateCard(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StatsCardCornerRadius))
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, RoundedCornerShape(StatsCardCornerRadius))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(StatsCardItemSpacing)
        ) {
            Text(
                text = "Unable to Load Stats",
                fontFamily = JetBrainsMono,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = StatsRed
            )
            Text(
                text = errorMessage,
                fontFamily = JetBrainsMono,
                fontSize = TextSizeBadge,
                color = StatsTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            GeneralButton(
                text = "RETRY",
                onClick = onRetry
            )
        }
    }
}
