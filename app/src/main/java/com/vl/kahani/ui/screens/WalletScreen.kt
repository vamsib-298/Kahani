package com.vl.kahani.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vl.kahani.data.CoinPackage
import com.vl.kahani.data.CoinPackages
import com.vl.kahani.data.CoinTransaction
import com.vl.kahani.data.KahaniStore
import com.vl.kahani.data.LocalStore
import com.vl.kahani.data.LocalStrings
import com.vl.kahani.data.TransactionType
import com.vl.kahani.ui.components.CoinGlyph
import com.vl.kahani.ui.components.GhostButton
import com.vl.kahani.ui.components.KahaniCard
import com.vl.kahani.ui.components.KahaniSearchField
import com.vl.kahani.ui.components.MetaTag
import com.vl.kahani.ui.components.PrimaryButton
import com.vl.kahani.ui.components.ProgressTrack
import com.vl.kahani.ui.components.ScreenTitleBar
import com.vl.kahani.ui.components.SectionHeader
import com.vl.kahani.ui.components.StateMessage
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniRadius
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType
import com.vl.kahani.ui.theme.Narrative

@Composable
fun WalletScreen(modifier: Modifier = Modifier) {
    val store = LocalStore.current
    val strings = LocalStrings.current
    var confirming by remember { mutableStateOf<CoinPackage?>(null) }

    Column(modifier.fillMaxSize()) {
        ScreenTitleBar(title = strings.navWallet)

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = KahaniSpacing.md,
                end = KahaniSpacing.md,
                bottom = KahaniSpacing.xxl,
            ),
        ) {
            item { BalanceCard(store.coinBalance) }

            item { ListenEarnCard() }

            item { DailyBonusCard() }

            item { ReferralCard() }

            item {
                SectionHeader(
                    label = strings.buyCoins,
                    modifier = Modifier.padding(top = KahaniSpacing.lg, bottom = KahaniSpacing.sm),
                )
            }

            items(CoinPackages.size) { index ->
                val pkg = CoinPackages[index]
                PackageCard(pkg = pkg, onBuy = { confirming = pkg })
                Spacer(Modifier.height(KahaniSpacing.xs))
            }

            item {
                Text(
                    strings.billingNotConfigured,
                    style = KahaniType.Micro,
                    color = KahaniColors.TextMuted,
                    modifier = Modifier.padding(top = KahaniSpacing.xs),
                )
            }

            item {
                SectionHeader(
                    label = strings.transactionHistory,
                    modifier = Modifier.padding(top = KahaniSpacing.lg, bottom = KahaniSpacing.xs),
                )
            }

            if (store.transactions.isEmpty()) {
                item {
                    StateMessage(title = strings.noTransactions, body = strings.libraryEmptyBody)
                }
            } else {
                items(store.transactions.size) { index ->
                    TransactionRow(store.transactions[index])
                }
            }
        }
    }

    val pkg = confirming
    if (pkg != null) {
        Dialog(onDismissRequest = { confirming = null }) {
            KahaniCard(Modifier.fillMaxWidth(), elevatedSurface = true, contentPadding = KahaniSpacing.lg) {
                Column {
                    Text(strings.buyCoins, style = KahaniType.ChapterTitle, color = KahaniColors.TextPrimary)
                    Spacer(Modifier.height(KahaniSpacing.xxs))
                    Text(
                        "${pkg.coins + pkg.bonusCoins} · ${pkg.priceLabel}",
                        style = KahaniType.UiBody,
                        color = KahaniColors.TextMuted,
                    )
                    Spacer(Modifier.height(KahaniSpacing.sm))
                    Text(
                        strings.billingNotConfigured,
                        style = KahaniType.Micro,
                        color = KahaniColors.TextMuted,
                    )
                    Spacer(Modifier.height(KahaniSpacing.lg))
                    Row(horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs)) {
                        GhostButton(strings.cancel, { confirming = null }, Modifier.weight(1f))
                        PrimaryButton(
                            text = strings.confirmUnlock,
                            onClick = {
                                store.creditPurchase(pkg)
                                confirming = null
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListenEarnCard() {
    val store = LocalStore.current
    val progress = (store.todayListenSeconds / 900f).coerceIn(0f, 1f)
    val minsLeft = (15 - (store.todayListenSeconds / 60)).coerceAtLeast(0)
    
    KahaniCard(Modifier.fillMaxWidth().padding(top = KahaniSpacing.sm)) {
        Column(verticalArrangement = Arrangement.spacedBy(KahaniSpacing.xs)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Listen & Earn", style = KahaniType.ChapterTitle, color = KahaniColors.TextPrimary)
                    Text(
                        if (progress >= 1f) "Daily limit reached! +10 coins added." else "Listen for $minsLeft more mins to earn 10 coins.",
                        style = KahaniType.Micro,
                        color = KahaniColors.TextMuted
                    )
                }
                Text("${(progress * 100).toInt()}%", style = KahaniType.UiBold, color = KahaniColors.Saffron)
            }
            ProgressTrack(fraction = progress)
        }
    }
}

@Composable
private fun DailyBonusCard() {
    val store = LocalStore.current
    val canClaim = store.canClaimDailyBonus()
    
    KahaniCard(
        Modifier.fillMaxWidth().padding(top = KahaniSpacing.sm),
        elevatedSurface = canClaim
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Daily Reward", style = KahaniType.ChapterTitle, color = KahaniColors.TextPrimary)
                Text(
                    if (canClaim) "Claim your 10 free coins!" else "Come back tomorrow for more.",
                    style = KahaniType.Micro,
                    color = KahaniColors.TextMuted
                )
            }
            PrimaryButton(
                text = if (canClaim) "Claim" else "Claimed",
                enabled = canClaim,
                onClick = { store.claimDailyBonus() }
            )
        }
    }
}

@Composable
private fun BalanceCard(balance: Int) {
    val strings = LocalStrings.current
    KahaniCard(Modifier.fillMaxWidth(), contentPadding = KahaniSpacing.lg) {
        Column {
            Text(strings.coinBalance, style = KahaniType.Micro, color = KahaniColors.TextMuted)
            Spacer(Modifier.height(KahaniSpacing.xs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoinGlyph(size = 28.dp)
                Spacer(Modifier.width(KahaniSpacing.xs))
                Text(
                    text = "$balance",
                    fontFamily = Narrative,
                    style = KahaniType.SeriesTitle,
                    color = KahaniColors.TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun ReferralCard() {
    val store = LocalStore.current
    val strings = LocalStrings.current
    val clipboard = LocalClipboardManager.current
    var code by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    KahaniCard(
        Modifier
            .fillMaxWidth()
            .padding(top = KahaniSpacing.sm),
    ) {
        Column {
            Text(strings.inviteFriend, style = KahaniType.ChapterTitle, color = KahaniColors.TextPrimary)
            Spacer(Modifier.height(KahaniSpacing.xxs))
            Text(strings.inviteBody, style = KahaniType.Micro, color = KahaniColors.TextMuted)

            Spacer(Modifier.height(KahaniSpacing.md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(strings.yourCode, style = KahaniType.Micro, color = KahaniColors.TextMuted)
                    Text(
                        store.referralCode,
                        style = KahaniType.UiBold.copy(letterSpacing = 3.sp),
                        color = KahaniColors.Saffron,
                    )
                }
                GhostButton(
                    text = if (message == strings.copiedCode) strings.copiedCode else strings.copyCode,
                    onClick = {
                        clipboard.setText(AnnotatedString(store.referralCode))
                        message = strings.copiedCode
                    },
                )
            }

            if (!store.referralClaimed) {
                Spacer(Modifier.height(KahaniSpacing.md))
                Text(strings.haveCode, style = KahaniType.Micro, color = KahaniColors.TextMuted)
                Spacer(Modifier.height(KahaniSpacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KahaniSearchField(
                        value = code,
                        onValueChange = { code = it.uppercase() },
                        hint = strings.enterCode,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(KahaniSpacing.xs))
                    PrimaryButton(
                        text = strings.claimCode,
                        enabled = code.isNotBlank(),
                        onClick = {
                            message = when (store.claimReferral(code)) {
                                KahaniStore.ReferralResult.CREDITED -> strings.referralCredited
                                KahaniStore.ReferralResult.ALREADY_CLAIMED -> strings.referralAlreadyClaimed
                                KahaniStore.ReferralResult.INVALID -> strings.referralInvalid
                                else -> "An unexpected error occurred."
                            }
                            code = ""
                        },
                    )
                }
            }

            message?.let {
                Spacer(Modifier.height(KahaniSpacing.xs))
                Text(it, style = KahaniType.Micro, color = KahaniColors.Saffron)
            }
        }
    }
}

@Composable
private fun PackageCard(pkg: CoinPackage, onBuy: () -> Unit) {
    val strings = LocalStrings.current
    KahaniCard(Modifier.fillMaxWidth(), elevatedSurface = pkg.isBestValue) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(KahaniRadius.chip))
                    .background(KahaniColors.Maroon900),
                contentAlignment = Alignment.Center,
            ) { CoinGlyph(size = 22.dp) }
            Spacer(Modifier.width(KahaniSpacing.sm))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${pkg.coins}",
                        style = KahaniType.CardTitle,
                        color = KahaniColors.TextPrimary,
                    )
                    if (pkg.bonusCoins > 0) {
                        Spacer(Modifier.width(KahaniSpacing.xxs))
                        Text(
                            "+${pkg.bonusCoins} ${strings.bonusCoins}",
                            style = KahaniType.MicroBold,
                            color = KahaniColors.Saffron,
                        )
                    }
                }
                if (pkg.isBestValue) {
                    Spacer(Modifier.height(KahaniSpacing.xxs))
                    MetaTag(strings.bestValue)
                }
            }
            PrimaryButton(text = pkg.priceLabel, onClick = onBuy)
        }
    }
}

@Composable
private fun TransactionRow(transaction: CoinTransaction) {
    val strings = LocalStrings.current
    val label = when (transaction.type) {
        TransactionType.PURCHASE -> strings.purchaseLabel
        TransactionType.SPEND -> strings.spendLabel
        TransactionType.REFERRAL_BONUS -> strings.referralLabel
        TransactionType.WELCOME_BONUS -> strings.welcomeLabel
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = KahaniSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = KahaniType.UiBody, color = KahaniColors.TextPrimary)
            Text(transaction.label, style = KahaniType.Micro, color = KahaniColors.TextMuted, maxLines = 1)
        }
        Text(
            text = if (transaction.amount >= 0) "+${transaction.amount}" else "${transaction.amount}",
            style = KahaniType.UiBold,
            color = if (transaction.amount >= 0) KahaniColors.Saffron else KahaniColors.TextMuted,
        )
    }
}
