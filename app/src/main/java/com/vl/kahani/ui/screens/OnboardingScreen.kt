package com.vl.kahani.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vl.kahani.data.AppLanguage
import com.vl.kahani.data.Genre
import com.vl.kahani.data.LocalStore
import com.vl.kahani.data.LocalStrings
import com.vl.kahani.data.SeedCatalog
import com.vl.kahani.ui.components.KahaniChip
import com.vl.kahani.ui.components.PrimaryButton
import com.vl.kahani.ui.components.SeriesPosterCard
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniRadius
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType
import com.vl.kahani.ui.theme.Narrative
import kotlinx.coroutines.delay

@Composable
fun OnboardingFlow(onDone: () -> Unit, modifier: Modifier = Modifier) {
    val store = LocalStore.current
    val strings = LocalStrings.current
    var step by remember(store.onboardingStep) { mutableIntStateOf(store.onboardingStep) }

    Box(
        modifier
            .fillMaxSize()
            .background(KahaniColors.Maroon900),
    ) {

        AnimatedVisibility(visible = step == 1, enter = fadeIn(), exit = fadeOut()) {
            OnboardingStep(
                title = strings.chooseLanguageTitle,
                body = strings.chooseLanguageBody,
                ctaLabel = strings.continueLabel,
                ctaEnabled = store.contentLanguages.isNotEmpty(),
                onCta = { 
                    store.updateOnboardingStep(2)
                    step = 2 
                },
            ) {
                ChipGrid(
                    items = listOf(AppLanguage.TELUGU, AppLanguage.HINDI, AppLanguage.ENGLISH),
                    columns = 1,
                    label = { it.nativeName },
                    selected = { store.contentLanguages.contains(it) },
                    onToggle = { 
                        store.contentLanguages.clear()
                        store.toggleContentLanguage(it) 
                    },
                )
            }
        }

        AnimatedVisibility(visible = step == 2, enter = fadeIn(), exit = fadeOut()) {
            OnboardingStep(
                title = strings.chooseGenreTitle,
                body = strings.chooseGenreBody,
                ctaLabel = if (store.genreInterests.size >= 3) {
                    strings.continueLabel
                } else {
                    strings.pickAtLeastThree
                },
                ctaEnabled = store.genreInterests.size >= 3,
                onCta = { 
                    store.updateOnboardingStep(3)
                    step = 3 
                },
            ) {
                ChipGrid(
                    items = Genre.entries.toList(),
                    columns = 2,
                    label = { strings.genre(it) },
                    selected = { store.genreInterests.contains(it) },
                    onToggle = { store.toggleGenreInterest(it) },
                )
            }
        }

        AnimatedVisibility(visible = step == 3, enter = fadeIn(), exit = fadeOut()) {
            val picks = remember(store.catalog.size) {
                store.catalog
                    .filter { it.onboardingRank != null && it.publishStatus == "PUBLISHED" }
                    .sortedBy { it.onboardingRank }
                    .take(4)
            }
            var selectedPick by remember { mutableStateOf<String?>(null) }

            OnboardingStep(
                title = strings.starterPicksTitle,
                body = strings.starterPicksBody,
                ctaLabel = strings.enterKahani,
                ctaEnabled = selectedPick != null,
                onCta = {
                    store.completeOnboarding()
                    onDone()
                },
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.sm),
                ) {
                    picks.take(2).forEach { series ->
                        val isSelected = selectedPick == series.id
                        Box(Modifier.weight(1f)) {
                            SeriesPosterCard(
                                series = series,
                                onClick = { selectedPick = series.id },
                                modifier = Modifier.fillMaxWidth(),
                                width = null,
                                showMetadata = true,
                                isSelected = isSelected,
                            )
                            if (isSelected) {
                                Box(
                                    Modifier
                                        .padding(KahaniSpacing.xs)
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(KahaniColors.Saffron, RoundedCornerShape(KahaniRadius.pill)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✓", color = KahaniColors.Maroon900, style = KahaniType.MicroBold)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(KahaniSpacing.sm))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.sm),
                ) {
                    picks.drop(2).forEach { series ->
                        val isSelected = selectedPick == series.id
                        Box(Modifier.weight(1f)) {
                            SeriesPosterCard(
                                series = series,
                                onClick = { selectedPick = series.id },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (isSelected) Modifier.border(2.dp, androidx.compose.ui.graphics.Color.Yellow, RoundedCornerShape(KahaniRadius.cover)) else Modifier),
                                width = null,
                                showMetadata = true,
                            )
                            if (isSelected) {
                                Box(
                                    Modifier
                                        .padding(KahaniSpacing.xs)
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(KahaniColors.Saffron, RoundedCornerShape(KahaniRadius.pill)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✓", color = KahaniColors.Maroon900, style = KahaniType.MicroBold)
                                }
                            }
                        }
                    }
                    if (picks.size < 4) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun OnboardingStep(
    title: String,
    body: String,
    ctaLabel: String,
    ctaEnabled: Boolean,
    onCta: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = KahaniSpacing.lg,
                end = KahaniSpacing.lg,
                top = KahaniSpacing.xxl,
            ),
        ) {
            item {
                Text(title, style = KahaniType.SeriesTitle, color = KahaniColors.TextPrimary)
                Spacer(Modifier.height(KahaniSpacing.xs))
                Text(body, style = KahaniType.UiBody, color = KahaniColors.TextMuted)
                Spacer(Modifier.height(KahaniSpacing.xl))
            }
            item { content() }
            item { Spacer(Modifier.height(KahaniSpacing.xxl)) }
        }
        PrimaryButton(
            text = ctaLabel,
            onClick = onCta,
            enabled = ctaEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(KahaniSpacing.lg),
        )
    }
}

@Composable
private fun <T> ChipGrid(
    items: List<T>,
    columns: Int,
    label: (T) -> String,
    selected: (T) -> Boolean,
    onToggle: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(KahaniSpacing.xs)) {
        items.chunked(columns).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs),
            ) {
                row.forEach { item ->
                    KahaniChip(
                        label = label(item),
                        modifier = Modifier.weight(1f),
                        selected = selected(item),
                        onClick = { onToggle(item) },
                    )
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
