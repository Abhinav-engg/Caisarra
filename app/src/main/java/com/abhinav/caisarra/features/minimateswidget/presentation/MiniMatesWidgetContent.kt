package com.abhinav.caisarra.features.minimateswidget.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.glance.WidgetPill
import com.abhinav.caisarra.core.ui.glance.WidgetSurface
import com.abhinav.caisarra.core.ui.glance.widgetDeepLink
import com.abhinav.caisarra.features.minimateswidget.domain.MiniMatesWidgetState
import com.caisaara.ui.theme.StatsPeakBadgeText
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeHeading
import com.caisaara.ui.theme.TextSizeMicro
import com.caisaara.ui.theme.ThemeNeonSky

@Composable
fun MiniMatesWidgetContent(state: MiniMatesWidgetState) {
    val overview = state.overview
    val level = state.level
    val size = LocalSize.current
    val boardHeight = size.height - 24.dp
    val total = overview.chapters.sumOf { it.levels.size }
    val subtitle = level?.title?.ifBlank { null } ?: level?.chapterName?.ifBlank { null } ?: "Find the checkmate"
    WidgetSurface(widgetDeepLink(LocalContext.current, "minimates")) {
        Row(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            if (level != null) {
                MiniMatesBoard(level, minOf(boardHeight / level.viewport.rows, size.width * 0.5f / level.viewport.cols))
            } else {
                Image(provider = ImageProvider(R.drawable.ic_tactic_mini_mates), contentDescription = null, modifier = GlanceModifier.size(boardHeight))
            }
            Spacer(GlanceModifier.width(14.dp))
            Column(GlanceModifier.defaultWeight()) {
                Text(text = "MINI MATES · LEVEL ${overview.currentLevel}", style = TextStyle(color = ColorProvider(ThemeNeonSky), fontSize = TextSizeMicro, fontWeight = FontWeight.Bold), maxLines = 1)
                Text(text = "Mate in ${level?.targetMoves ?: 1}", style = TextStyle(color = ColorProvider(StatsTextWhite), fontSize = TextSizeHeading, fontWeight = FontWeight.Bold), maxLines = 1)
                Text(text = subtitle, style = TextStyle(color = ColorProvider(StatsTextMuted), fontSize = TextSizeCaption), maxLines = 1)
                Spacer(GlanceModifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "★ ${overview.totalStars}", style = TextStyle(color = ColorProvider(StatsPeakBadgeText), fontSize = TextSizeCaption, fontWeight = FontWeight.Bold))
                    Text(text = "  ·  ${overview.completedLevels}/$total solved", style = TextStyle(color = ColorProvider(StatsTextMuted), fontSize = TextSizeCaption), maxLines = 1)
                }
                Spacer(GlanceModifier.height(10.dp))
                WidgetPill("Solve now", true)
            }
        }
    }
}
