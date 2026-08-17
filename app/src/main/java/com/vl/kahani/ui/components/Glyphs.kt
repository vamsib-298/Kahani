package com.vl.kahani.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vl.kahani.ui.theme.Chrome
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniType
import kotlin.math.cos
import kotlin.math.sin

/**
 * Every icon in Kahani is drawn here rather than pulled from an icon artifact. It keeps the app on
 * one visual language and matches the flat, 1px-border grammar of the rest of the design system.
 */

private const val STROKE_RATIO = 0.088f

@Composable
private fun Glyph(
    size: Dp,
    modifier: Modifier = Modifier,
    draw: DrawScope.(stroke: Float) -> Unit,
) {
    Canvas(modifier.size(size)) { draw(this, this.size.minDimension * STROKE_RATIO) }
}

enum class ChevronDirection { LEFT, RIGHT, UP, DOWN }

@Composable
fun ChevronGlyph(
    direction: ChevronDirection,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = KahaniColors.TextPrimary,
) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val h = this.size.height
        val a: Offset
        val b: Offset
        val c: Offset
        when (direction) {
            ChevronDirection.LEFT -> {
                a = Offset(w * 0.64f, h * 0.18f); b = Offset(w * 0.34f, h * 0.5f); c = Offset(w * 0.64f, h * 0.82f)
            }
            ChevronDirection.RIGHT -> {
                a = Offset(w * 0.36f, h * 0.18f); b = Offset(w * 0.66f, h * 0.5f); c = Offset(w * 0.36f, h * 0.82f)
            }
            ChevronDirection.UP -> {
                a = Offset(w * 0.18f, h * 0.64f); b = Offset(w * 0.5f, h * 0.34f); c = Offset(w * 0.82f, h * 0.64f)
            }
            ChevronDirection.DOWN -> {
                a = Offset(w * 0.18f, h * 0.36f); b = Offset(w * 0.5f, h * 0.66f); c = Offset(w * 0.82f, h * 0.36f)
            }
        }
        drawLine(tint, a, b, stroke, cap = StrokeCap.Round)
        drawLine(tint, b, c, stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun CloseGlyph(modifier: Modifier = Modifier, size: Dp = 18.dp, tint: Color = KahaniColors.TextMuted) {
    Glyph(size, modifier) { stroke ->
        val i = this.size.width * 0.24f
        val m = this.size.width - i
        drawLine(tint, Offset(i, i), Offset(m, m), stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(m, i), Offset(i, m), stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun CheckGlyph(modifier: Modifier = Modifier, size: Dp = 18.dp, tint: Color = KahaniColors.Saffron) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val h = this.size.height
        drawLine(tint, Offset(w * 0.18f, h * 0.52f), Offset(w * 0.42f, h * 0.76f), stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.42f, h * 0.76f), Offset(w * 0.84f, h * 0.24f), stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun PlusGlyph(modifier: Modifier = Modifier, size: Dp = 18.dp, tint: Color = KahaniColors.TextPrimary) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        drawLine(tint, Offset(w * 0.5f, w * 0.18f), Offset(w * 0.5f, w * 0.82f), stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.18f, w * 0.5f), Offset(w * 0.82f, w * 0.5f), stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun RefreshGlyph(modifier: Modifier = Modifier, size: Dp = 18.dp, tint: Color = KahaniColors.Saffron) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val inset = w * 0.16f
        drawArc(
            color = tint,
            startAngle = -60f,
            sweepAngle = 290f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(w - inset * 2, w - inset * 2),
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
        val tip = Offset(w * 0.76f, w * 0.2f)
        drawLine(tint, tip, Offset(w * 0.6f, w * 0.14f), stroke, cap = StrokeCap.Round)
        drawLine(tint, tip, Offset(w * 0.72f, w * 0.38f), stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun SearchGlyph(modifier: Modifier = Modifier, size: Dp = 18.dp, tint: Color = KahaniColors.TextMuted) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        drawCircle(tint, radius = w * 0.29f, center = Offset(w * 0.42f, w * 0.42f), style = Stroke(stroke))
        drawLine(tint, Offset(w * 0.64f, w * 0.64f), Offset(w * 0.86f, w * 0.86f), stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun PlayGlyph(modifier: Modifier = Modifier, size: Dp = 20.dp, tint: Color = KahaniColors.Maroon950) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.28f, h * 0.16f)
            lineTo(w * 0.84f, h * 0.5f)
            lineTo(w * 0.28f, h * 0.84f)
            close()
        }
        drawPath(path, tint)
    }
}

@Composable
fun LockGlyph(modifier: Modifier = Modifier, size: Dp = 16.dp, tint: Color = KahaniColors.TextMuted) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val h = this.size.height
        drawArc(
            color = tint,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.31f, h * 0.16f),
            size = Size(w * 0.38f, h * 0.38f),
            style = Stroke(stroke),
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.18f, h * 0.44f),
            size = Size(w * 0.64f, h * 0.42f),
            cornerRadius = CornerRadius(w * 0.12f),
        )
    }
}

