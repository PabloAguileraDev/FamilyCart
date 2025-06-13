package com.pablo.familycart.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pablo.familycart.R

val Typography = Typography(
    headlineMedium = TextStyle(
        fontSize = 26.sp,
        fontWeight = FontWeight.SemiBold
    ),
    headlineLarge = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineSmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    )
)