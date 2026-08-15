package com.vl.kahani.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vl.kahani.data.Chapter
import com.vl.kahani.data.Format
import com.vl.kahani.data.LocalStore
import com.vl.kahani.data.LocalStrings
import com.vl.kahani.data.Series
import com.vl.kahani.data.SeriesStatus
import com.vl.kahani.ui.components.AudioFormatGlyph
import com.vl.kahani.ui.components.BookmarkGlyph
import com.vl.kahani.ui.components.CoinGlyph
import com.vl.kahani.ui.components.CoverArt
import com.vl.kahani.ui.components.GhostButton
import com.vl.kahani.ui.components.IconTapTarget
import com.vl.kahani.ui.components.KahaniCard
import com.vl.kahani.ui.components.LoadState
import com.vl.kahani.ui.components.LockGlyph
import com.vl.kahani.ui.components.MetaTag
import com.vl.kahani.ui.components.PlayGlyph
import com.vl.kahani.ui.components.PrimaryButton
import com.vl.kahani.ui.components.RatingStars
import com.vl.kahani.ui.components.ScreenTitleBar
import com.vl.kahani.ui.components.SectionHeader
import com.vl.kahani.ui.components.ShimmerBox
import com.vl.kahani.ui.components.StarRatingPicker
import com.vl.kahani.ui.components.StateMessage
import com.vl.kahani.ui.components.TextFormatGlyph
import com.vl.kahani.ui.components.KahaniTextArea
import com.vl.kahani.ui.components.rememberLoader
import com.vl.kahani.ui.nav.LocalNavigator
import com.vl.kahani.ui.nav.Screen
import com.vl.kahani.ui.player.LocalPlayback
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniRadius
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType

@Composable
fun SeriesDetailScreen(seriesId: String, modifier: Modifier = Modifier) {
    val store = LocalStore.current
    val strings = LocalStrings.current
    val nav = LocalNavigator.current
    val playback = LocalPlayback.current

    val series = store.visibleSeries().firstOrNull { it.id == seriesId }
    val loader = rememberLoader(seriesId, willFail = series == null)

    var pendingUnlock by remember { mutableStateOf<Chapter?>(null) }
    var pendingFormat by remember { mutableStateOf(Format.TEXT) }
    var showTopUp by remember { mutableStateOf(false) }
    var justUnlockedId by remember { mutableStateOf<String?>(null) }
    var showReviewSheet by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {
        ScreenTitleBar(
            title = series?.title ?: strings.appName,
            onBack = { nav.back() },
            actions = {
                if (series != null) {
                    IconTapTarget(onClick = { store.toggleSaved(series.id) }) {
                        BookmarkGlyph(
                            filled = store.savedSeriesIds.contains(series.id),
                            tint = if (store.savedSeriesIds.contains(series.id)) {
                                KahaniColors.Saffron
                            } else {
                                KahaniColors.TextPrimary
                            },
                        )
                    }
                }
            },
        )

        when {
            loader.state == LoadState.LOADING -> DetailSkeleton()

            series == null || loader.state == LoadState.ERROR -> StateMessage(
                title = strings.errorTitle,
                body = strings.errorBody,
                actionLabel = strings.retry,
                onAction = { loader.retry() },
                showRetryIcon = true,
            )

            else -> {
                val chapters = store.chapters(series.id)
                val progress = store.progress[series.id]
                val resumeChapter = chapters.firstOrNull { it.id == progress?.chapterId } ?: chapters.first()

                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = KahaniSpacing.xxl),
                ) {
                    item { SeriesHeader(series) }

                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = KahaniSpacing.md, vertical = KahaniSpacing.sm),
                            horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs),
                        ) {
                            PrimaryButton(
                                text = if (progress != null) {
                                    "${strings.continueChapter} ${resumeChapter.chapterNumber}"
                                } else {
                                    strings.startSeries
                                },
                                onClick = {
                                    nav.go(Screen.Reader(series.id, resumeChapter.id))
                                },
                                modifier = Modifier.weight(1f),
                                leading = { PlayGlyph(size = 14.dp) },
                            )
                            GhostButton(
                                text = if (store.followedSeriesIds.contains(series.id)) {
                                    strings.following
                                } else {
                                    strings.follow
                                },
                                onClick = { store.toggleFollowed(series.id) },
                            )
                        }
                    }

                    item {
                        Text(
                            text = series.synopsis,
                            style = KahaniType.Synopsis,
                            color = KahaniColors.TextPrimary,
                            modifier = Modifier.padding(horizontal = KahaniSpacing.md),
                        )
                    }

                    item { ProductionNote(series) }

                    item {
                        ReviewsSection(
                            seriesId = series.id,
                            onWriteReview = { showReviewSheet = true },
                        )
                    }

                    item {
                        SectionHeader(
                            label = "${series.totalChapters} ${strings.chaptersLabel}",
                            modifier = Modifier.padding(
                                start = KahaniSpacing.md,
                                end = KahaniSpacing.md,
                                top = KahaniSpacing.lg,
                                bottom = KahaniSpacing.sm,
                            ),
                        )
                    }

                    items(chapters.size) { index ->
                        val chapter = chapters[index]
                        ChapterRow(
                            chapter = chapter,
                            unlocked = store.isUnlocked(chapter),
                            justUnlocked = justUnlockedId == chapter.id,
                            onShimmerDone = { justUnlockedId = null },
                            onOpen = { format ->
                                if (store.isUnlocked(chapter)) {
                                    openChapter(
                                        format = format,
                                        series = series,
                                        chapter = chapter,
                                        openReader = { nav.go(Screen.Reader(series.id, chapter.id)) },
                                        startAudio = { playback.start(series, chapter) },
                                    )
                                } else {
                                    pendingFormat = format
                                    pendingUnlock = chapter
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    val chapterToUnlock = pendingUnlock
    if (chapterToUnlock != null && series != null) {
        UnlockDialog(
            chapter = chapterToUnlock,
            balance = store.coinBalance,
            onDismiss = { pendingUnlock = null },
            onConfirm = {
                val ok = store.unlockChapter(chapterToUnlock, series.title)
                pendingUnlock = null
                if (ok) {
                    justUnlockedId = chapterToUnlock.id
                } else {
                    showTopUp = true
                }
            },
        )
    }

    if (showTopUp) {
        NotEnoughCoinsDialog(
            onDismiss = { showTopUp = false },
            onTopUp = {
                showTopUp = false
                nav.selectTab(Screen.Wallet)
            },
        )
    }

    if (showReviewSheet && series != null) {
        ReviewDialog(
            existing = store.myReview(series.id),
            onDismiss = { showReviewSheet = false },
            onSubmit = { rating, text ->
                store.submitReview(series.id, rating, text)
                showReviewSheet = false
            },
        )
    }
}

@Composable
private fun ReviewsSection(seriesId: String, onWriteReview: () -> Unit) {
    val store = LocalStore.current
    val strings = LocalStrings.current
    val series = store.visibleSeries().first { it.id == seriesId }
    val reviews = store.reviewsFor(seriesId)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = KahaniSpacing.md),
    ) {
        SectionHeader(
            label = strings.ratingsReviews,
            modifier = Modifier.padding(bottom = KahaniSpacing.sm),
            trailing = {
                Text(
                    text = if (store.myReview(seriesId) != null) strings.editReview else strings.writeReview,
                    style = KahaniType.Micro,
                    color = KahaniColors.Saffron,
                    modifier = Modifier
                        .clickable(onClick = onWriteReview)
                        .padding(KahaniSpacing.xs),
                )
            },
        )
        KahaniCard(Modifier.fillMaxWidth()) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ((store.displayRating(series) * 10).toInt() / 10f).toString(),
                        style = KahaniType.SeriesTitle,
                        color = KahaniColors.TextPrimary,
                    )
                    Spacer(Modifier.width(KahaniSpacing.xs))
                    Column {
                        RatingStars(store.displayRating(series), store.ratingCount(series))
                    }
                }
                if (reviews.isEmpty()) {
                    Spacer(Modifier.height(KahaniSpacing.sm))
                    Text(strings.noReviews, style = KahaniType.UiBody, color = KahaniColors.TextMuted)
                } else {
                    reviews.take(2).forEach { review ->
                        Spacer(Modifier.height(KahaniSpacing.md))
                        ReviewRow(review)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewRow(review: com.vl.kahani.data.Review) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                review.authorName,
                style = KahaniType.CardTitle,
                color = KahaniColors.TextPrimary,
            )
            Spacer(Modifier.width(KahaniSpacing.xs))
            RatingStars(review.rating.toFloat(), 0)
        }
        if (review.text.isNotBlank()) {
            Spacer(Modifier.height(KahaniSpacing.xxs))
            Text(review.text, style = KahaniType.Synopsis, color = KahaniColors.TextMuted)
        }
    }
}