@Composable
fun StarGlyph(modifier: Modifier = Modifier, size: Dp = 14.dp, tint: Color = KahaniColors.Saffron) {
    Canvas(modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val outer = this.size.minDimension * 0.48f
        val inner = outer * 0.45f
        val path = Path()
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outer else inner
            val angle = Math.toRadians((-90 + i * 36).toDouble())
            val x = cx + (r * cos(angle)).toFloat()
            val y = cy + (r * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, tint)
    }
}

@Composable
fun BookmarkGlyph(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    filled: Boolean = false,
    tint: Color = KahaniColors.TextPrimary,
) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.26f, h * 0.14f)
            lineTo(w * 0.74f, h * 0.14f)
            lineTo(w * 0.74f, h * 0.86f)
            lineTo(w * 0.5f, h * 0.64f)
            lineTo(w * 0.26f, h * 0.86f)
            close()
        }
        if (filled) drawPath(path, tint) else drawPath(path, tint, style = Stroke(stroke))
    }
}

@Composable
fun HeartGlyph(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    filled: Boolean = false,
    tint: Color = KahaniColors.TextPrimary,
) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.82f)
            cubicTo(w * 0.15f, h * 0.6f, w * 0.05f, h * 0.35f, w * 0.25f, h * 0.18f)
            cubicTo(w * 0.45f, h * 0.05f, w * 0.5f, h * 0.3f, w * 0.5f, h * 0.3f)
            cubicTo(w * 0.5f, h * 0.3f, w * 0.55f, h * 0.05f, w * 0.75f, h * 0.18f)
            cubicTo(w * 0.95f, h * 0.35f, w * 0.85f, h * 0.6f, w * 0.5f, h * 0.82f)
            close()
        }
        if (filled) drawPath(path, tint) else drawPath(path, tint, style = Stroke(stroke))
    }
}

@Composable
fun WarningGlyph(modifier: Modifier = Modifier, size: Dp = 18.dp, tint: Color = KahaniColors.TextMuted) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.15f, h * 0.85f)
            lineTo(w * 0.85f, h * 0.85f)
            close()
        }
        drawPath(path, tint, style = Stroke(stroke))
        drawLine(tint, Offset(w * 0.5f, h * 0.4f), Offset(w * 0.5f, h * 0.65f), stroke, cap = StrokeCap.Round)
        drawCircle(tint, radius = stroke * 0.8f, center = Offset(w * 0.5f, h * 0.76f))
    }
}

@Composable
fun HomeGlyph(modifier: Modifier = Modifier, size: Dp = 20.dp, tint: Color = KahaniColors.TextMuted) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val h = this.size.height
        val roof = Path().apply {
            moveTo(w * 0.14f, h * 0.5f)
            lineTo(w * 0.5f, h * 0.16f)
            lineTo(w * 0.86f, h * 0.5f)
        }
        drawPath(roof, tint, style = Stroke(stroke, cap = StrokeCap.Round))
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.26f, h * 0.46f),
            size = Size(w * 0.48f, h * 0.38f),
            cornerRadius = CornerRadius(w * 0.06f),
            style = Stroke(stroke),
        )
    }
}

