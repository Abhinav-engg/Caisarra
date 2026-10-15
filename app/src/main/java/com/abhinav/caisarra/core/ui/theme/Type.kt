package com.caisaara.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
val CaisaaraFontFamily = FontFamily.Default
object CaisaaraTypography {
    val Display = TextStyle(
        fontFamily = CaisaaraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    )
    val H1 = TextStyle(
        fontFamily = CaisaaraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    )
    val H2Bold = TextStyle(
        fontFamily = CaisaaraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    )
    val H2Medium = TextStyle(
        fontFamily = CaisaaraFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    )
    val H3SemiBold = TextStyle(
        fontFamily = CaisaaraFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    )
    val BodyMedium = TextStyle(
        fontFamily = CaisaaraFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    )

    val BodyRegular = TextStyle(
        fontFamily = CaisaaraFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )

    val Notation = TextStyle(
        fontFamily = CaisaaraFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )

    val Caption = TextStyle(
        fontFamily = CaisaaraFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )
    val MicroBold = TextStyle(
        fontFamily = CaisaaraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 12.sp,
    )
    val MicroRegular = TextStyle(
        fontFamily = CaisaaraFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 12.sp,
    )
}

val CaisaaraM3Typography = Typography(
    displayLarge = CaisaaraTypography.Display,
    displayMedium = CaisaaraTypography.H1,
    displaySmall = CaisaaraTypography.H2Bold,

    headlineLarge = CaisaaraTypography.H1,
    headlineMedium = CaisaaraTypography.H2Bold,
    headlineSmall = CaisaaraTypography.H3SemiBold,

    titleLarge = CaisaaraTypography.H2Medium,
    titleMedium = CaisaaraTypography.H3SemiBold,
    titleSmall = CaisaaraTypography.Notation,

    bodyLarge = CaisaaraTypography.BodyRegular,
    bodyMedium = CaisaaraTypography.Caption,
    bodySmall = CaisaaraTypography.MicroRegular,

    labelLarge = CaisaaraTypography.BodyMedium,
    labelMedium = CaisaaraTypography.Notation,
    labelSmall = CaisaaraTypography.MicroBold,
)