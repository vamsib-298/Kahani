package com.vl.kahani.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vl.kahani.data.AppNotification
import com.vl.kahani.data.LocalStore
import com.vl.kahani.data.LocalStrings
import com.vl.kahani.data.NotificationType
import com.vl.kahani.ui.components.BellGlyph
import com.vl.kahani.ui.components.CloseGlyph
import com.vl.kahani.ui.components.CoinGlyph
import com.vl.kahani.ui.components.IconTapTarget
import com.vl.kahani.ui.components.KahaniCard
import com.vl.kahani.ui.components.ScreenTitleBar
import com.vl.kahani.ui.components.StarGlyph
import com.vl.kahani.ui.components.StateMessage
import com.vl.kahani.ui.nav.LocalNavigator
import com.vl.kahani.ui.nav.Screen
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniRadius
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType

@Composable
fun NotificationsScreen(modifier: Modifier = Modifier) {
    val store = LocalStore.current
    val strings = LocalStrings.current
    val nav = LocalNavigator.current

    Column(modifier.fillMaxSize()) {
        ScreenTitleBar(
            title = strings.notifications,
            onBack = { nav.back() },
            actions = {
                if (store.notifications.any { !it.isRead }) {
                    Text(
                        strings.markAllRead,
                        style = KahaniType.Micro,
                        color = KahaniColors.Saffron,
                        modifier = Modifier
                            .clickable { store.markAllNotificationsRead() }
                            .padding(KahaniSpacing.sm),
                    )
                }
            },
        )

        if (store.notifications.isEmpty()) {
            StateMessage(
                title = strings.noNotificationsTitle,
                body = strings.noNotificationsBody,
            )
            return@Column
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = KahaniSpacing.md,
                end = KahaniSpacing.md,
                bottom = KahaniSpacing.xxl,
            ),
        ) {
            items(store.notifications.size) { index ->
                val notification = store.notifications[index]
                NotificationCard(
                    notification = notification,
                    onOpen = {
                        notification.seriesId?.let { nav.go(Screen.SeriesDetail(it)) }
                            ?: nav.selectTab(Screen.Wallet)
                    },
                    onDismiss = { store.dismissNotification(notification.id) },
                )
                Spacer(Modifier.height(KahaniSpacing.xs))
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: AppNotification,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
) {
    KahaniCard(Modifier.fillMaxWidth(), onClick = onOpen, contentPadding = KahaniSpacing.sm) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(KahaniRadius.chip))
                    .background(KahaniColors.Maroon700),
                contentAlignment = Alignment.Center,
            ) {
                when (notification.type) {
                    NotificationType.NEW_CHAPTER -> BellGlyph(size = 16.dp, tint = KahaniColors.Saffron)
                    NotificationType.LOW_COINS -> CoinGlyph(size = 16.dp)
                    NotificationType.EDITORS_PICK -> StarGlyph(size = 16.dp)
                }
            }
            Spacer(Modifier.width(KahaniSpacing.sm))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        notification.title,
                        style = KahaniType.CardTitle,
                        color = KahaniColors.TextPrimary,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (!notification.isRead) {
                        Spacer(Modifier.width(KahaniSpacing.xs))
                        Box(
                            Modifier
                                .size(7.dp)
                                .background(KahaniColors.Saffron, CircleShape),
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(notification.body, style = KahaniType.Micro, color = KahaniColors.TextMuted)
            }
            IconTapTarget(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                CloseGlyph(size = 14.dp)
            }
        }
    }
}
