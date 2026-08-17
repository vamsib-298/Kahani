package com.vl.kahani.ui.screens

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vl.kahani.data.Format
import com.google.android.play.core.review.ReviewManagerFactory
import com.vl.kahani.data.LocalStore
import com.vl.kahani.data.LocalStrings
import com.vl.kahani.ui.components.CheckGlyph
import com.vl.kahani.ui.components.ChevronDirection
import com.vl.kahani.ui.components.ChevronGlyph
import com.vl.kahani.ui.components.ContrastGlyph
import com.vl.kahani.ui.components.GhostButton
import com.vl.kahani.ui.components.IconTapTarget
import com.vl.kahani.ui.components.PrimaryButton
import com.vl.kahani.ui.components.StateMessage
import com.vl.kahani.ui.nav.LocalNavigator
import com.vl.kahani.ui.nav.Screen
import com.vl.kahani.ui.player.LocalPlayback
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniRadius
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType

@Composable
fun ReaderScreen(seriesId: String, chapterId: String, modifier: Modifier = Modifier) {
    val store = LocalStore.current
    val strings = LocalStrings.current
    val nav = LocalNavigator.current
    val playback = LocalPlayback.current

    // Stop audio playback when entering reader mode
    LaunchedEffect(Unit) {
        if (playback.isPlaying && playback.audioUrl != null) {
            playback.togglePlay()
        }
    }

    val series = store.visibleSeries().firstOrNull { it.id == seriesId }
    val chapters = remember(seriesId) { store.chapters(seriesId) }
    var currentId by remember(chapterId) { mutableStateOf(chapterId) }
    val chapter = chapters.firstOrNull { it.id == currentId }

    if (series == null || chapter == null) {
        StateMessage(
            title = strings.errorTitle,
            body = strings.errorBody,
            actionLabel = strings.retry,
            onAction = { nav.back() },
            showRetryIcon = true,
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    val index = chapters.indexOf(chapter)
    val hasNext = index < chapters.lastIndex && store.isUnlocked(chapters[index + 1])
    val hasPrevious = index > 0

    var controlsVisible by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    val dayMode = store.readerDayMode
    val highContrast = store.highContrastMode
    
    // Manage system bar colors for Reader Mode
    val context = LocalContext.current
    DisposableEffect(dayMode) {
        val activity = context as? ComponentActivity
        activity?.enableEdgeToEdge(
            statusBarStyle = if (dayMode) SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) 
                            else SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = if (dayMode) SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                                else SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        onDispose {
            // Restore dark theme system bars when leaving Reader
            activity?.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            )
        }
    }

    val background = if (dayMode) KahaniColors.ReaderLightBg else KahaniColors.Maroon950
    val ink = when {
        dayMode -> KahaniColors.ReaderLightInk
        highContrast -> KahaniColors.HighContrastInk
        else -> KahaniColors.TextPrimary
    }
    val mutedInk = when {
        dayMode -> KahaniColors.ReaderLightInk.copy(alpha = 0.6f)
        highContrast -> KahaniColors.HighContrastMutedInk
        else -> KahaniColors.TextMuted
    }

    val readFraction by remember {
        derivedStateOf {
            val max = scrollState.maxValue
            if (max <= 0) 0f else scrollState.value.toFloat() / max
        }
    }

    LaunchedEffect(currentId) {
        snapshotFlow { readFraction }.collect { fraction ->
            store.recordProgress(seriesId, currentId, Format.TEXT, fraction)
            
            // Collect Google Play Review after reading one chapter
            // Show at last chapter or after 1 full chapter read
            val isLastChapter = index == chapters.size - 1
            if ((fraction > 0.95f || (isLastChapter && fraction > 0.5f)) && !store.hasRequestedPlayReview) {
                val manager = ReviewManagerFactory.create(context)
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        (context as? android.app.Activity)?.let { activity ->
                            manager.launchReviewFlow(activity, reviewInfo)
                            store.markPlayReviewRequested()
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(currentId) { scrollState.scrollTo(0) }

    val paragraphs = remember(chapter.id) { chapter.textContent.split("\n\n") }

    Box(
        modifier
            .fillMaxSize()
            .background(background)
            .pointerInput(currentId, hasNext, hasPrevious) {
                var drag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { drag = 0f },
                    onDragEnd = {
                        if (drag < -120f && hasNext) currentId = chapters[index + 1].id
                        if (drag > 120f && hasPrevious) currentId = chapters[index - 1].id
                    },
                ) { _, amount -> drag += amount }
            },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { controlsVisible = !controlsVisible }
                .padding(horizontal = KahaniSpacing.lg),
        ) {
            Spacer(Modifier.statusBarsPadding().height(88.dp))
            Text(
                text = "${strings.chapterLabel} ${chapter.chapterNumber}",
                style = KahaniType.MicroBold,
                color = if (dayMode) Color(0xFF6200EE) else KahaniColors.Saffron, // Deep Purple for Day, Amber for Night
            )
            Spacer(Modifier.height(KahaniSpacing.xxs))
            Text(chapter.title, style = KahaniType.ChapterTitle, color = if (dayMode) Color(0xFF333333) else Color.White)
            Spacer(Modifier.height(KahaniSpacing.lg))

            paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph,
                    style = KahaniType.readerBody(store.readerFontSize),
                    color = ink,
                )
                Spacer(Modifier.height(KahaniSpacing.md))
            }

            Spacer(Modifier.height(KahaniSpacing.xl))
            ChapterCompleteMoment(
                visible = readFraction > 0.97f,
                hasNext = hasNext,
                ink = ink,
                mutedInk = mutedInk,
                onNext = { if (hasNext) currentId = chapters[index + 1].id },
            )
            Spacer(Modifier.height(200.dp))
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(background.copy(alpha = 0.98f))
                    .systemBarsPadding()
                    .padding(horizontal = KahaniSpacing.xs, vertical = 0.dp), // Height reduced
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconTapTarget(onClick = { nav.back() }) {
                    ChevronGlyph(ChevronDirection.LEFT, tint = ink)
                }
                Column(Modifier.weight(1f)) {
                    Text(series.title, style = KahaniType.CardTitle, color = ink, maxLines = 1)
                    Text(
                        "${chapter.chapterNumber} / ${chapters.size}",
                        style = KahaniType.Micro,
                        color = mutedInk,
                    )
                }
                if (chapter.audioUrl != null) {
                    GhostButton(
                        text = strings.switchToAudio,
                        onClick = { playback.start(series, chapter, readFraction, chapter.audioUrl) },
                        contentColor = ink,
                    )
                }
                Spacer(Modifier.width(KahaniSpacing.xs))
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            ReaderControls(
                ink = ink,
                mutedInk = mutedInk,
                background = background,
                hasNext = hasNext,
                hasPrevious = hasPrevious,
                onPrevious = { if (hasPrevious) currentId = chapters[index - 1].id },
                onNext = { if (hasNext) currentId = chapters[index + 1].id },
                onOpenSeries = { nav.go(Screen.SeriesDetail(seriesId)) },
            )
        }
    }
}

@Composable
private fun ChapterCompleteMoment(
    visible: Boolean,
    hasNext: Boolean,
    ink: Color,
    mutedInk: Color,
    onNext: () -> Unit,
) {
    val strings = LocalStrings.current
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(KahaniRadius.pill))
                    .border(1.dp, KahaniColors.Saffron, RoundedCornerShape(KahaniRadius.pill)),
                contentAlignment = Alignment.Center,
            ) { CheckGlyph(size = 20.dp) }
            Spacer(Modifier.height(KahaniSpacing.xs))
            Text(strings.chapterComplete, style = KahaniType.SectionLabel, color = ink)
            Spacer(Modifier.height(KahaniSpacing.xxs))
            Text(strings.swipeForNext, style = KahaniType.Micro, color = mutedInk)
            if (hasNext) {
                Spacer(Modifier.height(KahaniSpacing.md))
                PrimaryButton(strings.nextChapter, onNext)
            }
        }
    }
}

