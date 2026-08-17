package com.vl.kahani.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vl.kahani.data.LocalStore
import com.vl.kahani.ui.components.KahaniCard
import com.vl.kahani.ui.components.ScreenTitleBar
import com.vl.kahani.ui.nav.LocalNavigator
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType

@Composable
fun FollowingScreen(modifier: Modifier = Modifier) {
    val store = LocalStore.current
    val nav = LocalNavigator.current
    val followedAuthors = store.followedAuthorIds

    Column(
        modifier
            .fillMaxSize()
            .background(KahaniColors.Maroon900)
    ) {
        ScreenTitleBar(
            title = "Following",
            onBack = { nav.back() }
        )

        if (followedAuthors.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You're not following anyone yet.", color = KahaniColors.TextMuted)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(KahaniSpacing.md),
                verticalArrangement = Arrangement.spacedBy(KahaniSpacing.sm)
            ) {
                items(followedAuthors.size) { index ->
                    val authorId = followedAuthors[index]
                    val authorName = store.catalog.firstOrNull { (it as com.vl.kahani.data.Series).uploaderId == authorId }?.uploaderName ?: "Unknown Author"
                    
                    KahaniCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(authorName, style = KahaniType.CardTitle, color = KahaniColors.TextPrimary, modifier = Modifier.weight(1f))
                            Text(
                                "Unfollow",
                                color = KahaniColors.Saffron,
                                style = KahaniType.MicroBold,
                                modifier = Modifier
                                    .padding(KahaniSpacing.sm)
                                    .clickable { store.toggleFollowAuthor(authorId) }
                            )
                        }
                    }
                }
            }
        }
    }
}
