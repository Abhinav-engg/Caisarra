package com.abhinav.caisarra.features.themecustomization.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeTitle

@Composable
fun <T : Any> CustomizationSection(
    title: String,
    countLabel: String,
    options: List<T>,
    key: (T) -> Any,
    card: @Composable LazyItemScope.(T, Modifier) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = StatsTextWhite, fontSize = TextSizeTitle, fontWeight = FontWeight.SemiBold)
            Text("${options.size} $countLabel", color = StatsTextMuted, fontSize = TextSizeBody)
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(options, key = key) { card(it, Modifier.fillParentMaxWidth(0.46f)) }
        }
    }
}
