package com.vl.kahani.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniRadius
import com.vl.kahani.ui.theme.KahaniSize
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType

// ---- Buttons -----------------------------------------------------------------------

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
) {
    val alpha = if (enabled) 1f else 0.35f
    Row(
        modifier = modifier
            .heightIn(min = KahaniSize.touchTarget)
            .clip(RoundedCornerShape(KahaniRadius.pill))
            .background(KahaniColors.Saffron.copy(alpha = alpha))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = KahaniSpacing.lg),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(KahaniSpacing.xs))
        }
        Text(text, style = KahaniType.UiBold, color = KahaniColors.Maroon950, maxLines = 1)
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = KahaniColors.TextPrimary,
    leading: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .heightIn(min = KahaniSize.touchTarget)
            .clip(RoundedCornerShape(KahaniRadius.pill))
            .border(1.dp, KahaniColors.Maroon600, RoundedCornerShape(KahaniRadius.pill))
            .clickable(onClick = onClick)
            .padding(horizontal = KahaniSpacing.mdPlus),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(KahaniSpacing.xs))
        }
        Text(text, style = KahaniType.UiMedium, color = contentColor, maxLines = 1)
    }
}

@Composable
fun IconTapTarget(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(KahaniSize.touchTarget)
            .clip(RoundedCornerShape(KahaniRadius.pill))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

// ---- Chips -------------------------------------------------------------------------

@Composable
fun KahaniChip(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    val bg = if (selected) KahaniColors.Saffron else KahaniColors.Maroon800
    val fg = if (selected) KahaniColors.Maroon950 else KahaniColors.TextMuted
    Row(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(KahaniRadius.pill))
            .background(bg)
            .border(
                1.dp,
                if (selected) KahaniColors.Saffron else KahaniColors.Maroon600,
                RoundedCornerShape(KahaniRadius.pill),
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = KahaniSpacing.smPlus),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(KahaniSpacing.xxs))
        }
        Text(label, style = KahaniType.UiMedium, color = fg, maxLines = 1)
    }
}

@Composable
fun MetaTag(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(KahaniRadius.pill))
            .background(KahaniColors.Maroon800)
            .padding(horizontal = KahaniSpacing.xs, vertical = KahaniSpacing.xxs),
    ) {
        Text(label, style = KahaniType.MicroBold, color = KahaniColors.TextMuted, maxLines = 1)
    }
}

@Composable
fun CoinPill(balance: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(KahaniRadius.pill))
            .background(KahaniColors.Maroon950)
            .border(1.dp, KahaniColors.Maroon600, RoundedCornerShape(KahaniRadius.pill))
            .clickable(onClick = onClick)
            .padding(horizontal = KahaniSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinGlyph()
        Spacer(Modifier.width(KahaniSpacing.xxs))
        Text("$balance", style = KahaniType.UiBold, color = KahaniColors.TextPrimary)
    }
}

// ---- Surfaces ----------------------------------------------------------------------

@Composable
fun KahaniCard(
    modifier: Modifier = Modifier,
    elevatedSurface: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = KahaniSpacing.md,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(KahaniRadius.card))
            .background(if (elevatedSurface) KahaniColors.Maroon800 else KahaniColors.Maroon900)
            .border(1.dp, KahaniColors.Maroon600, RoundedCornerShape(KahaniRadius.card))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
    ) { content() }
}

@Composable
fun SectionHeader(
    label: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = KahaniType.SectionLabel, color = KahaniColors.TextPrimary)
        trailing?.invoke()
    }
}

@Composable
fun ScreenTitleBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = KahaniSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconTapTarget(onClick = onBack) { ChevronGlyph(ChevronDirection.LEFT) }
        } else {
            Spacer(Modifier.width(KahaniSpacing.xs))
        }
        Text(
            title,
            style = KahaniType.ScreenTitle,
            color = KahaniColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = if (onBack != null) 0.dp else KahaniSpacing.xs),
        )
        actions?.invoke()
    }
}

// ---- Progress ----------------------------------------------------------------------

@Composable
fun ProgressTrack(
    fraction: Float,
    modifier: Modifier = Modifier,
    height: Dp = KahaniSize.trackHeight,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(2.dp))
            .background(KahaniColors.Maroon600),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp))
                .background(KahaniColors.Saffron),
        )
    }
}

@Composable
fun RatingStars(rating: Float, count: Int, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        StarGlyph(size = 13.dp)
        Spacer(Modifier.width(KahaniSpacing.xxs))
        Text(
            text = ((rating * 10).toInt() / 10f).toString(),
            style = KahaniType.MicroBold,
            color = KahaniColors.TextPrimary,
        )
        if (count > 0) {
            Spacer(Modifier.width(KahaniSpacing.xxs))
            Text("($count)", style = KahaniType.Micro, color = KahaniColors.TextMuted)
        }
    }
}

