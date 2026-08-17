package com.vl.kahani.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vl.kahani.data.Genre
import com.vl.kahani.data.LocalStore
import com.vl.kahani.data.LocalStrings
import com.vl.kahani.data.Series
import com.vl.kahani.ui.components.BellGlyph
import com.vl.kahani.ui.components.CoinPill
import com.vl.kahani.ui.components.IconTapTarget
import com.vl.kahani.ui.components.LoadState
import com.vl.kahani.ui.components.PosterSkeleton
import com.vl.kahani.ui.components.PullToRefreshBox
import com.vl.kahani.ui.components.SearchGlyph
import com.vl.kahani.ui.components.SectionHeader
import com.vl.kahani.ui.components.SeriesPosterCard
import com.vl.kahani.ui.components.StateMessage
import com.vl.kahani.ui.components.rememberLoader
import com.vl.kahani.ui.nav.LocalNavigator
import com.vl.kahani.ui.nav.Screen
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType
import com.vl.kahani.ui.theme.Narrative
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val store = LocalStore.current
    val strings = LocalStrings.current
    val nav = LocalNavigator.current
    val loader = rememberLoader()
    var refreshing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(900)
            refreshing = false
        }
    }

    val catalog = store.visibleSeries()
    val continueSeries = store.inProgressSeries()
    val picks = catalog.filter { it.isEditorsPick }
    val fresh = catalog.filter { it.isNewThisWeek }
    val interestGenres = store.genreInterests.ifEmpty { Genre.entries.toList() }

    Column(modifier.fillMaxSize()) {
        HomeTopBar()

        when {
            loader.state == LoadState.LOADING || store.isCatalogLoading -> HomeSkeleton()

            loader.state == LoadState.ERROR -> StateMessage(
                title = strings.errorTitle,
                body = strings.errorBody,
                actionLabel = strings.retry,
                onAction = { loader.retry() },
                showRetryIcon = true,
            )

            else -> PullToRefreshBox(
                listState = listState,
                isRefreshing = refreshing,
                onRefresh = { refreshing = true },
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = KahaniSpacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(KahaniSpacing.lg),
                ) {
                    if (continueSeries.isNotEmpty()) {
                        item {
                            Shelf(
                                label = strings.continueReading,
                                series = continueSeries,
                                progressFor = { store.progress[it.id]?.fraction },
                                onOpen = { nav.go(Screen.SeriesDetail(it.id)) },
                            )
                        }
                    }
                    
                    // User Preferences at top
                    interestGenres.forEach { genre ->
                        val inGenre = catalog.filter { it.genre == genre }
                        if (inGenre.isNotEmpty()) {
                            item(key = genre.key) {
                                Shelf(
                                    label = strings.genre(genre),
                                    series = inGenre,
                                    onOpen = { nav.go(Screen.SeriesDetail(it.id)) },
                                )
                            }
                        }
                    }

                    if (picks.isNotEmpty()) {
                        item {
                            Shelf(
                                label = strings.editorsPicks,
                                series = picks,
                                onOpen = { nav.go(Screen.SeriesDetail(it.id)) },
                            )
                        }
                    }
                    if (fresh.isNotEmpty()) {
                        item {
                            Shelf(
                                label = strings.newThisWeek,
                                series = fresh,
                                onOpen = { nav.go(Screen.SeriesDetail(it.id)) },
                            )
                        }
                    }
                    
                    // Other Categories below
                    Genre.entries.filter { it !in interestGenres }.forEach { genre ->
                        val inGenre = catalog.filter { it.genre == genre }
                        if (inGenre.isNotEmpty()) {
                            item(key = "other_${genre.key}") {
                                Shelf(
                                    label = strings.genre(genre),
                                    series = inGenre,
                                    onOpen = { nav.go(Screen.SeriesDetail(it.id)) },
                                )
                            }
                        }
                    }
                    if (catalog.isEmpty()) {
                        item {
                            Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                StateMessage(
                                    title = "Your Story Begins Here",
                                    body = "The world of Kahani is currently being curated. Our storytellers are preparing thousands of chapters just for you. Check back very soon!",
                                )
                            }
                        }
                    }
                    
                    item {
                        Column(
                            Modifier.fillMaxWidth().padding(top = KahaniSpacing.xxl),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Made with 💙 in India",
                                style = KahaniType.MicroBold,
                                color = KahaniColors.TextMuted.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar() {
    val store = LocalStore.current
    val strings = LocalStrings.current
    val nav = LocalNavigator.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = KahaniSpacing.md, vertical = KahaniSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = strings.appName,
            fontFamily = Narrative,
            style = KahaniType.SeriesTitle,
            color = KahaniColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        Box {
            IconTapTarget(onClick = { nav.go(Screen.Notifications) }) { BellGlyph() }
            if (store.unreadNotificationCount > 0) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp)
                        .size(8.dp)
                        .background(KahaniColors.Saffron, CircleShape)
                        .border(1.dp, KahaniColors.Maroon900, CircleShape),
                )
            }
        }
        IconTapTarget(onClick = { nav.selectTab(Screen.Search) }) {
            SearchGlyph(size = 20.dp, tint = KahaniColors.TextPrimary)
        }
        Spacer(Modifier.width(KahaniSpacing.xxs))
        CoinPill(balance = store.coinBalance, onClick = { nav.selectTab(Screen.Wallet) })
    }
}

@Composable
private fun Shelf(
    label: String,
    series: List<Series>,
    onOpen: (Series) -> Unit,
    modifier: Modifier = Modifier,
    progressFor: (Series) -> Float? = { null },
) {
    Column(modifier.fillMaxWidth()) {
        SectionHeader(
            label = label,
            modifier = Modifier.padding(horizontal = KahaniSpacing.md),
        )
        Spacer(Modifier.height(KahaniSpacing.sm))
        LazyRow(
            contentPadding = PaddingValues(horizontal = KahaniSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.sm),
        ) {
            items(series.size) { index ->
                val item = series[index]
                SeriesPosterCard(
                    series = item,
                    onClick = { onOpen(item) },
                    progressFraction = progressFor(item),
                )
            }
        }
    }
}

@Composable
private fun HomeSkeleton() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = KahaniSpacing.md),
        verticalArrangement = Arrangement.spacedBy(KahaniSpacing.lg),
    ) {
        repeat(3) {
            Column {
                com.vl.kahani.ui.components.ShimmerBox(
                    Modifier
                        .width(120.dp)
                        .height(14.dp),
                    cornerRadius = 4.dp,
                )
                Spacer(Modifier.height(KahaniSpacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.sm)) {
                    repeat(3) { PosterSkeleton() }
                }
            }
        }
    }
}