@Composable
private fun ReaderControls(
    ink: Color,
    mutedInk: Color,
    background: Color,
    hasNext: Boolean,
    hasPrevious: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenSeries: () -> Unit,
) {
    val store = LocalStore.current
    Column(
        Modifier
            .fillMaxWidth()
            .background(background.copy(alpha = 0.98f))
            .systemBarsPadding()
            .padding(horizontal = KahaniSpacing.md, vertical = 0.dp), // Height reduced
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTapTarget(onClick = { store.updateReaderFontSize(store.readerFontSize - 1f) }) {
                    Text("A-", style = KahaniType.MicroBold, color = ink)
                }
                Text(
                    "${store.readerFontSize.toInt()}",
                    style = KahaniType.UiBold,
                    color = ink,
                    modifier = Modifier.padding(horizontal = KahaniSpacing.xxs)
                )
                IconTapTarget(onClick = { store.updateReaderFontSize(store.readerFontSize + 1f) }) {
                    Text("A+", style = KahaniType.UiBold, color = ink)
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTapTarget(onClick = { store.updateReaderDayMode(!store.readerDayMode) }) {
                    Text(if (store.readerDayMode) "🌙" else "☀️", style = KahaniType.UiBold)
                }
                Spacer(Modifier.width(KahaniSpacing.xs))
                IconTapTarget(onClick = { store.setHighContrast(!store.highContrastMode) }) {
                    ContrastGlyph(
                        size = 20.dp,
                        tint = if (store.highContrastMode) KahaniColors.Saffron else ink,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTapTarget(onClick = onPrevious) {
                    ChevronGlyph(
                        ChevronDirection.LEFT,
                        tint = if (hasPrevious) ink else mutedInk.copy(alpha = 0.4f),
                    )
                }
                IconTapTarget(onClick = onOpenSeries) {
                    Text("☰", style = KahaniType.UiBold, color = ink)
                }
                IconTapTarget(onClick = onNext) {
                    ChevronGlyph(
                        ChevronDirection.RIGHT,
                        tint = if (hasNext) ink else mutedInk.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }
}
