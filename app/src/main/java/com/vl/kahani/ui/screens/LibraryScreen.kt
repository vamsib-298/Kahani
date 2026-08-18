package com.vl.kahani.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vl.kahani.data.LocalStore
import com.vl.kahani.data.LocalStrings
import com.vl.kahani.data.SeedCatalog
import com.vl.kahani.data.Series
import com.vl.kahani.ui.components.IconTapTarget
import com.vl.kahani.ui.components.KahaniChip
import com.vl.kahani.ui.components.LoadState
import com.vl.kahani.ui.components.RowSkeleton
import com.vl.kahani.ui.components.ScreenTitleBar
import com.vl.kahani.ui.components.SectionHeader
import com.vl.kahani.ui.components.SeriesListRow
import com.vl.kahani.ui.components.StateMessage
import com.vl.kahani.ui.components.TrashGlyph
import com.vl.kahani.ui.components.rememberLoader
import com.vl.kahani.ui.nav.LocalNavigator
import com.vl.kahani.ui.nav.Screen
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType

private enum class LibraryTab { IN_PROGRESS, COMPLETED, SAVED, DOWNLOADS }

@Composable
fun LibraryScreen(modifier: Modifier = Modifier) {
    val store = LocalStore.current
    val strings = LocalStrings.current
    val nav = LocalNavigator.current
    val loader = rememberLoader()
    var tab by remember { mutableStateOf(LibraryTab.IN_PROGRESS) }

    val items: List<Series> = remember(tab, store.lastProgressUpdate, store.catalog.size) {
        when (tab) {
            LibraryTab.IN_PROGRESS -> store.inProgressSeries()
            LibraryTab.COMPLETED -> store.completedSeries()
            LibraryTab.SAVED -> store.savedSeriesIds.mapNotNull { id -> store.catalog.firstOrNull { it.id == id } }
            LibraryTab.DOWNLOADS -> emptyList()
        }
    }

    Column(modifier.fillMaxSize()) {
        ScreenTitleBar(title = strings.navLibrary)

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = KahaniSpacing.md, vertical = KahaniSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs),
        ) {
            KahaniChip(
                label = strings.inProgress,
                selected = tab == LibraryTab.IN_PROGRESS,
                onClick = { tab = LibraryTab.IN_PROGRESS },
            )
            KahaniChip(
                label = strings.completed,
                selected = tab == LibraryTab.COMPLETED,
                onClick = { tab = LibraryTab.COMPLETED },
            )
            KahaniChip(
                label = strings.savedTab,
                selected = tab == LibraryTab.SAVED,
                onClick = { tab = LibraryTab.SAVED },
            )
            KahaniChip(
                label = strings.downloads,
                selected = tab == LibraryTab.DOWNLOADS,
                onClick = { tab = LibraryTab.DOWNLOADS },
            )
        }

        when (loader.state) {
            LoadState.LOADING -> Column(
                Modifier.padding(horizontal = KahaniSpacing.md),
            ) { repeat(4) { RowSkeleton() } }

            LoadState.ERROR -> StateMessage(
                title = strings.errorTitle,
                body = strings.errorBody,
                actionLabel = strings.retry,
                onAction = { loader.retry() },
                showRetryIcon = true,
            )

            LoadState.CONTENT -> if (tab == LibraryTab.DOWNLOADS) {
                DownloadsTab()
            } else if (items.isEmpty()) {
                StateMessage(
                    title = strings.libraryEmptyTitle,
                    body = strings.libraryEmptyBody,
                    actionLabel = strings.browseGenres,
                    onAction = { nav.selectTab(Screen.Search()) },
                )
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = KahaniSpacing.md,
                        end = KahaniSpacing.md,
                        bottom = KahaniSpacing.xxl,
                    ),
                ) {
                    items(items.size) { index ->
                        val series = items[index]
                        SeriesListRow(
                            series = series,
                            onClick = { nav.go(Screen.SeriesDetail(series.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadsTab() {
    val store = LocalStore.current
    val strings = LocalStrings.current
    val nav = LocalNavigator.current

    if (store.downloads.isEmpty()) {
        StateMessage(
            title = strings.libraryEmptyTitle,
            body = strings.offlineReady + " — " + strings.libraryEmptyBody,
        )
        return
    }

    val megabytes = store.totalDownloadBytes() / 1_000_000.0

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = KahaniSpacing.md,
            end = KahaniSpacing.md,
            bottom = KahaniSpacing.xxl,
        ),
    ) {
        item {
            SectionHeader(
                label = "${((megabytes * 10).toInt() / 10.0)} MB ${strings.storageUsed}",
                modifier = Modifier.padding(vertical = KahaniSpacing.xs),
            )
        }
        items(store.downloads.size) { index ->
            val download = store.downloads[index]
            val series = store.catalog.firstOrNull { it.id == download.seriesId } ?: return@items
            val chapter = store.chapters(download.seriesId)
                .firstOrNull { it.id == download.chapterId }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    SeriesListRow(
                        series = series,
                        onClick = { nav.go(Screen.SeriesDetail(series.id)) },
                    )
                    Text(
                        text = "${chapter?.title ?: ""} · ${download.sizeBytes / 1_000_000.0} MB",
                        style = KahaniType.Micro,
                        color = KahaniColors.TextMuted,
                        modifier = Modifier.padding(bottom = KahaniSpacing.xs),
                    )
                }
                if (chapter != null) {
                    IconTapTarget(onClick = { store.toggleDownload(chapter, download.format) }) {
                        TrashGlyph()
                    }
                }
            }
            Spacer(Modifier.height(1.dp))
        }
    }
}