@Composable
fun LibraryGlyph(modifier: Modifier = Modifier, size: Dp = 20.dp, tint: Color = KahaniColors.TextMuted) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.14f, h * 0.18f),
            size = Size(w * 0.2f, h * 0.64f),
            cornerRadius = CornerRadius(w * 0.05f),
            style = Stroke(stroke),
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.4f, h * 0.24f),
            size = Size(w * 0.2f, h * 0.58f),
            cornerRadius = CornerRadius(w * 0.05f),
            style = Stroke(stroke),
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.66f, h * 0.3f),
            size = Size(w * 0.2f, h * 0.52f),
            cornerRadius = CornerRadius(w * 0.05f),
            style = Stroke(stroke),
        )
    }
}

@Composable
fun WalletGlyph(modifier: Modifier = Modifier, size: Dp = 20.dp, tint: Color = KahaniColors.TextMuted) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.12f, h * 0.26f),
            size = Size(w * 0.76f, h * 0.48f),
            cornerRadius = CornerRadius(w * 0.1f),
            style = Stroke(stroke),
        )
        drawCircle(tint, radius = w * 0.07f, center = Offset(w * 0.68f, h * 0.5f))
    }
}

@Composable
fun TunerGlyph(modifier: Modifier = Modifier, size: Dp = 20.dp, tint: Color = KahaniColors.TextMuted) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val h = this.size.height
        listOf(0.28f to 0.66f, 0.5f to 0.36f, 0.72f to 0.58f).forEach { pair ->
            val y = pair.first
            val knob = pair.second
            drawLine(tint, Offset(w * 0.14f, h * y), Offset(w * 0.86f, h * y), stroke, cap = StrokeCap.Round)
            drawCircle(KahaniColors.Maroon900, radius = w * 0.11f, center = Offset(w * knob, h * y))
            drawCircle(tint, radius = w * 0.11f, center = Offset(w * knob, h * y), style = Stroke(stroke))
        }
    }
}

@Composable
fun BellGlyph(modifier: Modifier = Modifier, size: Dp = 20.dp, tint: Color = KahaniColors.TextPrimary) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val h = this.size.height
        val body = Path().apply {
            moveTo(w * 0.2f, h * 0.68f)
            lineTo(w * 0.28f, h * 0.44f)
            cubicTo(w * 0.3f, h * 0.18f, w * 0.7f, h * 0.18f, w * 0.72f, h * 0.44f)
            lineTo(w * 0.8f, h * 0.68f)
            close()
        }
        drawPath(body, tint, style = Stroke(stroke, cap = StrokeCap.Round))
        drawLine(tint, Offset(w * 0.42f, h * 0.81f), Offset(w * 0.58f, h * 0.81f), stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun TrashGlyph(modifier: Modifier = Modifier, size: Dp = 18.dp, tint: Color = KahaniColors.TextMuted) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val h = this.size.height
        drawLine(tint, Offset(w * 0.16f, h * 0.3f), Offset(w * 0.84f, h * 0.3f), stroke, cap = StrokeCap.Round)
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.26f, h * 0.3f),
            size = Size(w * 0.48f, h * 0.54f),
            cornerRadius = CornerRadius(w * 0.06f),
            style = Stroke(stroke),
        )
        drawLine(tint, Offset(w * 0.4f, h * 0.19f), Offset(w * 0.6f, h * 0.19f), stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun PauseGlyph(modifier: Modifier = Modifier, size: Dp = 20.dp, tint: Color = KahaniColors.Maroon950) {
    Canvas(modifier.size(size)) {
        val barW = this.size.width * 0.17f
        val barH = this.size.height * 0.76f
        val top = (this.size.height - barH) / 2f
        drawRoundRect(tint, Offset(this.size.width * 0.3f - barW / 2f, top), Size(barW, barH), CornerRadius(barW / 3f))
        drawRoundRect(tint, Offset(this.size.width * 0.7f - barW / 2f, top), Size(barW, barH), CornerRadius(barW / 3f))
    }
}

@Composable
fun SkipGlyph(
    forward: Boolean,
    seconds: Int = 15,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    tint: Color = KahaniColors.TextPrimary,
) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val w = this.size.width
            val inset = w * 0.1f
            val stroke = w * 0.07f
            drawArc(
                color = tint,
                startAngle = if (forward) -60f else -120f,
                sweepAngle = if (forward) 300f else -300f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(w - inset * 2, w - inset * 2),
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            val dir = if (forward) 1f else -1f
            val tip = Offset(w * (0.5f + dir * 0.3f), w * 0.19f)
            val arm = w * 0.12f
            drawLine(tint, tip, Offset(tip.x - dir * arm, tip.y - arm * 0.8f), stroke, cap = StrokeCap.Round)
            drawLine(tint, tip, Offset(tip.x - dir * arm, tip.y + arm * 0.8f), stroke, cap = StrokeCap.Round)
        }
        Text(
            text = "$seconds",
            style = KahaniType.MicroBold.copy(fontSize = 9.sp, letterSpacing = 0.sp),
            color = tint,
        )
    }
}