// ---- Input -------------------------------------------------------------------------

@Composable
fun KahaniSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    onSubmit: () -> Unit = {},
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = KahaniSize.touchTarget)
            .clip(RoundedCornerShape(KahaniRadius.pill))
            .background(KahaniColors.Maroon800)
            .border(
                1.dp,
                if (focused) KahaniColors.Saffron else KahaniColors.Maroon600,
                RoundedCornerShape(KahaniRadius.pill),
            )
            .padding(horizontal = KahaniSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchGlyph(tint = if (focused) KahaniColors.Saffron else KahaniColors.TextMuted)
        Spacer(Modifier.width(KahaniSpacing.xs))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    hint,
                    style = KahaniType.UiBody,
                    color = KahaniColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                interactionSource = interaction,
                textStyle = KahaniType.UiBody.copy(color = KahaniColors.TextPrimary),
                cursorBrush = SolidColor(KahaniColors.Saffron),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            IconTapTarget(onClick = { onValueChange("") }, modifier = Modifier.size(40.dp)) {
                CloseGlyph()
            }
        }
    }
}

@Composable
fun KahaniTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 92.dp,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(KahaniRadius.card))
            .background(KahaniColors.Maroon800)
            .border(
                1.dp,
                if (focused) KahaniColors.Saffron else KahaniColors.Maroon600,
                RoundedCornerShape(KahaniRadius.card),
            )
            .padding(KahaniSpacing.sm),
    ) {
        if (value.isEmpty()) {
            Text(hint, style = KahaniType.UiBody, color = KahaniColors.TextMuted)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            interactionSource = interaction,
            textStyle = KahaniType.Synopsis.copy(color = KahaniColors.TextPrimary),
            cursorBrush = SolidColor(KahaniColors.Saffron),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun StarRatingPicker(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        (1..5).forEach { value ->
            IconTapTarget(onClick = { onRatingChange(value) }, modifier = Modifier.size(40.dp)) {
                StarGlyph(
                    size = 22.dp,
                    tint = if (value <= rating) KahaniColors.Saffron else KahaniColors.Maroon600,
                )
            }
        }
    }
}

// ---- Rows --------------------------------------------------------------------------
@Composable
fun SettingToggleRow(
    title: String,
    body: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = KahaniSize.touchTarget)
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = KahaniSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(KahaniSpacing.sm))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = KahaniType.UiBody, color = KahaniColors.TextPrimary)
            if (body != null) {
                Spacer(Modifier.height(2.dp))
                Text(body, style = KahaniType.Micro, color = KahaniColors.TextMuted)
            }
        }
        Spacer(Modifier.width(KahaniSpacing.sm))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = KahaniColors.Maroon950,
                checkedTrackColor = KahaniColors.Saffron,
                checkedBorderColor = KahaniColors.Saffron,
                uncheckedThumbColor = KahaniColors.TextMuted,
                uncheckedTrackColor = KahaniColors.Maroon700,
                uncheckedBorderColor = KahaniColors.Maroon600,
            ),
        )
    }
}

@Composable
fun NavRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = KahaniSize.touchTarget)
            .clickable(onClick = onClick)
            .padding(vertical = KahaniSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(KahaniSpacing.sm))
        }
        Text(
            title,
            style = KahaniType.UiBody,
            color = KahaniColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        if (detail != null) {
            Text(detail, style = KahaniType.Micro, color = KahaniColors.TextMuted)
            Spacer(Modifier.width(KahaniSpacing.xs))
        }
        ChevronGlyph(ChevronDirection.RIGHT, size = 16.dp, tint = KahaniColors.TextMuted)
    }
}

@Composable
fun HairlineDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(KahaniColors.Maroon600),
    )
}

// ---- States ------------------------------------------------------------------------

@Composable
fun ShimmerBox(modifier: Modifier = Modifier, cornerRadius: Dp = KahaniRadius.row) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label = "shimmerShift",
    )
    Box(
        modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(KahaniColors.Maroon800)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        KahaniColors.Maroon700.copy(alpha = 0.9f),
                        Color.Transparent,
                    ),
                    start = Offset(shift * 420f, 0f),
                    end = Offset(shift * 420f + 280f, 280f),
                ),
            ),
    )
}

@Composable
fun StateMessage(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    showRetryIcon: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KahaniSpacing.xl, vertical = KahaniSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            title,
            style = KahaniType.ChapterTitle,
            color = KahaniColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(KahaniSpacing.xs))
        Text(
            body,
            style = KahaniType.UiBody,
            color = KahaniColors.TextMuted,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(KahaniSpacing.mdPlus))
            GhostButton(
                text = actionLabel,
                onClick = onAction,
                leading = if (showRetryIcon) {
                    { RefreshGlyph(size = 16.dp) }
                } else {
                    null
                },
            )
        }
    }
}
