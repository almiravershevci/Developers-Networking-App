package com.example.developernetworkingapp.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object AppDesignTokens {
    val screenHorizontalPadding = 18.dp
    val screenVerticalSpacing = 14.dp
    val screenContentPadding = PaddingValues(bottom = 28.dp, top = 6.dp)

    val cardShape = RoundedCornerShape(18.dp)
    val cardLargeShape = RoundedCornerShape(20.dp)
    val cardInnerPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)

    val inputSectionSpacing = 16.dp
    val compactButtonHeight = 38.dp
    const val notificationAutoHideMs = 3500L
}