@Composable
private fun ReviewDialog(
    existing: com.vl.kahani.data.Review?,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit,
) {
    val strings = LocalStrings.current
    var rating by remember { mutableIntStateOf(existing?.rating ?: 0) }
    var text by remember { mutableStateOf(existing?.text ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        KahaniCard(Modifier.fillMaxWidth(), elevatedSurface = true, contentPadding = KahaniSpacing.lg) {
            Column {
                Text(
                    if (existing != null) strings.editReview else strings.writeReview,
                    style = KahaniType.ChapterTitle,
                    color = KahaniColors.TextPrimary,
                )
                Spacer(Modifier.height(KahaniSpacing.md))
                Text(strings.yourRating, style = KahaniType.Micro, color = KahaniColors.TextMuted)
                StarRatingPicker(rating = rating, onRatingChange = { rating = it })
                Spacer(Modifier.height(KahaniSpacing.sm))
                KahaniTextArea(
                    value = text,
                    onValueChange = { text = it },
                    hint = strings.reviewHint,
                )
                Spacer(Modifier.height(KahaniSpacing.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs)) {
                    GhostButton(strings.cancel, onDismiss, Modifier.weight(1f))
                    PrimaryButton(
                        text = strings.postReview,
                        onClick = { onSubmit(rating, text) },
                        modifier = Modifier.weight(1f),
                        enabled = rating > 0,
                    )
                }
            }
        }
    }
}

private fun openChapter(
    format: Format,
    series: Series,
    chapter: Chapter,
    openReader: () -> Unit,
    startAudio: () -> Unit,
) {
    if (format == Format.TEXT) openReader() else startAudio()
}

@Composable
private fun SeriesHeader(series: Series) {
    val strings = LocalStrings.current
    val store = LocalStore.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = KahaniSpacing.md, vertical = KahaniSpacing.xs),
    ) {
        CoverArt(
            series = series,
            modifier = Modifier.size(width = 124.dp, height = 168.dp),
            showTitle = false,
        )
        Spacer(Modifier.width(KahaniSpacing.md))
        Column(Modifier.weight(1f)) {
            Text(series.title, style = KahaniType.SeriesTitle, color = KahaniColors.TextPrimary)
            Spacer(Modifier.height(KahaniSpacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xxs)) {
                MetaTag(strings.genre(series.genre))
                MetaTag(series.language.nativeName)
            }
            Spacer(Modifier.height(KahaniSpacing.xs))
            MetaTag(if (series.status == SeriesStatus.COMPLETED) strings.completed else strings.ongoing)
            Spacer(Modifier.height(KahaniSpacing.xs))
            RatingStars(store.displayRating(series), store.ratingCount(series))
            Spacer(Modifier.height(KahaniSpacing.xs))
            Text(
                "${strings.narratedBy} ${series.narratorName}",
                style = KahaniType.Micro,
                color = KahaniColors.TextMuted,
            )
        }
    }
}

