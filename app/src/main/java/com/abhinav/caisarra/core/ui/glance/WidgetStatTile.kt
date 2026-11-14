package com.abhinav.caisarra.core.ui.glance

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeTitle

@Composable
fun RowScope.WidgetStatTile(value: String, label: String, accent: Color, onClick: Action? = null) {
    Box(GlanceModifier.defaultWeight().fillMaxHeight().padding(horizontal = 3.dp)) {
        WidgetCard(GlanceModifier.fillMaxSize(), onClick, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(GlanceModifier.width(18.dp).height(3.dp).background(accent).cornerRadius(2.dp))
                Spacer(GlanceModifier.height(6.dp))
                Text(text = value, style = TextStyle(color = ColorProvider(StatsTextWhite), fontSize = TextSizeTitle, fontWeight = FontWeight.Bold), maxLines = 1)
                Text(text = label, style = TextStyle(color = ColorProvider(accent), fontSize = TextSizeCaption, fontWeight = FontWeight.Medium), maxLines = 1)
            }
        }
    }
}
