package com.vl.kahani.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

sealed interface Screen {
    data object Home : Screen
    data object Search : Screen
    data object Library : Screen
    data object Wallet : Screen
    data object Settings : Screen
    data object Notifications : Screen
    data class SeriesDetail(val seriesId: String) : Screen
    data class Reader(val seriesId: String, val chapterId: String) : Screen
}

/**
 * A five-tab app with shallow stacks doesn't need a navigation library. This keeps routes as real
 * Kotlin types with no string parsing and no serialization plugin.
 */
class Navigator(start: Screen = Screen.Home) {
    private val stack = mutableStateListOf(start)

    val current: Screen get() = stack.last()

    val canGoBack: Boolean get() = stack.size > 1

    fun go(screen: Screen) {
        stack.add(screen)
    }

    fun selectTab(tab: Screen) {
        if (current == tab && stack.size == 1) return
        stack.clear()
        stack.add(tab)
    }

    fun back(): Boolean {
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }

    /** Which bottom tab should read as active for the screen currently on top. */
    fun activeTab(): Screen = stack.firstOrNull() ?: Screen.Home
}

@Composable
fun rememberNavigator(): Navigator = remember { Navigator() }

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("Navigator not provided") }
