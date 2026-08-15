package com.vl.kahani.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.vl.kahani.ui.theme.KahaniColors

private const val PULL_THRESHOLD = 180f

/**
 * Pull-to-refresh built on the stable nested-scroll API so it cooperates with the list instead of
 * competing with it for gestures. The indicator is a saffron arc that fills as you pull.
 */
@Composable
fun PullToRefreshBox(
    listState: LazyListState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    var pull by remember { mutableFloatStateOf(0f) }

    val atTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0

    val connection = remember(atTop, isRefreshing) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0f && pull > 0f) {
                    val consumed = -minOf(pull, -available.y)
                    pull += consumed
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.y > 0f && atTop && !isRefreshing) {
                    pull = (pull + available.y * 0.5f).coerceAtMost(PULL_THRESHOLD * 1.4f)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (pull >= PULL_THRESHOLD && !isRefreshing) onRefresh()
                pull = 0f
                return Velocity.Zero
            }
        }
    }

    val progress = (pull / PULL_THRESHOLD).coerceIn(0f, 1f)
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (isRefreshing) 1f else progress,
        label = "pullAlpha",
    )

    Box(modifier.nestedScroll(connection)) {
        content()
        if (indicatorAlpha > 0.02f) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        alpha = indicatorAlpha
                        translationY = if (isRefreshing) 48f else pull * 0.6f
                    },
            ) {
                Canvas(Modifier.size(26.dp)) {
                    val stroke = size.minDimension * 0.1f
                    drawCircle(
                        color = KahaniColors.Maroon600,
                        radius = size.minDimension / 2f - stroke,
                        style = Stroke(stroke),
                    )
                    drawArc(
                        color = KahaniColors.Saffron,
                        startAngle = -90f,
                        sweepAngle = 360f * if (isRefreshing) 0.75f else progress,
                        useCenter = false,
                        topLeft = Offset(stroke, stroke),
                        size = androidx.compose.ui.geometry.Size(
                            size.width - stroke * 2,
                            size.height - stroke * 2,
                        ),
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                }
            }
        }
    }
}
