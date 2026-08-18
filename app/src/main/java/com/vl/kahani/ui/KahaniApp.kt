package com.vl.kahani.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vl.kahani.data.Format
import com.vl.kahani.data.KahaniStore
import com.vl.kahani.data.LocalStore
import com.vl.kahani.data.LocalStrings
import com.vl.kahani.data.stringsFor
import com.vl.kahani.ui.components.*
import com.vl.kahani.ui.nav.LocalNavigator
import com.vl.kahani.ui.nav.Screen
import com.vl.kahani.ui.nav.rememberNavigator
import com.vl.kahani.ui.player.AudioPlayerScreen
import com.vl.kahani.ui.player.LocalPlayback
import com.vl.kahani.ui.player.MiniPlayer
import com.vl.kahani.ui.player.rememberPlaybackController
import com.vl.kahani.ui.screens.*
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType
import kotlinx.coroutines.delay

val LocalPipMode = staticCompositionLocalOf { false }

private const val TICK_MS = 200L

@Composable
fun KahaniApp(isInPipMode: Boolean = false) {
    val context = LocalContext.current
    val store = remember { KahaniStore(context.applicationContext) }
    val navigator = rememberNavigator()
    val playback = rememberPlaybackController()
    val strings = stringsFor(store.uiLanguage)

    LaunchedEffect(store.rewardMessage) {
        store.rewardMessage?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            store.rewardMessage = null
        }
    }

    CompositionLocalProvider(
        LocalStore provides store,
        LocalStrings provides strings,
        LocalNavigator provides navigator,
        LocalPlayback provides playback,
        LocalPipMode provides isInPipMode,
    ) {
        // Optimization: Use stable callbacks for tab selection
        val onTabSelected = remember(navigator) {
            { tab: Screen -> navigator.selectTab(tab) }
        }

        // Simulated playback clock.
        LaunchedEffect(playback.isPlaying) {
            while (playback.isPlaying) {
                delay(TICK_MS)
                val finished = playback.tick(TICK_MS / 1000f)
                playback.series?.let { series ->
                    store.addListenTime(TICK_MS / 1000f)
                    playback.chapter?.let { chapter ->
                        store.recordProgress(series.id, chapter.id, if (playback.audioUrl != null) Format.AUDIO else Format.TEXT, playback.fraction)
                    }
                }
                if (finished) break
            }
        }

        // Stop audio when entering reader mode
        LaunchedEffect(navigator.current) {
            if (navigator.current is Screen.Reader) {
                playback.pause()
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(KahaniColors.Maroon900),
        ) {
            when {
                !store.isAuthenticated -> {
                    LoginScreen(onLoginSuccess = { store.login(it) })
                }
                !store.isOnboarded -> {
                    OnboardingFlow(onDone = { store.completeOnboarding() }, modifier = Modifier.systemBarsPadding())
                }
                else -> {
                    val screen = navigator.current
                    val immersive = screen is Screen.Reader

                    BackHandler(enabled = playback.expanded || navigator.canGoBack) {
                        if (playback.expanded) playback.expanded = false else navigator.back()
                    }

                    Column(
                        Modifier.fillMaxSize().then(if (immersive) Modifier else Modifier.systemBarsPadding()),
                    ) {
                        Box(Modifier.weight(1f)) {
                            Crossfade(targetState = screen, label = "screenTransition") { currentScreen ->
                                when (currentScreen) {
                                    Screen.Home -> HomeScreen()
                                    Screen.Search -> SearchScreen()
                                    Screen.Upload -> UploadScreen()
                                    Screen.Library -> LibraryScreen()
                                    Screen.Profile -> ProfileScreen()
                                    Screen.Wallet -> WalletScreen()
                                    Screen.Notifications -> NotificationsScreen()
                                    Screen.Following -> FollowingScreen()
                                    is Screen.SeriesDetail -> SeriesDetailScreen(currentScreen.seriesId)
                                    is Screen.Reader -> ReaderScreen(currentScreen.seriesId, currentScreen.chapterId)
                                }
                            }
                        }

                        if (!immersive && !isInPipMode) {
                            if (playback.isActive && !playback.expanded) {
                                HairlineDivider()
                                MiniPlayer()
                            }
                            HairlineDivider()
                            BottomBar(
                                active = navigator.activeTab(),
                                onTabSelected = onTabSelected
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = playback.expanded && !isInPipMode,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                    ) {
                        Box(Modifier.fillMaxSize().background(KahaniColors.Maroon900).systemBarsPadding()) { 
                            AudioPlayerScreen() 
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBar(active: Screen, onTabSelected: (Screen) -> Unit) {
    val strings = LocalStrings.current
    Row(
        Modifier
            .fillMaxWidth()
            .background(KahaniColors.Maroon950) // Solid Black for responsiveness
            .padding(vertical = KahaniSpacing.xxs),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BottomTab(strings.navHome, active == Screen.Home, { onTabSelected(Screen.Home) }) {
            HomeGlyph(tint = it)
        }
        BottomTab(strings.navSearch, active == Screen.Search, { onTabSelected(Screen.Search) }) {
            SearchGlyph(size = 20.dp, tint = it)
        }
        BottomTab("Upload", active == Screen.Upload, { onTabSelected(Screen.Upload) }) {
            UploadGlyph(tint = it)
        }
        BottomTab(strings.navLibrary, active == Screen.Library, { onTabSelected(Screen.Library) }) {
            LibraryGlyph(tint = it)
        }
        BottomTab("Profile", active == Screen.Profile, { onTabSelected(Screen.Profile) }) {
            ProfileGlyph(tint = it)
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
