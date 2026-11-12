package com.abhinav.caisarra.features.dailytactics.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.components.PageIndicatorDots
import com.abhinav.caisarra.presentation.common.singleClickable
import com.caisaara.ui.theme.SilverColor
import com.caisaara.ui.theme.TextSizeHeading
import kotlin.math.roundToInt

private val TacticCardSpacing = 12.dp
private val TacticCardMinWidth = 100.dp
private val TacticCardPeek = 20.dp
private const val TacticCardsVisible = 3

@Composable
fun DailyTacticsSection(
    onTacticClick: (DailyTactic) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tactics = DailyTactic.entries
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .singleClickable(onClick = onViewAll),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Tactics & Puzzles",
                color = SilverColor,
                fontSize = TextSizeHeading,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View all puzzles",
                tint = SilverColor
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val allFit = TacticCardMinWidth * tactics.size + TacticCardSpacing * (tactics.size - 1) <= maxWidth
            val cardWidth = if (allFit) {
                (maxWidth - TacticCardSpacing * (tactics.size - 1)) / tactics.size
            } else {
                ((maxWidth - TacticCardPeek - TacticCardSpacing * TacticCardsVisible) / TacticCardsVisible)
                    .coerceAtLeast(TacticCardMinWidth)
            }

            val stepPx = with(LocalDensity.current) { (cardWidth + TacticCardSpacing).toPx() }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(TacticCardSpacing)
                ) {
                    tactics.forEach { tactic ->
                        DailyTacticCard(
                            tactic = tactic,
                            onClick = { onTacticClick(tactic) },
                            modifier = Modifier
                                .width(cardWidth)
                                .fillMaxHeight()
                        )
                    }
                }

                if (scrollState.maxValue > 0) {
                    PageIndicatorDots(
                        count = tactics.size,
                        selectedIndex = if (scrollState.canScrollForward) {
                            (scrollState.value / stepPx).roundToInt().coerceIn(0, tactics.lastIndex)
                        } else {
                            tactics.lastIndex
                        }
                    )
                }
            }
        }
    }
}
