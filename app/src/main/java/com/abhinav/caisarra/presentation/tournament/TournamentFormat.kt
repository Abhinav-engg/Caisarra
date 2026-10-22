package com.abhinav.caisarra.presentation.tournament

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.R

object TournamentColors {
    val Background = Color(0xFF090D14)
    val Card = Color(0xFF0F151E)
    val CardBorder = Color(0xFF1E2838)
    val ActiveBorder = Color(0xFF00E5FF)
    val Accent = Color(0xFF00E5FF)
    val Success = Color(0xFF10B981)
    val Danger = Color(0xFFEF4444)
    val Text = Color.White
    val Muted = Color(0xFF8B98A5)
    val Subtitle = Color(0xFF6F7D8E)
    val InputBackground = Color(0x990A0F17)
    val InputBorder = Color(0xFF223040)
}

@Composable
fun TournamentBackground(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TournamentColors.Background)
    ) {
        // Chess King piece watermark in the background
        Image(
            painter = painterResource(id = R.drawable.dark_chess_piece),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.18f),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopStart
        )
        content()
    }
}

@Composable
fun TournamentHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = title.uppercase(),
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TournamentActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF01E3F9),
                        Color(0xFF0EB4C4),
                        Color(0xFF1D828C)
                    )
                )
            )
            .clickable(
                enabled = enabled && !loading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.Black,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TournamentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textAlignment: TextAlign = TextAlign.Start
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                placeholder,
                color = Color(0xFF596575),
                fontSize = 14.sp,
                textAlign = textAlignment,
                modifier = if (textAlignment == TextAlign.Center) Modifier.fillMaxWidth() else Modifier
            )
        },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 14.5.sp,
            textAlign = textAlignment
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = TournamentColors.InputBackground,
            unfocusedContainerColor = TournamentColors.InputBackground,
            disabledContainerColor = TournamentColors.InputBackground,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = TournamentColors.ActiveBorder,
            unfocusedBorderColor = TournamentColors.InputBorder,
            cursorColor = TournamentColors.ActiveBorder
        )
    )
}

@Composable
fun tournamentFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = TournamentColors.InputBackground,
    unfocusedContainerColor = TournamentColors.InputBackground,
    disabledContainerColor = TournamentColors.InputBackground,
    focusedTextColor = TournamentColors.Text,
    unfocusedTextColor = TournamentColors.Text,
    focusedBorderColor = TournamentColors.ActiveBorder,
    unfocusedBorderColor = TournamentColors.InputBorder,
    focusedLabelColor = TournamentColors.ActiveBorder,
    unfocusedLabelColor = TournamentColors.Muted,
    cursorColor = TournamentColors.ActiveBorder
)

fun formatLabel(format: String): String = when (format.lowercase()) {
    "swiss" -> "Swiss"
    "round_robin", "round robin" -> "Round Robin"
    "knockout" -> "Knockout"
    else -> format.replaceFirstChar { it.uppercase() }
}

fun statusLabel(status: String): String = when (status.lowercase()) {
    "registration" -> "Registration Open"
    "ongoing" -> "In Progress"
    "completed" -> "Completed"
    "cancelled" -> "Cancelled"
    else -> status.replaceFirstChar { it.uppercase() }
}

fun timeControlDisplay(timeControl: String): String = when (timeControl.trim()) {
    "1+0" -> "1 min (Bullet)"
    "3+0" -> "3 min (Blitz)"
    "3+2" -> "3+2 (Blitz)"
    "5+0" -> "5 min (Blitz)"
    "10+0" -> "10 min (Rapid)"
    "15+10" -> "15+10 (Rapid)"
    else -> timeControl
}

fun resultLabel(result: String?): String = when (result) {
    null, "" -> ""
    "white_win" -> "1-0"
    "black_win" -> "0-1"
    "draw" -> "½-½"
    else -> result
}

fun scoreLabel(score: Double): String =
    if (score % 1.0 == 0.0) score.toInt().toString() else score.toString()