@Composable
fun CoinGlyph(modifier: Modifier = Modifier, size: Dp = 16.dp, tint: Color = KahaniColors.Saffron) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            drawCircle(tint, radius = this.size.minDimension / 2f)
            drawCircle(
                KahaniColors.Maroon950.copy(alpha = 0.25f),
                radius = this.size.minDimension * 0.37f,
                style = Stroke(this.size.minDimension * 0.05f),
            )
        }
        Text(
            text = "₹",
            fontFamily = Chrome,
            fontSize = (size.value * 0.55f).sp,
            color = KahaniColors.Maroon950,
        )
    }
}

@Composable
fun TextFormatGlyph(modifier: Modifier = Modifier, size: Dp = 14.dp, tint: Color = KahaniColors.TextMuted) {
    Canvas(modifier.size(size)) {
        val stroke = this.size.height * 0.1f
        listOf(1f, 0.82f, 0.94f, 0.6f).forEachIndexed { i, wr ->
            val y = this.size.height * (0.18f + i * 0.22f)
            drawLine(tint, Offset(0f, y), Offset(this.size.width * wr, y), stroke, cap = StrokeCap.Round)
        }
    }
}

@Composable
fun AudioFormatGlyph(modifier: Modifier = Modifier, size: Dp = 14.dp, tint: Color = KahaniColors.TextMuted) {
    Canvas(modifier.size(size)) {
        val bars = listOf(0.4f, 0.75f, 1f, 0.55f, 0.85f)
        val barW = this.size.width / (bars.size * 2f - 1f)
        bars.forEachIndexed { i, h ->
            val barH = this.size.height * h
            drawRoundRect(
                color = tint,
                topLeft = Offset(i * barW * 2f, (this.size.height - barH) / 2f),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(barW / 2f),
            )
        }
    }
}

@Composable
fun DownloadGlyph(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    tint: Color = KahaniColors.TextMuted,
    complete: Boolean = false,
) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val h = this.size.height
        if (complete) {
            drawLine(tint, Offset(w * 0.18f, h * 0.5f), Offset(w * 0.42f, h * 0.74f), stroke, cap = StrokeCap.Round)
            drawLine(tint, Offset(w * 0.42f, h * 0.74f), Offset(w * 0.84f, h * 0.22f), stroke, cap = StrokeCap.Round)
        } else {
            drawLine(tint, Offset(w * 0.5f, h * 0.14f), Offset(w * 0.5f, h * 0.62f), stroke, cap = StrokeCap.Round)
            drawLine(tint, Offset(w * 0.5f, h * 0.64f), Offset(w * 0.28f, h * 0.42f), stroke, cap = StrokeCap.Round)
            drawLine(tint, Offset(w * 0.5f, h * 0.64f), Offset(w * 0.72f, h * 0.42f), stroke, cap = StrokeCap.Round)
            drawLine(tint, Offset(w * 0.18f, h * 0.86f), Offset(w * 0.82f, h * 0.86f), stroke, cap = StrokeCap.Round)
        }
    }
}

