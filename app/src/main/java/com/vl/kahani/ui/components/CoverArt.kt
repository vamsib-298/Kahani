package com.vl.kahani.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vl.kahani.data.Series
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniRadius
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType
import com.vl.kahani.ui.theme.Narrative
import kotlin.math.abs

/**
 * Cover art is drawn, not downloaded — a lamp-lit gradient seeded from the series id, so every
 * series has a stable, distinct cover with no image assets and no placeholder URLs.
 * Stays strictly inside the palette: maroon field, one saffron light source.
 */
@Composable
fun CoverArt(
    series: Series,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = KahaniRadius.cover,
    showTitle: Boolean = true,
) {
    val seed = abs(series.id.hashCode())
    val glowX = 0.22f + (seed % 55) / 100f
    val glowY = 0.18f + (seed / 7 % 45) / 100f
    val tilt = (seed / 13 % 4)

    val base = when (tilt) {
        0 -> listOf(KahaniColors.Maroon950, KahaniColors.Maroon800)
        1 -> listOf(KahaniColors.Maroon900, KahaniColors.Maroon700)
        2 -> listOf(KahaniColors.Maroon800, KahaniColors.Maroon950)
        else -> listOf(KahaniColors.Maroon700, KahaniColors.Maroon900)
    }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .border(1.dp, KahaniColors.Maroon600, RoundedCornerShape(cornerRadius)),
    ) {
        val w = maxWidth
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.linearGradient(base, start = Offset.Zero, end = Offset(size.width, size.height)))

            val glowCenter = Offset(size.width * glowX, size.height * glowY)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(KahaniColors.Saffron.copy(alpha = 0.30f), KahaniColors.Saffron.copy(alpha = 0f)),
                    center = glowCenter,
                    radius = size.minDimension * 0.75f,
                ),
                radius = size.minDimension * 0.75f,
                center = glowCenter,
            )

            // Lamplight rings — the "storyteller's room" motif, kept very faint.
            repeat(3) { i ->
                drawCircle(
                    color = KahaniColors.Saffron.copy(alpha = 0.06f),
                    radius = size.minDimension * (0.30f + i * 0.22f),
                    center = glowCenter,
                    style = Stroke(width = size.minDimension * 0.012f),
                )
            }

            drawRect(
                brush = Brush.verticalGradient(
                    0.45f to KahaniColors.Maroon950.copy(alpha = 0f),
                    1f to KahaniColors.Maroon950.copy(alpha = 0.82f),
                ),
            )
        }

        Text(
            text = series.title.take(1),
            fontFamily = Narrative,
            fontWeight = FontWeight.SemiBold,
            fontSize = (w.value * 0.5f).sp,
            color = KahaniColors.TextPrimary.copy(alpha = 0.10f),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = w * 0.12f),
        )

        if (showTitle) {
            Text(
                text = series.title,
                style = KahaniType.CardTitle.copy(fontSize = (w.value * 0.093f).coerceIn(11f, 17f).sp),
                color = KahaniColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(KahaniSpacing.sm),
            )
        }
    }
}

/** Thin saffron progress rail along the bottom edge of a cover. */
@Composable
fun CoverProgressRail(fraction: Float, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(KahaniColors.Maroon950.copy(alpha = 0.7f)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxSize()
                .background(KahaniColors.Saffron),
        )
    }
}
