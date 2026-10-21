package com.abhinav.caisarra.presentation.nearby.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.presentation.common.singleClick
import com.abhinav.caisarra.presentation.nearby.NearbyColors

@Composable
fun NearbyTopBar(
    visible: Boolean,
    switchEnabled: Boolean,
    onBack: () -> Unit,
    onVisibleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NearbyColors.Surface)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = singleClick(onBack)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = NearbyColors.TextPrimary
            )
        }
        Text(
            text = "Nearby Players",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = NearbyColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(NearbyColors.SurfaceHigh)
                .border(1.dp, NearbyColors.Border, CircleShape)
                .padding(start = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Visibility,
                contentDescription = null,
                tint = NearbyColors.Accent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Appear on Map",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NearbyColors.TextPrimary
            )
            Switch(
                checked = visible,
                enabled = switchEnabled,
                onCheckedChange = onVisibleChange,
                modifier = Modifier.scale(0.75f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NearbyColors.Background,
                    checkedTrackColor = NearbyColors.Accent,
                    uncheckedThumbColor = NearbyColors.TextSecondary,
                    uncheckedTrackColor = NearbyColors.Surface,
                    uncheckedBorderColor = NearbyColors.Border
                )
            )
        }
    }
}
