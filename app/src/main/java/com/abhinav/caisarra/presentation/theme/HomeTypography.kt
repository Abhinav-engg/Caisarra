package com.abhinav.caisarra.presentation.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.R

val JetBrainsMono = FontFamily(
    Font(
        resId = R.font.jetbrains_mono_regular,
        weight = FontWeight.Normal
    ),
    Font(
        resId = R.font.jetbrains_mono_medium,
        weight = FontWeight.Medium
    ),
    Font(
        resId = R.font.jetbrains_mono_bold,
        weight = FontWeight.Bold
    )
)

val HomeTitleStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp
)

val HomeBodyStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp
)
