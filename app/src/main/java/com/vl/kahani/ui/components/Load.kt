package com.vl.kahani.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

enum class LoadState { LOADING, CONTENT, ERROR }

/**
 * Every screen goes through an explicit loading pass so the skeletons and the error path are real
 * code paths, not something we bolt on when the network layer lands.
 */
class Loader internal constructor() {
    var state by mutableStateOf(LoadState.LOADING)
        internal set
    internal var attempt by mutableIntStateOf(0)

    fun retry() {
        attempt++
    }
}

@Composable
fun rememberLoader(key: Any? = Unit, willFail: Boolean = false, delayMs: Long = 420): Loader {
    val loader = remember(key) { Loader() }
    LaunchedEffect(key, loader.attempt) {
        loader.state = LoadState.LOADING
        delay(delayMs)
        loader.state = if (willFail) LoadState.ERROR else LoadState.CONTENT
    }
    return loader
}
