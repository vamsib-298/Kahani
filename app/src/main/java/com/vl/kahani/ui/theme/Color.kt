package com.vl.kahani.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The complete Kahani palette. No hue outside this object may appear anywhere in the app.
 * Saffron is the single accent and only ever marks something actionable or important.
 */
object KahaniColors {
    val Maroon950 = Color(0xFF150A0D)
    val Maroon900 = Color(0xFF2A1015)
    val Maroon800 = Color(0xFF3A1820)
    val Maroon700 = Color(0xFF452029)
    val Maroon600 = Color(0xFF4A2A32)

    val Saffron = Color(0xFFF2A93B)

    val TextPrimary = Color(0xFFF5EDE7)
    val TextMuted = Color(0xFFC7A8A0)

    val ReaderLightBg = Color(0xFFF5EDE7)
    val ReaderLightInk = Color(0xFF2A1015)

    /** High-contrast reading pair. Same family, pushed to the ends of the range. */
    val HighContrastInk = Color(0xFFFFFFFF)
    val HighContrastMutedInk = Color(0xFFE8DCD6)
}