package com.abhinav.caisarra.presentation.tournament

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TournamentFilterBar(
    resultCount: Int,
    sort: TournamentSort,
    activeFilterCount: Int,
    onSortChange: (TournamentSort) -> Unit,
    onOpenFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sortMenuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "OPEN TOURNAMENTS · $resultCount",
            color = TournamentColors.Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Box {
            BarButton(
                icon = Icons.AutoMirrored.Filled.Sort,
                label = "Sort",
                highlighted = sort != TournamentSort.Newest,
                onClick = { sortMenuOpen = true }
            )
            DropdownMenu(
                expanded = sortMenuOpen,
                onDismissRequest = { sortMenuOpen = false },
                containerColor = TournamentColors.Card
            ) {
                TournamentSort.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                option.label,
                                color = if (option == sort) TournamentColors.Accent else Color.White,
                                fontSize = 14.sp
                            )
                        },
                        trailingIcon = {
                            if (option == sort) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = TournamentColors.Accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        onClick = {
                            sortMenuOpen = false
                            onSortChange(option)
                        }
                    )
                }
            }
        }
        BarButton(
            icon = Icons.Default.FilterList,
            label = "Filter",
            highlighted = activeFilterCount > 0,
            badge = activeFilterCount.takeIf { it > 0 },
            onClick = onOpenFilters
        )
    }
}

@Composable
private fun BarButton(
    icon: ImageVector,
    label: String,
    highlighted: Boolean,
    onClick: () -> Unit,
    badge: Int? = null
) {
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (highlighted) Color(0xFF14202D) else TournamentColors.Card)
            .border(
                1.dp,
                if (highlighted) TournamentColors.ActiveBorder else TournamentColors.CardBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (highlighted) TournamentColors.Accent else TournamentColors.Muted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White, fontSize = 13.sp)
        if (badge != null) {
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(TournamentColors.Accent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    badge.toString(),
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
