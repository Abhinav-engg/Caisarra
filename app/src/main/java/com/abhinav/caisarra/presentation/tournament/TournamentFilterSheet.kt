@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.abhinav.caisarra.presentation.tournament

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.data.remote.dto.TournamentDto
import kotlin.math.roundToInt

@Composable
fun TournamentFilterSheet(
    tournaments: List<TournamentDto>,
    initial: TournamentFilters,
    sort: TournamentSort,
    onApply: (TournamentFilters) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember { mutableStateOf(initial) }
    val joinedBounds = remember(tournaments) { 0..maxOf(2, tournaments.maxOfOrNull { it.players ?: 0 } ?: 0) }
    val capacityBounds = remember(tournaments) {
        val min = tournaments.minOfOrNull { it.maxPlayers } ?: 2
        val max = tournaments.maxOfOrNull { it.maxPlayers } ?: 2
        min.coerceAtMost(max - 1).coerceAtLeast(0)..max
    }
    val matches = remember(draft, tournaments) { tournaments.applyFiltersAndSort(draft, sort).size }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = TournamentColors.Card,
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filter tournaments",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { draft = TournamentFilters() },
                    enabled = draft.activeCount > 0
                ) {
                    Text(
                        "Reset",
                        color = if (draft.activeCount > 0) TournamentColors.Accent else TournamentColors.Subtitle
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                SectionTitle("Status")
                ChipGroup(
                    options = TournamentStatusFilter.entries,
                    selected = draft.statuses,
                    label = { it.label },
                    onToggle = { draft = draft.copy(statuses = draft.statuses.toggle(it)) }
                )

                SectionTitle("Format")
                ChipGroup(
                    options = TournamentFormatFilter.entries,
                    selected = draft.formats,
                    label = { it.label },
                    onToggle = { draft = draft.copy(formats = draft.formats.toggle(it)) }
                )

                SectionTitle("Speed")
                ChipGroup(
                    options = TournamentSpeed.entries,
                    selected = draft.speeds,
                    label = { it.label },
                    onToggle = { draft = draft.copy(speeds = draft.speeds.toggle(it)) }
                )

                SectionTitle("Rating requirement")
                ChipGroup(
                    options = TournamentRatingFilter.entries,
                    selected = setOf(draft.rating),
                    label = { it.label },
                    onToggle = { draft = draft.copy(rating = it) }
                )

                PlayerRangeSection(
                    title = "Players joined",
                    bounds = joinedBounds,
                    value = draft.joinedRange,
                    onChange = { draft = draft.copy(joinedRange = it) }
                )

                if (capacityBounds.first < capacityBounds.last) {
                    PlayerRangeSection(
                        title = "Max players (capacity)",
                        bounds = capacityBounds,
                        value = draft.capacityRange,
                        onChange = { draft = draft.copy(capacityRange = it) }
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Open spots only", color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                        Text("Hide tournaments that are already full", color = TournamentColors.Muted, fontSize = 12.5.sp)
                    }
                    Switch(
                        checked = draft.openSpotsOnly,
                        onCheckedChange = { draft = draft.copy(openSpotsOnly = it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = TournamentColors.Accent,
                            uncheckedThumbColor = TournamentColors.Muted,
                            uncheckedTrackColor = TournamentColors.InputBackground,
                            uncheckedBorderColor = TournamentColors.InputBorder
                        )
                    )
                }
                Spacer(Modifier.height(20.dp))
            }

            TournamentActionButton(
                text = if (matches == 1) "Show 1 tournament" else "Show $matches tournaments",
                onClick = { onApply(draft) }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(16.dp))
    Text(text, color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun <T> ChipGroup(
    options: List<T>,
    selected: Set<T>,
    label: (T) -> String,
    onToggle: (T) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            TournamentFilterChip(
                text = label(option),
                selected = option in selected,
                onClick = { onToggle(option) }
            )
        }
    }
}

@Composable
private fun PlayerRangeSection(
    title: String,
    bounds: IntRange,
    value: IntRange?,
    onChange: (IntRange?) -> Unit
) {
    val current = value
        ?.let { it.first.coerceIn(bounds)..it.last.coerceIn(bounds) }
        ?.takeIf { it.first <= it.last }
        ?: bounds
    Spacer(Modifier.height(16.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(
            if (value == null) "Any" else "${current.first} – ${current.last}",
            color = if (value == null) TournamentColors.Muted else TournamentColors.Accent,
            fontSize = 13.sp
        )
    }
    val span = bounds.last - bounds.first
    RangeSlider(
        value = current.first.toFloat()..current.last.toFloat(),
        onValueChange = { range ->
            val next = range.start.roundToInt()..range.endInclusive.roundToInt()
            onChange(if (next == bounds) null else next)
        },
        valueRange = bounds.first.toFloat()..bounds.last.toFloat(),
        steps = if (span in 2..40) span - 1 else 0,
        colors = SliderDefaults.colors(
            thumbColor = TournamentColors.Accent,
            activeTrackColor = TournamentColors.Accent,
            inactiveTrackColor = TournamentColors.InputBorder,
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent
        )
    )
}

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