@Composable
private fun ProductionNote(series: Series) {
    KahaniCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KahaniSpacing.md, vertical = KahaniSpacing.md),
        contentPadding = KahaniSpacing.sm,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(3.dp, 26.dp)
                    .background(KahaniColors.Saffron, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.width(KahaniSpacing.sm))
            Text(
                series.productionNote,
                style = KahaniType.Micro,
                color = KahaniColors.TextMuted,
            )
        }
    }
}

@Composable
private fun ChapterRow(
    chapter: Chapter,
    unlocked: Boolean,
    justUnlocked: Boolean,
    onShimmerDone: () -> Unit,
    onOpen: (Format) -> Unit,
) {
    val strings = LocalStrings.current
    val sweep = remember { Animatable(0f) }

    LaunchedEffect(justUnlocked) {
        if (justUnlocked) {
            sweep.snapTo(0f)
            sweep.animateTo(1f, tween(durationMillis = 700))
            onShimmerDone()
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = KahaniSpacing.md, vertical = KahaniSpacing.xxs),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clip(RoundedCornerShape(KahaniRadius.row))
                .background(KahaniColors.Maroon800)
                .border(1.dp, KahaniColors.Maroon600, RoundedCornerShape(KahaniRadius.row))
                .clickable { onOpen(Format.TEXT) }
                .padding(horizontal = KahaniSpacing.sm, vertical = KahaniSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(KahaniRadius.chip))
                    .background(KahaniColors.Maroon700),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    chapter.chapterNumber.toString().padStart(2, '0'),
                    style = KahaniType.MicroBold,
                    color = if (unlocked) KahaniColors.Saffron else KahaniColors.TextMuted,
                )
            }
            Spacer(Modifier.width(KahaniSpacing.sm))
            Column(Modifier.weight(1f)) {
                Text(
                    chapter.title,
                    style = KahaniType.CardTitle,
                    color = KahaniColors.TextPrimary,
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${chapter.durationSeconds / 60} ${strings.minutesShort} · ${chapter.wordCount} ${strings.wordsLabel}",
                    style = KahaniType.Micro,
                    color = KahaniColors.TextMuted,
                )
            }
            Spacer(Modifier.width(KahaniSpacing.xs))
            FormatButton(unlocked = unlocked, onClick = { onOpen(Format.TEXT) }) {
                TextFormatGlyph(tint = if (unlocked) KahaniColors.TextPrimary else KahaniColors.TextMuted)
            }
            Spacer(Modifier.width(KahaniSpacing.xxs))
            FormatButton(unlocked = unlocked, onClick = { onOpen(Format.AUDIO) }) {
                AudioFormatGlyph(tint = if (unlocked) KahaniColors.TextPrimary else KahaniColors.TextMuted)
            }
            Spacer(Modifier.width(KahaniSpacing.xs))
            when {
                chapter.isFreePreview -> MetaTag(strings.freeLabel)
                unlocked -> Box(Modifier.size(20.dp))
                else -> Row(
                    Modifier
                        .clip(RoundedCornerShape(KahaniRadius.pill))
                        .background(KahaniColors.Saffron)
                        .clickable { onOpen(Format.TEXT) }
                        .padding(horizontal = KahaniSpacing.xs, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LockGlyph(size = 12.dp, tint = KahaniColors.Maroon950)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${chapter.unlockCost}",
                        style = KahaniType.MicroBold,
                        color = KahaniColors.Maroon950,
                    )
                }
            }
        }

        if (sweep.value > 0f && sweep.value < 1f) {
            val shift = (sweep.value * 2f - 0.5f) * 1200f
            Box(
                Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(KahaniRadius.row))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                KahaniColors.Saffron.copy(alpha = 0.38f),
                                Color.Transparent,
                            ),
                            start = Offset(shift, 0f),
                            end = Offset(shift + 320f, 120f),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun FormatButton(unlocked: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(KahaniRadius.chip))
            .background(KahaniColors.Maroon700)
            .border(1.dp, KahaniColors.Maroon600, RoundedCornerShape(KahaniRadius.chip))
            .clickable(enabled = true, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
        if (!unlocked) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(KahaniColors.Maroon800.copy(alpha = 0.55f)),
            )
        }
    }
}