@Composable
fun TimerGlyph(modifier: Modifier = Modifier, size: Dp = 18.dp, tint: Color = KahaniColors.TextMuted) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        drawCircle(tint, radius = w / 2f - stroke, style = Stroke(stroke))
        val c = Offset(w / 2f, w / 2f)
        drawLine(tint, c, Offset(c.x, w * 0.28f), stroke, cap = StrokeCap.Round)
        drawLine(tint, c, Offset(w * 0.7f, c.y), stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun SpeedGlyph(modifier: Modifier = Modifier, size: Dp = 18.dp, tint: Color = KahaniColors.TextMuted) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val inset = stroke
        drawArc(
            color = tint,
            startAngle = 160f,
            sweepAngle = 220f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(w - inset * 2, w - inset * 2),
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
        drawLine(tint, Offset(w / 2f, w * 0.58f), Offset(w * 0.72f, w * 0.32f), stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun ContrastGlyph(modifier: Modifier = Modifier, size: Dp = 18.dp, tint: Color = KahaniColors.TextMuted) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val r = w / 2f - stroke
        drawCircle(tint, radius = r, style = Stroke(stroke))
        drawArc(
            color = tint,
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w / 2f - r, w / 2f - r),
            size = Size(r * 2, r * 2),
        )
    }
}

@Composable
fun ProfileGlyph(modifier: Modifier = Modifier, size: Dp = 20.dp, tint: Color = KahaniColors.TextMuted) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val h = this.size.height
        
        // Draw head (circle)
        val headRadius = w * 0.25f
        drawCircle(tint, radius = headRadius, center = Offset(w / 2f, h * 0.3f))
        
        // Draw body (path-like shape)
        val bodyTop = h * 0.55f
        val bodyBottom = h * 0.95f
        val bodyLeftX = w * 0.2f
        val bodyRightX = w * 0.8f
        
        drawLine(
            tint,
            start = Offset(bodyLeftX, bodyTop),
            end = Offset(bodyRightX, bodyTop),
            strokeWidth = stroke * 2,
        )
        drawLine(
            tint,
            start = Offset(bodyLeftX, bodyTop),
            end = Offset(bodyLeftX, bodyBottom),
            strokeWidth = stroke * 2,
        )
        drawLine(
            tint,
            start = Offset(bodyRightX, bodyTop),
            end = Offset(bodyRightX, bodyBottom),
            strokeWidth = stroke * 2,
        )
    }
}

@Composable
fun UploadGlyph(modifier: Modifier = Modifier, size: Dp = 20.dp, tint: Color = KahaniColors.TextMuted) {
    Glyph(size, modifier) { stroke ->
        val w = this.size.width
        val h = this.size.height
        
        // Cloud body
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.75f)
            cubicTo(w * 0.05f, h * 0.75f, w * 0.05f, h * 0.5f, w * 0.25f, h * 0.5f)
            cubicTo(w * 0.25f, h * 0.25f, w * 0.75f, h * 0.25f, w * 0.75f, h * 0.5f)
            cubicTo(w * 0.95f, h * 0.5f, w * 0.95f, h * 0.75f, w * 0.8f, h * 0.75f)
            close()
        }
        drawPath(path, tint, style = Stroke(stroke))
        
        // Up arrow
        drawLine(tint, Offset(w * 0.5f, h * 0.45f), Offset(w * 0.5f, h * 0.85f), stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.5f, h * 0.45f), Offset(w * 0.35f, h * 0.65f), stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.5f, h * 0.45f), Offset(w * 0.65f, h * 0.65f), stroke, cap = StrokeCap.Round)
    }
}

