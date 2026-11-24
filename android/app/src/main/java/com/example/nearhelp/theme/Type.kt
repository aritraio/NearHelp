package com.example.nearhelp.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun type(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = FontFamily.SansSerif, fontSize = size.sp, lineHeight = height.sp, fontWeight = weight,
)
val Typography = Typography(
    displayLarge = type(28, 34, FontWeight.SemiBold),
    headlineLarge = type(28, 34, FontWeight.SemiBold),
    headlineMedium = type(24, 30, FontWeight.SemiBold),
    headlineSmall = type(23, 29, FontWeight.SemiBold),
    titleLarge = type(19, 25, FontWeight.SemiBold),
    titleMedium = type(18, 24, FontWeight.SemiBold),
    titleSmall = type(16, 22, FontWeight.SemiBold),
    bodyLarge = type(16, 24), bodyMedium = type(14, 21), bodySmall = type(13, 19),
    labelLarge = type(16, 22, FontWeight.SemiBold), labelMedium = type(14, 20, FontWeight.Medium),
    labelSmall = type(12, 17, FontWeight.Medium),
)
