package com.vl.kahani.ui.nav

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList

sealed interface Screen {
    data object Home : Screen
    data object Search : Screen
    data object Upload : Screen
    data object Library : Screen
    data object Profile : Screen
    data object Wallet : Screen
    data object Notifications : Screen
    data object Following : Screen
    data class SeriesDetail(val seriesId: String) : Screen
    data class Reader(val seriesId: String, val chapterId: String) : Screen
}

/**
 * A navigator that maintains a separate backstack for each primary tab.
 */
class Navigator(private val startTab: Screen = Screen.Home) {
    private val stacks = mutableStateMapOf<Screen, SnapshotStateList<Screen>>().apply {
        put(Screen.Home, mutableStateListOf(Screen.Home))
        put(Screen.Search, mutableStateListOf(Screen.Search))
        put(Screen.Upload, mutableStateListOf(Screen.Upload))
        put(Screen.Library, mutableStateListOf(Screen.Library))
        put(Screen.Profile, mutableStateListOf(Screen.Profile))
    }
    
    var activeTab by mutableStateOf(startTab)
        private set

    val current: Screen get() = stacks[activeTab]?.last() ?: activeTab

    val canGoBack: Boolean get() = (stacks[activeTab]?.size ?: 0) > 1 || activeTab != startTab

    fun go(screen: Screen) {
        stacks[activeTab]?.add(screen)
    }

    fun selectTab(tab: Screen) {
        if (activeTab == tab) {
            // Tapping the same tab again: reset that tab's stack to root
            stacks[tab]?.let { stack ->
                if (stack.size > 1) {
                    while (stack.size > 1) {
                        stack.removeAt(stack.lastIndex)
                    }
                }
            }
            return
        }
        activeTab = tab
        // If the tab stack was cleared for some reason, restore the root
        if (stacks[tab]?.isEmpty() == true) {
            stacks[tab]?.add(tab)
        }
    }

    fun back(): Boolean {
        val currentStack = stacks[activeTab] ?: return false
        if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.lastIndex)
            return true
        }
        if (activeTab != startTab) {
            activeTab = startTab
            return true
        }
        return false
    }

    /** Which bottom tab is currently active. */
    fun activeTab(): Screen = activeTab
}

@Composable
fun rememberNavigator(): Navigator = remember { Navigator() }

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("Navigator not provided") }
