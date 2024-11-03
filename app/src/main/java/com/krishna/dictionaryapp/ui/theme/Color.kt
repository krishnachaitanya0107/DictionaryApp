package com.krishna.dictionaryapp.ui.theme

import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color

val Teal200 = Color(0xFF03DAC5)
val lightBlue = Color(0xFF10ACF3)
val darkBlue = Color(0xFF3F51B5)

val LightGray = Color(0xFFD8D8D8)
val DarkGray = Color(0xFF2A2A2A)


val Colors.titleColor
    get() = if (isLight) DarkGray else LightGray


val Colors.descriptionColor
    get() = if (isLight) DarkGray.copy(alpha = 0.5f)
    else LightGray.copy(alpha = 0.5f)
