package com.vl.kahani.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vl.kahani.data.Format
import com.vl.kahani.data.LocalStore
import com.vl.kahani.data.LocalStrings
import com.vl.kahani.data.NarrationType
import com.vl.kahani.ui.components.ChevronDirection
import com.vl.kahani.ui.components.ChevronGlyph
import com.vl.kahani.ui.components.CoverArt
import com.vl.kahani.ui.components.DownloadGlyph
import com.vl.kahani.ui.components.IconTapTarget
import com.vl.kahani.ui.components.KahaniChip
import com.vl.kahani.ui.components.PauseGlyph
import com.vl.kahani.ui.components.PlayGlyph
import com.vl.kahani.ui.components.SkipGlyph
import com.vl.kahani.ui.components.SpeedGlyph
import com.vl.kahani.ui.components.TextFormatGlyph
import com.vl.kahani.ui.components.TimerGlyph
import com.vl.kahani.ui.nav.LocalNavigator
import com.vl.kahani.ui.nav.Screen
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniRadius
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType

private val SPEEDS = listOf(0.8f, 1f, 1.25f, 1.5f, 1.75f, 2f)
private val SLEEP_MINUTES = listOf(15, 30, 45, 60)

@Composable
fun AudioPlayerScreen(modifier: Modifier = Modifier) {
    val playback = LocalPlayback.current
    val store = LocalStore.current
    val strings = LocalStrings.current
    val navigator = LocalNavigator.current
    val series = playback.series
    val chapter = playback.chapter
    if (series == null || chapter == null) return

    var showSpeed by remember { mutableStateOf(false) }
    var showSleep by remember { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxSize()
            .background(KahaniColors.Maroon900)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 24f) playback.expanded = false
                }
            }
            .padding(horizontal = KahaniSpacing.lg),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = KahaniSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTapTarget(onClick = { playback.expanded = false }) {
                ChevronGlyph(ChevronDirection.DOWN)
            }
            Text(
                strings.nowPlaying,
                style = KahaniType.MicroBold,
                color = KahaniColors.TextMuted,
                modifier = Modifier.weight(1f),
            )
            IconTapTarget(onClick = { store.toggleDownload(chapter, Format.AUDIO) }) {
                DownloadGlyph(
                    complete = store.isDownloaded(chapter.id),
                    tint = if (store.isDownloaded(chapter.id)) KahaniColors.Saffron else KahaniColors.TextMuted,
                )
            }
        }

        Spacer(Modifier.height(KahaniSpacing.lg))
        CoverArt(
            series = series,
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .aspectRatio(0.84f)
                .align(Alignment.CenterHorizontally),
            showTitle = false,
        )

        Spacer(Modifier.height(KahaniSpacing.xl))
        Text(
            "${strings.chapterLabel} ${chapter.chapterNumber} · ${chapter.title}",
            style = KahaniType.ChapterTitle,
            color = KahaniColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(KahaniSpacing.xxs))
        Text(series.title, style = KahaniType.UiBody, color = KahaniColors.TextMuted, maxLines = 1)
        Spacer(Modifier.height(KahaniSpacing.xs))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(KahaniRadius.pill))
                    .background(KahaniColors.Saffron),
            )
            Spacer(Modifier.width(KahaniSpacing.xs))
            Text(
                text = if (series.narrationType == NarrationType.HUMAN) {
                    "${strings.humanNarrated} · ${series.narratorName}"
                } else {
                    "${strings.aiNarrated} · ${series.narratorName}"
                },
                style = KahaniType.Micro,
                color = KahaniColors.TextMuted,
            )
        }

        Spacer(Modifier.height(KahaniSpacing.lg))
        Scrubber(
            fraction = playback.fraction,
            onSeekFraction = { playback.seekTo(it * playback.durationSeconds) },
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                formatClock(playback.positionSeconds),
                style = KahaniType.Micro,
                color = KahaniColors.TextMuted,
            )
            Text(
                formatClock(playback.durationSeconds),
                style = KahaniType.Micro,
                color = KahaniColors.TextMuted,
            )
        }

        Spacer(Modifier.height(KahaniSpacing.md))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTapTarget(onClick = { playback.skip(-15) }) { SkipGlyph(forward = false) }
            Spacer(Modifier.width(KahaniSpacing.lg))
            Box(
                Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(KahaniRadius.pill))
                    .background(KahaniColors.Saffron)
                    .clickable { playback.togglePlay() },
                contentAlignment = Alignment.Center,
            ) {
                if (playback.isPlaying) PauseGlyph(size = 26.dp) else PlayGlyph(size = 26.dp)
            }
            Spacer(Modifier.width(KahaniSpacing.lg))
            IconTapTarget(onClick = { playback.skip(15) }) { SkipGlyph(forward = true) }
        }

        Spacer(Modifier.height(KahaniSpacing.lg))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs),
        ) {
            KahaniChip(
                label = "${playback.speed}x",
                selected = showSpeed,
                onClick = {
                    showSpeed = !showSpeed
                    showSleep = false
                },
                leading = {
                    SpeedGlyph(
                        size = 14.dp,
                        tint = if (showSpeed) KahaniColors.Maroon950 else KahaniColors.TextMuted,
                    )
                },
            )
            KahaniChip(
                label = sleepLabel(playback, strings.timerOff, strings.endOfChapter, strings.minutesShort),
                selected = showSleep,
                onClick = {
                    showSleep = !showSleep
                    showSpeed = false
                },
                leading = {
                    TimerGlyph(
                        size = 14.dp,
                        tint = if (showSleep) KahaniColors.Maroon950 else KahaniColors.TextMuted,
                    )
                },
            )
            KahaniChip(
                label = strings.switchToText,
                onClick = {
                    playback.expanded = false
                    navigator.go(Screen.Reader(series.id, chapter.id))
                },
                leading = { TextFormatGlyph(size = 13.dp) },
            )
        }

        if (showSpeed) {
            Spacer(Modifier.height(KahaniSpacing.sm))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs),
            ) {
                SPEEDS.forEach { speed ->
                    KahaniChip(
                        label = "${speed}x",
                        selected = playback.speed == speed,
                        onClick = { playback.updateSpeed(speed) },
                    )
                }
            }
        }

        if (showSleep) {
            Spacer(Modifier.height(KahaniSpacing.sm))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs),
            ) {
                KahaniChip(
                    label = strings.timerOff,
                    selected = playback.sleepTimerMinutes == null && !playback.sleepStopAtChapterEnd,
                    onClick = { playback.clearSleepTimer() },
                )
                SLEEP_MINUTES.forEach { minutes ->
                    KahaniChip(
                        label = "$minutes ${strings.minutesShort}",
                        selected = playback.sleepTimerMinutes == minutes,
                        onClick = { playback.setSleepTimer(minutes) },
                    )
                }
                KahaniChip(
                    label = strings.endOfChapter,
                    selected = playback.sleepStopAtChapterEnd,
                    onClick = { playback.setSleepAtChapterEnd() },
                )
            }
        }

        Spacer(Modifier.height(KahaniSpacing.lg))
    }
}