@Composable
private fun UnlockDialog(
    chapter: Chapter,
    balance: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val strings = LocalStrings.current
    Dialog(onDismissRequest = onDismiss) {
        KahaniCard(Modifier.fillMaxWidth(), elevatedSurface = true, contentPadding = KahaniSpacing.lg) {
            Column {
                Text(strings.unlockChapter, style = KahaniType.ChapterTitle, color = KahaniColors.TextPrimary)
                Spacer(Modifier.height(KahaniSpacing.xxs))
                Text(
                    "${chapter.chapterNumber}. ${chapter.title}",
                    style = KahaniType.UiBody,
                    color = KahaniColors.TextMuted,
                )
                Spacer(Modifier.height(KahaniSpacing.md))
                CostRow(strings.unlockCostLabel, "-${chapter.unlockCost}")
                Spacer(Modifier.height(KahaniSpacing.xs))
                CostRow(strings.balanceAfter, "${balance - chapter.unlockCost}")
                Spacer(Modifier.height(KahaniSpacing.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs)) {
                    GhostButton(strings.cancel, onDismiss, Modifier.weight(1f))
                    PrimaryButton(strings.confirmUnlock, onConfirm, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CostRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = KahaniType.UiBody, color = KahaniColors.TextMuted, modifier = Modifier.weight(1f))
        CoinGlyph(size = 14.dp)
        Spacer(Modifier.width(4.dp))
        Text(value, style = KahaniType.UiBold, color = KahaniColors.TextPrimary)
    }
}

@Composable
private fun NotEnoughCoinsDialog(onDismiss: () -> Unit, onTopUp: () -> Unit) {
    val strings = LocalStrings.current
    Dialog(onDismissRequest = onDismiss) {
        KahaniCard(Modifier.fillMaxWidth(), elevatedSurface = true, contentPadding = KahaniSpacing.lg) {
            Column {
                Text(strings.notEnoughCoins, style = KahaniType.ChapterTitle, color = KahaniColors.TextPrimary)
                Spacer(Modifier.height(KahaniSpacing.xxs))
                Text(strings.notEnoughCoinsBody, style = KahaniType.UiBody, color = KahaniColors.TextMuted)
                Spacer(Modifier.height(KahaniSpacing.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs)) {
                    GhostButton(strings.cancel, onDismiss, Modifier.weight(1f))
                    PrimaryButton(strings.topUp, onTopUp, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DetailSkeleton() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = KahaniSpacing.md),
    ) {
        Row {
            ShimmerBox(Modifier.size(width = 124.dp, height = 168.dp), cornerRadius = KahaniRadius.cover)
            Spacer(Modifier.width(KahaniSpacing.md))
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(KahaniSpacing.xs),
            ) {
                ShimmerBox(Modifier.fillMaxWidth(0.8f).height(22.dp), cornerRadius = 4.dp)
                ShimmerBox(Modifier.fillMaxWidth(0.5f).height(14.dp), cornerRadius = 4.dp)
                ShimmerBox(Modifier.fillMaxWidth(0.6f).height(14.dp), cornerRadius = 4.dp)
            }
        }
        Spacer(Modifier.height(KahaniSpacing.lg))
        repeat(5) {
            ShimmerBox(
                Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(bottom = KahaniSpacing.xxs),
            )
            Spacer(Modifier.height(KahaniSpacing.xxs))
        }
    }
}
