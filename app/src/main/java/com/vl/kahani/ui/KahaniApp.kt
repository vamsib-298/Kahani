package com.vl.kahani.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vl.kahani.data.Format
import com.vl.kahani.data.KahaniStore
import com.vl.kahani.data.LocalStore
import com.vl.kahani.data.LocalStrings
import com.vl.kahani.data.stringsFor
import com.vl.kahani.ui.components.HairlineDivider
import com.vl.kahani.ui.components.HomeGlyph
import com.vl.kahani.ui.components.LibraryGlyph
import com.vl.kahani.ui.components.SearchGlyph
import com.vl.kahani.ui.components.TunerGlyph
import com.vl.kahani.ui.components.WalletGlyph
import com.vl.kahani.ui.nav.LocalNavigator
import com.vl.kahani.ui.nav.Screen
import com.vl.kahani.ui.nav.rememberNavigator
import com.vl.kahani.ui.player.AudioPlayerScreen
import com.vl.kahani.ui.player.LocalPlayback
import com.vl.kahani.ui.player.MiniPlayer
import com.vl.kahani.ui.player.rememberPlaybackController
import com.vl.kahani.ui.screens.HomeScreen
import com.vl.kahani.ui.screens.LibraryScreen
import com.vl.kahani.ui.screens.NotificationsScreen
import com.vl.kahani.ui.screens.OnboardingFlow
import com.vl.kahani.ui.screens.ReaderScreen
import com.vl.kahani.ui.screens.SearchScreen
import com.vl.kahani.ui.screens.SeriesDetailScreen
import com.vl.kahani.ui.screens.SettingsScreen
import com.vl.kahani.ui.screens.WalletScreen
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType
import kotlinx.coroutines.delay

private const val TICK_MS = 200L

@Composable
fun KahaniApp() {
    val context = LocalContext.current
    val store = remember { KahaniStore(context.applicationContext) }
    val navigator = rememberNavigator()
    val playback = rememberPlaybackController()
    val strings = stringsFor(store.uiLanguage)

    var onboarded by remember { mutableStateOf(store.isOnboarded) }

    CompositionLocalProvider(
        LocalStore provides store,
        LocalStrings provides strings,
        LocalNavigator provides navigator,
        LocalPlayback provides playback,
    ) {
        // Simulated playback clock. Replaced wholesale when a real media player is wired in.
        LaunchedEffect(playback.isPlaying) {
            while (playback.isPlaying) {
                delay(TICK_MS)
                val finished = playback.tick(TICK_MS / 1000f)
                playback.series?.let { series ->
                    playback.chapter?.let { chapter ->
                        store.recordProgress(series.id, chapter.id, Format.AUDIO, playback.fraction)
                    }
                }
                if (finished) break
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(KahaniColors.Maroon900),
        ) {
            if (!onboarded) {
                OnboardingFlow(
                    onDone = { onboarded = true },
                    modifier = Modifier.systemBarsPadding(),
                )
            } else {
                val screen = navigator.current
                val immersive = screen is Screen.Reader

                BackHandler(enabled = playback.expanded || navigator.canGoBack) {
                    if (playback.expanded) playback.expanded = false else navigator.back()
                }

                Column(
                    Modifier
                        .fillMaxSize()
                        .then(if (immersive) Modifier else Modifier.systemBarsPadding()),
                ) {
                    Box(Modifier.weight(1f)) {
                        when (screen) {
                            Screen.Home -> HomeScreen()
                            Screen.Search -> SearchScreen()
                            Screen.Library -> LibraryScreen()
                            Screen.Wallet -> WalletScreen()
                            Screen.Settings -> SettingsScreen()
                            Screen.Notifications -> NotificationsScreen()
                            is Screen.SeriesDetail -> SeriesDetailScreen(screen.seriesId)
                            is Screen.Reader -> ReaderScreen(screen.seriesId, screen.chapterId)
                        }
                    }

                    if (!immersive) {
                        if (playback.isActive && !playback.expanded) {
                            HairlineDivider()
                            MiniPlayer()
                        }
                        HairlineDivider()
                        BottomBar(active = navigator.activeTab())
                    }
                }

                AnimatedVisibility(
                    visible = playback.expanded,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(KahaniColors.Maroon900)
                            .systemBarsPadding(),
                    ) { AudioPlayerScreen() }
                }
            }
        }
    }
}

@Composable
private fun BottomBar(active: Screen) {
    val navigator = LocalNavigator.current
    val strings = LocalStrings.current
    Row(
        Modifier
            .fillMaxWidth()
            .background(KahaniColors.Maroon900)
            .padding(vertical = KahaniSpacing.xxs),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BottomTab(strings.navHome, active == Screen.Home, { navigator.selectTab(Screen.Home) }) {
            HomeGlyph(tint = it)
        }
        BottomTab(strings.navSearch, active == Screen.Search, { navigator.selectTab(Screen.Search) }) {
            SearchGlyph(size = 20.dp, tint = it)
        }
        BottomTab(strings.navLibrary, active == Screen.Library, { navigator.selectTab(Screen.Library) }) {
            LibraryGlyph(tint = it)
        }
        BottomTab(strings.navWallet, active == Screen.Wallet, { navigator.selectTab(Screen.Wallet) }) {
            WalletGlyph(tint = it)
        }
        BottomTab(strings.navSettings, active == Screen.Settings, { navigator.selectTab(Screen.Settings) }) {
            TunerGlyph(tint = it)
        }
    }
}

@Composable
private fun BottomTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    glyph: @Composable (androidx.compose.ui.graphics.Color) -> Unit,
) {
    val tint = if (selected) KahaniColors.Saffron else KahaniColors.TextMuted
    Column(
        Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = KahaniSpacing.sm, vertical = KahaniSpacing.xs)
            .height(46.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        glyph(tint)
        Spacer(Modifier.height(3.dp))
        Text(label, style = KahaniType.MicroBold, color = tint, maxLines = 1)
    }
}
