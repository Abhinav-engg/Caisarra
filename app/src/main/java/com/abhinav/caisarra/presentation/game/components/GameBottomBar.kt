package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.caisaara.ui.theme.CardBackground
import com.caisaara.ui.theme.CardBorderNavy
import com.caisaara.ui.theme.PlaceholderColor
import com.caisaara.ui.theme.RedNormal
import com.caisaara.ui.theme.SubtleText
import com.caisaara.ui.theme.TextColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameBottomBar(
    canUndo: Boolean,
    canRedo: Boolean,
    optionsEnabled: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onOfferDraw: () -> Unit,
    onResign: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showOptions by remember { mutableStateOf(false) }
    var showResignConfirm by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth().background(CardBackground)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(CardBorderNavy)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomBarItem(Icons.Default.Menu, "Options", optionsEnabled, { showOptions = true }, Modifier.weight(1f))
            BottomBarItem(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Back", canUndo, onUndo, Modifier.weight(1f))
            BottomBarItem(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Forward", canRedo, onRedo, Modifier.weight(1f))
        }
    }

    if (showOptions) {
        ModalBottomSheet(
            onDismissRequest = { showOptions = false },
            containerColor = CardBackground
        ) {
            OptionRow("Offer draw", TextColor) {
                showOptions = false
                onOfferDraw()
            }
            OptionRow("Resign", RedNormal) {
                showOptions = false
                showResignConfirm = true
            }
        }
    }

    if (showResignConfirm) {
        AlertDialog(
            onDismissRequest = { showResignConfirm = false },
            containerColor = CardBackground,
            titleContentColor = TextColor,
            textContentColor = SubtleText,
            title = { Text("Resign game?") },
            text = { Text("This will end the game.") },
            confirmButton = {
                TextButton(onClick = {
                    showResignConfirm = false
                    onResign()
                }) { Text("Resign", color = RedNormal) }
            },
            dismissButton = {
                TextButton(onClick = { showResignConfirm = false }) {
                    Text("Cancel", color = TextColor)
                }
            }
        )
    }
}

@Composable
private fun BottomBarItem(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (enabled) TextColor else PlaceholderColor

    Column(
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color)
        Text(text = label, color = color, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun OptionRow(text: String, color: Color, onClick: () -> Unit) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    )
}