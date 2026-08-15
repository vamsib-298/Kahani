package com.vl.kahani.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vl.kahani.data.AppLanguage
import com.vl.kahani.data.LocalStore
import com.vl.kahani.data.LocalStrings
import com.vl.kahani.ui.components.ContrastGlyph
import com.vl.kahani.ui.components.DownloadGlyph
import com.vl.kahani.ui.components.HairlineDivider
import com.vl.kahani.ui.components.KahaniCard
import com.vl.kahani.ui.components.KahaniChip
import com.vl.kahani.ui.components.NavRow
import com.vl.kahani.ui.components.ScreenTitleBar
import com.vl.kahani.ui.components.SectionHeader
import com.vl.kahani.ui.components.SettingToggleRow
import com.vl.kahani.ui.components.TunerGlyph
import com.vl.kahani.ui.nav.LocalNavigator
import com.vl.kahani.ui.nav.Screen
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType
import com.vl.kahani.ui.theme.Narrative

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val store = LocalStore.current
    val strings = LocalStrings.current
    val nav = LocalNavigator.current

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenTitleBar(title = strings.navSettings)

        SettingsSection(strings.uiLanguage) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs),
            ) {
                AppLanguage.entries.forEach { language ->
                    KahaniChip(
                        label = language.nativeName,
                        selected = store.uiLanguage == language,
                        onClick = { store.applyUiLanguage(language) },
                    )
                }
            }
            Spacer(Modifier.height(KahaniSpacing.md))
            Text(strings.contentLanguages, style = KahaniType.Micro, color = KahaniColors.TextMuted)
            Spacer(Modifier.height(KahaniSpacing.xs))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs),
            ) {
                AppLanguage.entries.forEach { language ->
                    KahaniChip(
                        label = language.nativeName,
                        selected = store.contentLanguages.contains(language),
                        onClick = { store.toggleContentLanguage(language) },
                    )
                }
            }
        }

        SettingsSection(strings.accessibility) {
            SettingToggleRow(
                title = strings.highContrast,
                body = strings.highContrastBody,
                checked = store.highContrastMode,
                onCheckedChange = { store.setHighContrast(it) },
                leading = { ContrastGlyph() },
            )
            HairlineDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(strings.defaultTextSize, style = KahaniType.UiBody, color = KahaniColors.TextPrimary)
                    Text(
                        "${store.readerFontSize.toInt()} sp",
                        style = KahaniType.Micro,
                        color = KahaniColors.TextMuted,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs)) {
                    listOf(15f, 17f, 20f, 24f).forEach { size ->
                        KahaniChip(
                            label = size.toInt().toString(),
                            selected = store.readerFontSize == size,
                            onClick = { store.updateReaderFontSize(size) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(KahaniSpacing.sm))
            Text(
                text = "Aa — ${strings.textSize}",
                fontFamily = Narrative,
                style = com.vl.kahani.ui.theme.KahaniType.readerBody(store.readerFontSize),
                color = KahaniColors.TextPrimary,
            )
        }

        SettingsSection(strings.dataSaver) {
            SettingToggleRow(
                title = strings.dataSaver,
                body = strings.dataSaverBody,
                checked = store.dataSaverMode,
                onCheckedChange = { store.setDataSaver(it) },
                leading = { DownloadGlyph() },
            )
            HairlineDivider()
            SettingToggleRow(
                title = strings.lowStorage,
                body = strings.lowStorageBody,
                checked = store.lowStorageMode,
                onCheckedChange = { store.setLowStorage(it) },
                leading = { TunerGlyph(size = 18.dp) },
            )
        }

        SettingsSection(strings.familySafe) {
            SettingToggleRow(
                title = strings.familySafe,
                body = strings.familySafeBody,
                checked = store.familySafeMode,
                onCheckedChange = { store.setFamilySafe(it) },
            )
        }

        SettingsSection(strings.notificationPrefs) {
            SettingToggleRow(
                title = strings.newChapterAlerts,
                body = null,
                checked = store.newChapterAlerts,
                onCheckedChange = { store.updateNewChapterAlerts(it) },
            )
            HairlineDivider()
            SettingToggleRow(
                title = strings.editorialAlerts,
                body = null,
                checked = store.editorialAlerts,
                onCheckedChange = { store.updateEditorialAlerts(it) },
            )
            HairlineDivider()
            SettingToggleRow(
                title = strings.lowCoinAlerts,
                body = null,
                checked = store.lowCoinAlerts,
                onCheckedChange = { store.updateLowCoinAlerts(it) },
            )
        }

        SettingsSection(strings.account) {
            NavRow(
                title = strings.notifications,
                onClick = { nav.go(Screen.Notifications) },
            )
            HairlineDivider()
            NavRow(
                title = strings.helpSupport,
                onClick = { },
            )
            HairlineDivider()
            NavRow(
                title = strings.logOut,
                onClick = { },
            )
        }

        Text(
            text = "Phone sign-in, coin purchases and cloud sync activate once the Firebase project and Play Billing account are connected.",
            style = KahaniType.Micro,
            color = KahaniColors.TextMuted,
            modifier = Modifier.padding(
                horizontal = KahaniSpacing.md,
                vertical = KahaniSpacing.lg,
            ),
        )
        Spacer(Modifier.height(KahaniSpacing.xxl))
    }
}

@Composable
private fun SettingsSection(label: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(horizontal = KahaniSpacing.md)) {
        SectionHeader(
            label = label,
            modifier = Modifier.padding(top = KahaniSpacing.md, bottom = KahaniSpacing.xs),
        )
        KahaniCard(Modifier.fillMaxWidth()) {
            Column { content() }
        }
    }
}