private fun sleepLabel(
    playback: PlaybackController,
    off: String,
    endOfChapter: String,
    minutesShort: String,
): String = when {
    playback.sleepStopAtChapterEnd -> endOfChapter
    playback.sleepTimerMinutes != null -> "${(playback.sleepSecondsRemaining / 60).toInt() + 1} $minutesShort"
    else -> off
}

@Composable
private fun Scrubber(fraction: Float, onSeekFraction: (Float) -> Unit) {
    var width by remember { mutableIntStateOf(1) }
    Box(
        Modifier
            .fillMaxWidth()
            .height(28.dp)
            .onSizeChanged { width = it.width.coerceAtLeast(1) }
            .pointerInput(Unit) {
                detectTapGestures { offset -> onSeekFraction((offset.x / width).coerceIn(0f, 1f)) }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    onSeekFraction((change.position.x / width).coerceIn(0f, 1f))
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(KahaniColors.Maroon600),
        )
        Box(
            Modifier
                .fillMaxWidth(fraction)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(KahaniColors.Saffron),
        )
        Box(
            Modifier
                .fillMaxWidth(fraction)
                .height(28.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(KahaniRadius.pill))
                    .background(KahaniColors.Saffron)
                    .border(2.dp, KahaniColors.Maroon900, RoundedCornerShape(KahaniRadius.pill)),
            )
        }
    }
}

@Composable
fun MiniPlayer(modifier: Modifier = Modifier) {
    val playback = LocalPlayback.current
    val series = playback.series ?: return
    val chapter = playback.chapter ?: return

    Column(
        modifier
            .fillMaxWidth()
            .background(KahaniColors.Maroon800)
            .clickable { playback.expanded = true },
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(KahaniColors.Maroon600),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(playback.fraction)
                    .height(2.dp)
                    .background(KahaniColors.Saffron),
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = KahaniSpacing.sm, vertical = KahaniSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoverArt(
                series = series,
                modifier = Modifier.size(38.dp),
                cornerRadius = KahaniRadius.chip,
                showTitle = false,
            )
            Spacer(Modifier.width(KahaniSpacing.sm))
            Column(Modifier.weight(1f)) {
                Text(
                    chapter.title,
                    style = KahaniType.CardTitle,
                    color = KahaniColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    series.title,
                    style = KahaniType.Micro,
                    color = KahaniColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconTapTarget(onClick = { playback.togglePlay() }) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(KahaniRadius.pill))
                        .background(KahaniColors.Saffron),
                    contentAlignment = Alignment.Center,
                ) {
                    if (playback.isPlaying) PauseGlyph(size = 14.dp) else PlayGlyph(size = 14.dp)
                }
            }
        }
    }
}
