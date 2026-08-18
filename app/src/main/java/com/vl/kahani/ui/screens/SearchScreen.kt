package com.vl.kahani.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vl.kahani.data.AppLanguage
import com.vl.kahani.data.Genre
import com.vl.kahani.data.LocalStore
import com.vl.kahani.data.LocalStrings
import com.vl.kahani.data.SeriesStatus
import com.vl.kahani.ui.components.KahaniChip
import com.vl.kahani.ui.components.KahaniSearchField
import com.vl.kahani.ui.components.ScreenTitleBar
import com.vl.kahani.ui.components.RowSkeleton
import com.vl.kahani.ui.components.SectionHeader
import com.vl.kahani.ui.components.SeriesListRow
import com.vl.kahani.ui.components.StateMessage
import com.vl.kahani.ui.components.LoadState
import com.vl.kahani.ui.components.rememberLoader
import com.vl.kahani.ui.nav.LocalNavigator
import com.vl.kahani.ui.nav.Screen
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType

@Composable
fun SearchScreen(
    initialGenre: Genre? = null,
    initialLanguage: AppLanguage? = null,
    modifier: Modifier = Modifier
) {
    val store = LocalStore.current
    val strings = LocalStrings.current
    val nav = LocalNavigator.current
    val loader = rememberLoader()

    var query by remember { mutableStateOf("") }
    var genreFilter by remember { mutableStateOf(initialGenre) }
    var languageFilter by remember { mutableStateOf(initialLanguage) }
    var freeToStart by remember { mutableStateOf(false) }
    var completedOnly by remember { mutableStateOf(false) }
    var sortByMostPlayed by remember { mutableStateOf(false) }

    val hasFilters = genreFilter != null || languageFilter != null || freeToStart || completedOnly || sortByMostPlayed

    val results = store.visibleSeries().filter { series ->
        val q = query.trim().lowercase()
        val matchesQuery = q.isEmpty() ||
            series.title.lowercase().contains(q) ||
            series.synopsis.lowercase().contains(q) ||
            series.narratorName.lowercase().contains(q) ||
            strings.genre(series.genre).lowercase().contains(q) ||
            series.language.englishName.lowercase().contains(q)
        val matchesGenre = genreFilter == null || series.genre == genreFilter
        val matchesLanguage = languageFilter == null || series.language == languageFilter
        val matchesCompleted = !completedOnly || series.status == SeriesStatus.COMPLETED
        val matchesFree = !freeToStart || store.chapters(series.id).any { it.isFreePreview }
        matchesQuery && matchesGenre && matchesLanguage && matchesCompleted && matchesFree
    }.let { list ->
        if (sortByMostPlayed) list.sortedByDescending { it.playCount } else list
    }

    Column(modifier.fillMaxSize()) {
        ScreenTitleBar(
            title = when {
                genreFilter != null && languageFilter != null -> "${strings.genre(genreFilter!!)} · ${languageFilter?.nativeName}"
                genreFilter != null -> strings.genre(genreFilter!!)
                languageFilter != null -> languageFilter?.nativeName ?: strings.navSearch
                else -> strings.navSearch
            }
        )
        Column(Modifier.padding(horizontal = KahaniSpacing.md, vertical = KahaniSpacing.xs)) {
            KahaniSearchField(
                value = query,
                onValueChange = { query = it },
                hint = strings.searchHint,
                onSubmit = { store.rememberSearch(query) },
            )
            Spacer(Modifier.height(KahaniSpacing.sm))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs),
            ) {
                KahaniChip(
                    label = genreFilter?.let { strings.genre(it) } ?: strings.filterGenre,
                    selected = genreFilter != null,
                    onClick = {
                        val all = Genre.entries.filter { !store.familySafeMode || !it.isMature }
                        val idx = all.indexOf(genreFilter)
                        genreFilter = if (idx == all.lastIndex) null else all[idx + 1]
                    },
                )
                KahaniChip(
                    label = languageFilter?.nativeName ?: strings.filterLanguage,
                    selected = languageFilter != null,
                    onClick = {
                        val all = AppLanguage.entries.toList()
                        val idx = all.indexOf(languageFilter)
                        languageFilter = if (idx == all.lastIndex) null else all[idx + 1]
                    },
                )
                KahaniChip(
                    label = strings.filterFreeToStart,
                    selected = freeToStart,
                    onClick = { freeToStart = !freeToStart },
                )
                KahaniChip(
                    label = strings.filterCompleted,
                    selected = completedOnly,
                    onClick = { completedOnly = !completedOnly },
                )
                KahaniChip(
                    label = "Most Played",
                    selected = sortByMostPlayed,
                    onClick = { sortByMostPlayed = !sortByMostPlayed },
                )
                if (hasFilters) {
                    KahaniChip(
                        label = strings.clearFilters,
                        onClick = {
                            genreFilter = null
                            languageFilter = null
                            freeToStart = false
                            completedOnly = false
                            sortByMostPlayed = false
                        },
                    )
                }
            }
        }

        when (loader.state) {
            LoadState.LOADING -> Column(
                Modifier.padding(horizontal = KahaniSpacing.md),
                verticalArrangement = Arrangement.spacedBy(KahaniSpacing.xs),
            ) { repeat(5) { RowSkeleton() } }

            LoadState.ERROR -> StateMessage(
                title = strings.errorTitle,
                body = strings.errorBody,
                actionLabel = strings.retry,
                onAction = { loader.retry() },
                showRetryIcon = true,
            )

            LoadState.CONTENT -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = KahaniSpacing.md,
                    end = KahaniSpacing.md,
                    bottom = KahaniSpacing.xxl,
                ),
            ) {
                if (query.isBlank() && !hasFilters && store.recentSearches.isNotEmpty()) {
                    item {
                        SectionHeader(
                            label = strings.recentSearches,
                            modifier = Modifier.padding(vertical = KahaniSpacing.xs),
                            trailing = {
                                Text(
                                    strings.clearFilters,
                                    style = KahaniType.Micro,
                                    color = KahaniColors.Saffron,
                                    modifier = Modifier
                                        .clickable { store.clearRecentSearches() }
                                        .padding(KahaniSpacing.xs),
                                )
                            },
                        )
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs)) {
                            items(store.recentSearches.size) { i ->
                                val term = store.recentSearches[i]
                                KahaniChip(label = term, onClick = { query = term })
                            }
                        }
                    }
                    item { Spacer(Modifier.height(KahaniSpacing.md)) }
                }

                if (results.isNotEmpty() && (hasFilters || query.isNotBlank())) {
                    item {
                        Text(
                            text = "${results.size} stories found",
                            style = KahaniType.Micro,
                            color = KahaniColors.TextMuted,
                            modifier = Modifier.padding(vertical = KahaniSpacing.xs)
                        )
                    }
                }

                if (results.isEmpty()) {
                    item {
                        StateMessage(
                            title = strings.noResultsTitle,
                            body = strings.noResultsBody,
                        )
                    }
                    item {
                        SectionHeader(
                            label = strings.browseGenres,
                            modifier = Modifier.padding(bottom = KahaniSpacing.sm),
                        )
                    }
                    val genres = Genre.entries.filter { !store.familySafeMode || !it.isMature }
                    items(genres.chunked(2).size) { rowIndex ->
                        val pair = genres.chunked(2)[rowIndex]
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = KahaniSpacing.xs),
                            horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs),
                        ) {
                            pair.forEach { g ->
                                KahaniChip(
                                    label = strings.genre(g),
                                    modifier = Modifier.weight(1f),
                                    onClick = { genreFilter = g },
                                )
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                } else {
                    items(results.size) { index ->
                        val series = results[index]
                        SeriesListRow(
                            series = series,
                            onClick = {
                                store.rememberSearch(query)
                                nav.go(Screen.SeriesDetail(series.id))
                            },
                        )
                    }
                }
            }
        }
    }
}
