package com.abhinav.caisarra.presentation.nearby.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.data.remote.dto.NearbyPlayer
import com.abhinav.caisarra.presentation.nearby.NearbyColors
import kotlin.math.roundToInt

fun Modifier.anchoredAt(position: Offset, anchorY: Float): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        layout(placeable.width, placeable.height) {
            placeable.place(
                (position.x - placeable.width / 2f).roundToInt(),
                (position.y - anchorY).roundToInt()
            )
        }
    }

@Composable
fun RatingChip(text: String, highlighted: Boolean, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Text(
        text = text,
        modifier = modifier
            .clip(shape)
            .background(NearbyColors.Surface)
            .border(1.dp, if (highlighted) NearbyColors.Accent else NearbyColors.Border, shape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = if (highlighted) NearbyColors.Accent else NearbyColors.TextPrimary
    )
}

@Composable
fun PlayerMarker(
    player: NearbyPlayer,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarSize = if (selected) 56.dp else 44.dp
    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(avatarSize)
                .then(
                    if (selected) {
                        Modifier.shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            clip = false,
                            ambientColor = NearbyColors.Accent,
                            spotColor = NearbyColors.Accent
                        )
                    } else {
                        Modifier
                    }
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (selected) 1f else 0.85f)
                    .clip(CircleShape)
                    .background(NearbyColors.SurfaceHigh)
                    .border(
                        2.dp,
                        if (selected) NearbyColors.Accent else NearbyColors.Border,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.username.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NearbyColors.TextPrimary
                )
            }
            if (player.active) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(NearbyColors.Online)
                        .border(2.dp, NearbyColors.Background, CircleShape)
                )
            }
        }
        RatingChip(
            text = if (selected) "${player.rating} ELO" else "${player.rating}",
            highlighted = selected
        )
    }
}

@Composable
fun ClusterMarker(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(NearbyColors.Surface)
                .border(2.dp, NearbyColors.Accent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NearbyColors.Accent
            )
        }
        RatingChip(text = "$count players", highlighted = false)
    }
}

@Composable
fun YouMarker(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(NearbyColors.SurfaceHigh)
                .border(2.dp, NearbyColors.Accent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "You",
                tint = NearbyColors.Accent
            )
        }
        val shape = RoundedCornerShape(10.dp)
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = NearbyColors.Accent, fontWeight = FontWeight.Bold)) {
                    append("You")
                }
                withStyle(SpanStyle(color = NearbyColors.TextSecondary)) {
                    append(" (~1 km)")
                }
            },
            modifier = Modifier
                .clip(shape)
                .background(NearbyColors.Surface)
                .border(1.dp, NearbyColors.Border, shape)
                .padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun RingLabel(text: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(10.dp)
    Text(
        text = text,
        modifier = modifier
            .clip(shape)
            .background(NearbyColors.Surface)
            .border(1.dp, NearbyColors.Border, shape)
            .padding(horizontal = 10.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        color = NearbyColors.TextSecondary
    )
}