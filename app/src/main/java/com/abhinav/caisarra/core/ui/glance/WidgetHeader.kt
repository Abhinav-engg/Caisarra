package com.abhinav.caisarra.core.ui.glance

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.abhinav.caisarra.R
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeMedium
import com.caisaara.ui.theme.ThemeNeonSky

@Composable
fun WidgetHeader(title: String, subtitle: String, highlight: Boolean = false, icon: Int = R.drawable.ic_streak_flame, tint: Color? = null, trailing: @Composable () -> Unit = {}) {
    Row(GlanceModifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(provider = ImageProvider(icon), contentDescription = null, modifier = GlanceModifier.size(32.dp), colorFilter = tint?.let { ColorFilter.tint(ColorProvider(it)) })
        Spacer(GlanceModifier.width(8.dp))
        Column(GlanceModifier.defaultWeight()) {
            Text(text = title, style = TextStyle(color = ColorProvider(StatsTextWhite), fontSize = TextSizeMedium, fontWeight = FontWeight.Bold), maxLines = 1)
            Text(text = subtitle, style = TextStyle(color = ColorProvider(if (highlight) ThemeNeonSky else StatsTextMuted), fontSize = TextSizeCaption), maxLines = 1)
        }
        trailing()
    }
}
