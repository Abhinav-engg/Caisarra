package com.abhinav.caisarra.features.themecustomization.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.data.local.BOARD_THEME_KEY
import com.abhinav.caisarra.data.local.PIECE_THEME_KEY
import com.abhinav.caisarra.data.local.settingsDataStore
import com.abhinav.caisarra.presentation.game.model.BoardThemes
import com.abhinav.caisarra.presentation.game.model.PieceThemes
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeHeading
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun ThemeCustomizationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val savedTheme by remember(context) {
        context.settingsDataStore.data.map { BoardThemes.getByStorageKey(it[BOARD_THEME_KEY]) }
    }.collectAsState(initial = BoardThemes.MidnightCyan)
    val savedPieces by remember(context) {
        context.settingsDataStore.data.map { PieceThemes.getByStorageKey(it[PIECE_THEME_KEY]) }
    }.collectAsState(initial = PieceThemes.Classic)
    var selectedTheme by remember { mutableStateOf(savedTheme) }
    var selectedPieces by remember { mutableStateOf(savedPieces) }
    LaunchedEffect(savedTheme) { selectedTheme = savedTheme }
    LaunchedEffect(savedPieces) { selectedPieces = savedPieces }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .systemBarsPadding()
    ) {
        CaisaaraTopBar(title = "THEME CUSTOMIZATION", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Board & Theme Customization", color = StatsTextWhite, fontSize = TextSizeHeading, fontWeight = FontWeight.Bold)
                Text("Your board. Your playing style.", color = StatsTextMuted, fontSize = TextSizeBody)
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) { ThemePreviewCard(selectedTheme, selectedPieces) }
            CustomizationSection("Choose your theme", "themes", BoardThemes.allThemes, key = { it.id }) { theme, modifier ->
                CustomizationOptionCard(
                    icon = R.drawable.white_knight,
                    title = theme.name,
                    description = theme.description,
                    selected = theme.id == selectedTheme.id,
                    onClick = { selectedTheme = theme },
                    modifier = modifier,
                    iconBorder = theme.darkSquare
                ) { ThemePaletteBar(theme.palette) }
            }
            CustomizationSection("Choose your pieces", "sets", PieceThemes.allThemes, key = { it.id }) { pieces, modifier ->
                CustomizationOptionCard(
                    icon = pieces.previewWhitePiece,
                    title = pieces.name,
                    description = pieces.description,
                    selected = pieces.id == selectedPieces.id,
                    onClick = { selectedPieces = pieces },
                    modifier = modifier
                ) { PieceSetPreviewRow(pieces) }
            }
        }
        GeneralButton(
            text = "Save & Apply",
            onClick = {
                scope.launch {
                    context.settingsDataStore.edit {
                        it[BOARD_THEME_KEY] = selectedTheme.id.storageKey
                        it[PIECE_THEME_KEY] = selectedPieces.id.storageKey
                    }
                }
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}
