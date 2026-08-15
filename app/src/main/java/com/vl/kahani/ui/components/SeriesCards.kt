package com.vl.kahani.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vl.kahani.data.LocalStrings
import com.vl.kahani.data.Series
import com.vl.kahani.data.SeriesStatus
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniRadius
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType

/** Poster card used in every horizontal shelf on Home and Library. */
@Composable
fun SeriesPosterCard(
    series: Series,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp? = 148.dp,
    progressFraction: Float? = null,
) {
    val strings = LocalStrings.current
    val sizing = if (width != null) Modifier.width(width) else Modifier.fillMaxWidth()
    Column(
        modifier
            .then(sizing)
            .clip(RoundedCornerShape(KahaniRadius.cover))
            .clickable(onClick = onClick),
    ) {
        Box {
            CoverArt(
                series = series,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f),
            )
            if (progressFraction != null) {
                CoverProgressRail(
                    fraction = progressFraction,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
        Spacer(Modifier.height(KahaniSpacing.xs))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = strings.genre(series.genre),
                style = KahaniType.MicroBold,
                color = KahaniColors.TextMuted,
                maxLines = 1,
            )
            Text(
                text = "  ·  ${series.totalChapters}",
                style = KahaniType.Micro,
                color = KahaniColors.TextMuted,
                maxLines = 1,
            )
        }
    }
}

/** Wide row used in Search results, Library lists and download management. */
@Composable
fun SeriesListRow(
    series: Series,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val strings = LocalStrings.current
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(KahaniRadius.card))
            .clickable(onClick = onClick)
            .padding(vertical = KahaniSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverArt(
            series = series,
            modifier = Modifier.size(width = 58.dp, height = 78.dp),
            cornerRadius = KahaniRadius.row,
            showTitle = false,
        )
        Spacer(Modifier.width(KahaniSpacing.sm))
        Column(Modifier.weight(1f)) {
            Text(
                series.title,
                style = KahaniType.CardTitle,
                color = KahaniColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${strings.genre(series.genre)} · ${series.language.nativeName} · ${series.totalChapters} ${strings.chaptersLabel}",
                style = KahaniType.Micro,
                color = KahaniColors.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(KahaniSpacing.xxs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingStars(series.ratingAvg, 0)
                Spacer(Modifier.width(KahaniSpacing.xs))
                MetaTag(if (series.status == SeriesStatus.COMPLETED) strings.completed else strings.ongoing)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(KahaniSpacing.xs))
            trailing()
        }
    }
}

@Composable
fun PosterSkeleton(width: Dp = 148.dp, modifier: Modifier = Modifier) {
    Column(modifier.width(width)) {
        ShimmerBox(
            Modifier
                .width(width)
                .aspectRatio(0.72f),
            cornerRadius = KahaniRadius.cover,
        )
        Spacer(Modifier.height(KahaniSpacing.xs))
        ShimmerBox(
            Modifier
                .width(width * 0.6f)
                .height(11.dp),
            cornerRadius = 4.dp,
        )
    }
}

@Composable
fun RowSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = KahaniSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShimmerBox(Modifier.size(width = 58.dp, height = 78.dp))
        Spacer(Modifier.width(KahaniSpacing.sm))
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(KahaniSpacing.xs),
        ) {
            ShimmerBox(
                Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp),
                cornerRadius = 4.dp,
            )
            ShimmerBox(
                Modifier
                    .fillMaxWidth(0.45f)
                    .height(11.dp),
                cornerRadius = 4.dp,
            )
        }
    }
}
