package com.abhinav.caisarra.presentation.nearby.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.data.remote.dto.NearbyPlayer
import com.abhinav.caisarra.presentation.common.singleClick
import com.abhinav.caisarra.presentation.nearby.NearbyColors

@Composable
fun PlayerCard(
    player: NearbyPlayer,
    onChallenge: () -> Unit,
    onAddFriend: () -> Unit,
    onChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(24.dp)
    val avatarShape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(NearbyColors.Surface)
            .border(1.dp, NearbyColors.Border, cardShape)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(NearbyColors.Border)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(avatarShape)
                    .background(NearbyColors.SurfaceHigh)
                    .border(1.dp, NearbyColors.Accent.copy(alpha = 0.5f), avatarShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.username.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = NearbyColors.Accent
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = player.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NearbyColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    RatingChip(text = "${player.rating}", highlighted = true)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.NearMe,
                        contentDescription = null,
                        tint = NearbyColors.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${player.distanceKm} km away",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NearbyColors.TextSecondary
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(NearbyColors.AccentDark, NearbyColors.Accent)
                        )
                    )
                    .clickable(
                        onClick = singleClick(onChallenge),
                        role = Role.Button
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SportsEsports,
                    contentDescription = null,
                    tint = NearbyColors.OnAccent
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Challenge",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NearbyColors.OnAccent
                )
            }
            SquareIconButton(Icons.Filled.PersonAdd, "Add friend", onAddFriend)
        }
    }
}

@Composable
fun SquareIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(shape)
            .background(NearbyColors.SurfaceHigh)
            .border(1.dp, NearbyColors.Border, shape)
            .clickable(
                onClick = singleClick(onClick),
                role = Role.Button,
                onClickLabel = contentDescription
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = NearbyColors.TextPrimary)
    }
}
