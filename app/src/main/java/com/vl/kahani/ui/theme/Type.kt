package com.vl.kahani.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
/**
 * Narrative face. Android's platform serif family is Noto Serif, which carries the Devanagari,
 * Tamil and Bengali coverage the catalog needs — so this resolves correctly with no font download.
 * Swap this single value if we ever bundle the Noto Serif files directly.
 */
val Narrative: FontFamily = FontFamily.Serif

/** UI chrome face. Platform sans-serif; metric-close to Inter. */
val Chrome: FontFamily = FontFamily.SansSerif

object KahaniType {
    val SeriesTitle = TextStyle(
        fontFamily = Narrative,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    )
    val ScreenTitle = TextStyle(
        fontFamily = Narrative,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 27.sp,
    )
    val ChapterTitle = TextStyle(
        fontFamily = Narrative,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 25.sp,
    )
    val CardTitle = TextStyle(
        fontFamily = Narrative,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    )
    val SectionLabel = TextStyle(
        fontFamily = Narrative,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    )
    val Synopsis = TextStyle(
        fontFamily = Narrative,
        fontWeight = FontWeight.Normal,
        fontSize = 14.5f.sp,
        lineHeight = 24.sp,
    )

    val UiBody = TextStyle(
        fontFamily = Chrome,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5f.sp,
        lineHeight = 19.sp,
    )
    val UiMedium = TextStyle(
        fontFamily = Chrome,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    )
    val UiBold = TextStyle(
        fontFamily = Chrome,
        fontWeight = FontWeight.Bold,
        fontSize = 13.5f.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2f.sp,
    )
    val Micro = TextStyle(
        fontFamily = Chrome,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp,
    )
    val MicroBold = TextStyle(
        fontFamily = Chrome,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.5f.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.6f.sp,
    )

    /** Reader body. Size is user-controlled; line height stays locked to the 1.85 ratio. */
    fun readerBody(sizeSp: Float) = TextStyle(
        fontFamily = Narrative,
        fontWeight = FontWeight.Normal,
        fontSize = sizeSp.sp,
        lineHeight = (sizeSp * 1.85f).sp,
    )
}

val Typography = Typography(
    displayLarge = KahaniType.SeriesTitle,
    titleLarge = KahaniType.ScreenTitle,
    titleMedium = KahaniType.ChapterTitle,
    titleSmall = KahaniType.CardTitle,
    bodyLarge = KahaniType.Synopsis,
    bodyMedium = KahaniType.UiBody,
    labelLarge = KahaniType.UiBold,
    labelMedium = KahaniType.UiMedium,
    labelSmall = KahaniType.Micro,
)