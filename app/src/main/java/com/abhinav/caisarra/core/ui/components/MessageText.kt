package com.abhinav.caisarra.core.ui.components

import android.content.ClipData
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTextWhite
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    linkColor: Color = StatsCyan,
    fontSize: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    style: TextStyle = TextStyle.Default
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val toaster = rememberToaster()
    var menuOpen by remember { mutableStateOf(false) }
    val annotated = remember(text, linkColor) { text.linkify(linkColor) }

    Box(modifier) {
        Text(
            text = annotated,
            color = color,
            fontSize = fontSize,
            lineHeight = lineHeight,
            style = style,
            modifier = Modifier.combinedClickable(
                onClick = {},
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuOpen = true
                }
            )
        )
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
            containerColor = StatsCardBg
        ) {
            DropdownMenuItem(
                text = { Text("Copy", color = StatsTextWhite) },
                leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = StatsCyan) },
                onClick = {
                    menuOpen = false
                    scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("message", text))) }
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) toaster.show("Message copied")
                }
            )
            DropdownMenuItem(
                text = { Text("Share", color = StatsTextWhite) },
                leadingIcon = { Icon(Icons.Default.Share, null, tint = StatsCyan) },
                onClick = {
                    menuOpen = false
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
                    context.startActivity(Intent.createChooser(send, null))
                }
            )
        }
    }
}
