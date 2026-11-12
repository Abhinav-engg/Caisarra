package com.abhinav.caisarra.features.quickplaywidget.presentation

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import com.abhinav.caisarra.R
import com.abhinav.caisarra.app.MainActivity
import com.abhinav.caisarra.core.ui.glance.WidgetHeader
import com.abhinav.caisarra.core.ui.glance.WidgetStatTile
import com.abhinav.caisarra.core.ui.glance.WidgetSurface
import com.abhinav.caisarra.core.ui.glance.widgetDeepLink
import com.abhinav.caisarra.features.quickplaywidget.domain.quickControls
import com.caisaara.ui.theme.ThemeNeonSky

@Composable
fun QuickPlayContent() {
    val context = LocalContext.current
    WidgetSurface(actionStartActivity<MainActivity>()) {
        WidgetHeader("Quick Play", "Jump into a rated game", icon = R.drawable.ic_swords, tint = ThemeNeonSky)
        Row(GlanceModifier.fillMaxWidth().defaultWeight()) {
            quickControls.forEach { control ->
                WidgetStatTile(control.clock, control.label, control.accent, widgetDeepLink(context, "play/${control.minutes}/${control.increment}"))
            }
        }
    }
}
