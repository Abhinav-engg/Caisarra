package com.abhinav.caisarra.presentation.game.screens.components.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono

private val ScreenBg = Color(0xFF0B1218)
private val TextMain = Color(0xFFF4F7FB)
private val Muted = Color(0xFF8FA0B3)
private val Cyan = Color(0xFF3AD6F0)
private val Loss = Color(0xFFF08D96)

@Composable
fun AnalysisNavigationControls(
    currentPly: Int,
    totalPlies: Int,
    isPlayingToImportantMove: Boolean,
    onFirst: () -> Unit,
    onPrev: () -> Unit,
    onPlayImportant: () -> Unit,
    onNext: () -> Unit,
    onLast: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onFirst,
            enabled = currentPly > 0,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = "First Move",
                tint = if (currentPly > 0) TextMain else Muted.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = onPrev,
            enabled = currentPly > 0,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Move",
                tint = if (currentPly > 0) TextMain else Muted.copy(alpha = 0.4f),
                modifier = Modifier.size(22.dp)
            )
        }

        Button(
            onClick = onPlayImportant,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlayingToImportantMove) Loss else Cyan
            ),
            shape = RoundedCornerShape(18.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            if (isPlayingToImportantMove) {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = "Stop",
                    tint = ScreenBg,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "STOP",
                    color = ScreenBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = JetBrainsMono
                )
            } else {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = "Important Move",
                    tint = ScreenBg,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "KEY MOVE",
                    color = ScreenBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = JetBrainsMono
                )
            }
        }

        IconButton(
            onClick = onNext,
            enabled = currentPly < totalPlies,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Move",
                tint = if (currentPly < totalPlies) TextMain else Muted.copy(alpha = 0.4f),
                modifier = Modifier.size(22.dp)
            )
        }

        IconButton(
            onClick = onLast,
            enabled = currentPly < totalPlies,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = "Last Move",
                tint = if (currentPly < totalPlies) TextMain else Muted.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
