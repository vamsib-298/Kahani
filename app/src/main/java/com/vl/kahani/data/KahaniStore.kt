package com.vl.kahani.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Single source of truth for user state. Backed by SharedPreferences today; every mutation goes
 * through a method here so the Firestore + Cloud Functions implementation can replace the bodies
 * without any screen changing.
 *
 * Coin mutations are client-side for now. They MUST move to a Cloud Function before launch —
 * a client that can write its own balance can mint free chapters.
 */
class KahaniStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("kahani_state", Context.MODE_PRIVATE)

    var isOnboarded by mutableStateOf(prefs.getBoolean(KEY_ONBOARDED, false))
        private set

    var uiLanguage by mutableStateOf(AppLanguage.fromCode(prefs.getString(KEY_UI_LANG, "en")!!))
        private set

    val contentLanguages = mutableStateListOf<AppLanguage>().apply {
        addAll(prefs.readSet(KEY_CONTENT_LANGS).map { AppLanguage.fromCode(it) })
    }

    val genreInterests = mutableStateListOf<Genre>().apply {
        addAll(prefs.readSet(KEY_GENRES).mapNotNull { key -> Genre.entries.firstOrNull { it.key == key } })
    }

    var coinBalance by mutableIntStateOf(prefs.getInt(KEY_COINS, WELCOME_COINS))
        private set

    var familySafeMode by mutableStateOf(prefs.getBoolean(KEY_FAMILY_SAFE, false))
        private set

    var dataSaverMode by mutableStateOf(prefs.getBoolean(KEY_DATA_SAVER, true))
        private set

    var lowStorageMode by mutableStateOf(prefs.getBoolean(KEY_LOW_STORAGE, false))
        private set

    var highContrastMode by mutableStateOf(prefs.getBoolean(KEY_HIGH_CONTRAST, false))
        private set

    var readerFontSize by mutableFloatStateOf(prefs.getFloat(KEY_FONT_SIZE, 17f))
        private set

    var readerDayMode by mutableStateOf(prefs.getBoolean(KEY_DAY_MODE, false))
        private set

    var newChapterAlerts by mutableStateOf(prefs.getBoolean(KEY_ALERT_CHAPTER, true))
        private set

    var editorialAlerts by mutableStateOf(prefs.getBoolean(KEY_ALERT_EDITORIAL, true))
        private set

    var lowCoinAlerts by mutableStateOf(prefs.getBoolean(KEY_ALERT_COINS, true))
        private set

    val unlockedChapterIds = mutableStateListOf<String>().apply {
        addAll(prefs.readSet(KEY_UNLOCKED))
    }

    val savedSeriesIds = mutableStateListOf<String>().apply { addAll(prefs.readSet(KEY_SAVED)) }

    val followedSeriesIds = mutableStateListOf<String>().apply { addAll(prefs.readSet(KEY_FOLLOWED)) }

    val recentSearches = mutableStateListOf<String>().apply {
        addAll(prefs.getString(KEY_RECENT_SEARCH, "")!!.split("\u001F").filter { it.isNotBlank() })
    }

    /** seriesId -> progress. Text and audio share one entry per series, by design. */
    val progress = mutableStateMapOf<String, ReadingProgress>().apply {
        prefs.readSet(KEY_PROGRESS).forEach { encoded ->
            val p = encoded.split("|")
            if (p.size == 5) {
                put(
                    p[0],
                    ReadingProgress(
                        seriesId = p[0],
                        chapterId = p[1],
                        format = Format.valueOf(p[2]),
                        fraction = p[3].toFloatOrNull() ?: 0f,
                        lastAccessedAt = p[4].toLongOrNull() ?: 0L,
                    ),
                )
            }
        }
    }

    val transactions = mutableStateListOf<CoinTransaction>()

    val downloads = mutableStateListOf<DownloadedChapter>()

    val notifications = mutableStateListOf<AppNotification>()

    val reviews = mutableStateListOf<Review>()

    /** Stable per-install invite code. Server-issued once auth lands. */
    val referralCode: String = prefs.getString(KEY_REFERRAL_CODE, null) ?: newReferralCode().also {
        prefs.edit().putString(KEY_REFERRAL_CODE, it).apply()
    }

    var referralClaimed by mutableStateOf(prefs.getBoolean(KEY_REFERRAL_CLAIMED, false))
        private set

    init {
        if (!prefs.getBoolean(KEY_SEEDED, false)) {
            transactions += CoinTransaction(
                id = "t_welcome",
                type = TransactionType.WELCOME_BONUS,
                amount = WELCOME_COINS,
                label = "Welcome bonus",
                createdAt = System.currentTimeMillis(),
            )
            prefs.edit().putBoolean(KEY_SEEDED, true).apply()
        }
        seedNotifications()
        seedReviews()
    }

    // ---- Onboarding ----------------------------------------------------------------

    fun applyUiLanguage(language: AppLanguage) {
        uiLanguage = language
        prefs.edit().putString(KEY_UI_LANG, language.code).apply()
    }

    fun toggleContentLanguage(language: AppLanguage) {
        if (!contentLanguages.remove(language)) contentLanguages.add(language)
        if (contentLanguages.isNotEmpty()) applyUiLanguage(contentLanguages.first())
        prefs.writeSet(KEY_CONTENT_LANGS, contentLanguages.map { it.code })
    }

    fun toggleGenreInterest(genre: Genre) {
        if (!genreInterests.remove(genre)) genreInterests.add(genre)
        prefs.writeSet(KEY_GENRES, genreInterests.map { it.key })
    }

    fun completeOnboarding() {
        isOnboarded = true
        prefs.edit().putBoolean(KEY_ONBOARDED, true).apply()
    }

    // ---- Settings ------------------------------------------------------------------

    fun setFamilySafe(value: Boolean) {
        familySafeMode = value
        prefs.edit().putBoolean(KEY_FAMILY_SAFE, value).apply()
    }

    fun setDataSaver(value: Boolean) {
        dataSaverMode = value
        prefs.edit().putBoolean(KEY_DATA_SAVER, value).apply()
    }

    fun setLowStorage(value: Boolean) {
        lowStorageMode = value
        prefs.edit().putBoolean(KEY_LOW_STORAGE, value).apply()
    }

    fun setHighContrast(value: Boolean) {
        highContrastMode = value
        prefs.edit().putBoolean(KEY_HIGH_CONTRAST, value).apply()
    }

    fun updateReaderFontSize(value: Float) {
        readerFontSize = value.coerceIn(15f, 24f)
        prefs.edit().putFloat(KEY_FONT_SIZE, readerFontSize).apply()
    }

    fun updateReaderDayMode(value: Boolean) {
        readerDayMode = value
        prefs.edit().putBoolean(KEY_DAY_MODE, value).apply()
    }

    fun updateNewChapterAlerts(value: Boolean) {
        newChapterAlerts = value
        prefs.edit().putBoolean(KEY_ALERT_CHAPTER, value).apply()
    }

    fun updateEditorialAlerts(value: Boolean) {
        editorialAlerts = value
        prefs.edit().putBoolean(KEY_ALERT_EDITORIAL, value).apply()
    }

    fun updateLowCoinAlerts(value: Boolean) {
        lowCoinAlerts = value
        prefs.edit().putBoolean(KEY_ALERT_COINS, value).apply()
    }

    // ---- Catalog access ------------------------------------------------------------

    fun visibleSeries(): List<Series> =
        SeedCatalog.series.filter { !familySafeMode || (!it.isMature && !it.genre.isMature) }

    fun chapters(seriesId: String): List<Chapter> = SeedCatalog.chaptersFor(seriesId)

    fun isUnlocked(chapter: Chapter): Boolean =
        chapter.isFreePreview || unlockedChapterIds.contains(chapter.id)

    // ---- Coins ---------------------------------------------------------------------

    fun canAfford(cost: Int): Boolean = coinBalance >= cost

    fun unlockChapter(chapter: Chapter, seriesTitle: String): Boolean {
        if (isUnlocked(chapter)) return true
        if (!canAfford(chapter.unlockCost)) return false
        coinBalance -= chapter.unlockCost
        unlockedChapterIds += chapter.id
        transactions.add(
            0,
            CoinTransaction(
                id = "t_${chapter.id}_${System.currentTimeMillis()}",
                type = TransactionType.SPEND,
                amount = -chapter.unlockCost,
                label = "$seriesTitle · ${chapter.chapterNumber}",
                createdAt = System.currentTimeMillis(),
            ),
        )
        persistCoins()
        return true
    }

    fun creditPurchase(pkg: CoinPackage) {
        val total = pkg.coins + pkg.bonusCoins
        coinBalance += total
        transactions.add(
            0,
            CoinTransaction(
                id = "t_${pkg.id}_${System.currentTimeMillis()}",
                type = TransactionType.PURCHASE,
                amount = total,
                label = "${pkg.coins} + ${pkg.bonusCoins} coins",
                createdAt = System.currentTimeMillis(),
            ),
        )
        persistCoins()
    }

    private fun persistCoins() {
        prefs.edit().putInt(KEY_COINS, coinBalance).apply()
        prefs.writeSet(KEY_UNLOCKED, unlockedChapterIds)
    }

    // ---- Library -------------------------------------------------------------------

    fun toggleSaved(seriesId: String) {
        if (!savedSeriesIds.remove(seriesId)) savedSeriesIds.add(seriesId)
        prefs.writeSet(KEY_SAVED, savedSeriesIds)
    }

    fun toggleFollowed(seriesId: String) {
        if (!followedSeriesIds.remove(seriesId)) followedSeriesIds.add(seriesId)
        prefs.writeSet(KEY_FOLLOWED, followedSeriesIds)
    }

    fun recordProgress(seriesId: String, chapterId: String, format: Format, fraction: Float) {
        val previous = progress[seriesId]
        progress[seriesId] = ReadingProgress(
            seriesId = seriesId,
            chapterId = chapterId,
            format = format,
            fraction = fraction.coerceIn(0f, 1f),
            lastAccessedAt = System.currentTimeMillis(),
        )
        // Reading and playback report continuously; only touch disk on a meaningful move.
        val moved = previous == null ||
            previous.chapterId != chapterId ||
            kotlin.math.abs(previous.fraction - fraction) >= 0.02f
        if (!moved) return
        prefs.writeSet(
            KEY_PROGRESS,
            progress.values.map { "${it.seriesId}|${it.chapterId}|${it.format}|${it.fraction}|${it.lastAccessedAt}" },
        )
    }

    fun inProgressSeries(): List<Series> =
        progress.values
            .sortedByDescending { it.lastAccessedAt }
            .mapNotNull { SeedCatalog.seriesById(it.seriesId) }
            .filter { !familySafeMode || !it.isMature }

    fun completedSeries(): List<Series> =
        progress.values
            .filter { it.fraction >= 0.99f && SeedCatalog.seriesById(it.seriesId)?.status == SeriesStatus.COMPLETED }
            .mapNotNull { SeedCatalog.seriesById(it.seriesId) }

    // ---- Downloads -----------------------------------------------------------------

    fun isDownloaded(chapterId: String): Boolean = downloads.any { it.chapterId == chapterId }

    fun toggleDownload(chapter: Chapter, format: Format) {
        val existing = downloads.firstOrNull { it.chapterId == chapter.id }
        if (existing != null) {
            downloads.remove(existing)
            return
        }
        val perSecond = if (lowStorageMode || dataSaverMode) 4_000L else 12_000L
        downloads.add(
            DownloadedChapter(
                chapterId = chapter.id,
                seriesId = chapter.seriesId,
                format = format,
                sizeBytes = if (format == Format.AUDIO) chapter.durationSeconds * perSecond else chapter.wordCount * 6L,
                downloadedAt = System.currentTimeMillis(),
            ),
        )
    }

    fun totalDownloadBytes(): Long = downloads.sumOf { it.sizeBytes }

    // ---- Search --------------------------------------------------------------------

    fun rememberSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        recentSearches.remove(trimmed)
        recentSearches.add(0, trimmed)
        while (recentSearches.size > 6) recentSearches.removeAt(recentSearches.lastIndex)
        prefs.edit().putString(KEY_RECENT_SEARCH, recentSearches.joinToString("\u001F")).apply()
    }

    fun clearRecentSearches() {
        recentSearches.clear()
        prefs.edit().remove(KEY_RECENT_SEARCH).apply()
    }

    // ---- Reviews -------------------------------------------------------------------

    fun reviewsFor(seriesId: String): List<Review> =
        reviews.filter { it.seriesId == seriesId }.sortedByDescending { it.createdAt }

    fun myReview(seriesId: String): Review? =
        reviews.firstOrNull { it.seriesId == seriesId && it.isMine }

    /** Catalog average blended with this device's own review, so the number the user sees moves. */
    fun displayRating(series: Series): Float {
        val mine = myReview(series.id) ?: return series.ratingAvg
        val total = series.ratingAvg * series.ratingCount + mine.rating
        return total / (series.ratingCount + 1)
    }

    fun ratingCount(series: Series): Int =
        series.ratingCount + if (myReview(series.id) != null) 1 else 0

    fun submitReview(seriesId: String, rating: Int, text: String) {
        reviews.removeAll { it.seriesId == seriesId && it.isMine }
        reviews.add(
            Review(
                id = "r_${seriesId}_me",
                seriesId = seriesId,
                authorName = "You",
                rating = rating.coerceIn(1, 5),
                text = text.trim(),
                createdAt = System.currentTimeMillis(),
                isMine = true,
            ),
        )
    }

    // ---- Referrals -----------------------------------------------------------------

    enum class ReferralResult { CREDITED, ALREADY_CLAIMED, INVALID }

    fun claimReferral(code: String): ReferralResult {
        if (referralClaimed) return ReferralResult.ALREADY_CLAIMED
        val cleaned = code.trim().uppercase()
        // Server-side validation replaces this once Cloud Functions are live.
        if (cleaned.length != 6 || cleaned == referralCode || !cleaned.all { it.isLetterOrDigit() }) {
            return ReferralResult.INVALID
        }
        referralClaimed = true
        prefs.edit().putBoolean(KEY_REFERRAL_CLAIMED, true).apply()
        coinBalance += REFERRAL_COINS
        transactions.add(
            0,
            CoinTransaction(
                id = "t_referral_${System.currentTimeMillis()}",
                type = TransactionType.REFERRAL_BONUS,
                amount = REFERRAL_COINS,
                label = cleaned,
                createdAt = System.currentTimeMillis(),
            ),
        )
        persistCoins()
        return ReferralResult.CREDITED
    }

    // ---- Notifications -------------------------------------------------------------

    val unreadNotificationCount: Int get() = notifications.count { !it.isRead }
    fun markAllNotificationsRead() {
        for (i in notifications.indices) notifications[i] = notifications[i].copy(isRead = true)
    }

    fun dismissNotification(id: String) {
        notifications.removeAll { it.id == id }
    }

    private fun seedNotifications() {
        if (notifications.isNotEmpty()) return
        val now = System.currentTimeMillis()
        notifications += AppNotification(
            id = "n1",
            type = NotificationType.NEW_CHAPTER,
            title = "आख़िरी लोकल · Chapter 22",
            body = "A new chapter is out. Mira finally checks the last coach.",
            seriesId = "s_aakhri_local",
            createdAt = now - 2 * 3600_000,
            isRead = false,
        )
        notifications += AppNotification(
            id = "n2",
            type = NotificationType.EDITORS_PICK,
            title = "New in Editor's Picks",
            body = "The Sunday Fast — all 14 chapters, complete.",
            seriesId = "s_sunday_fast",
            createdAt = now - 26 * 3600_000,
            isRead = false,
        )
        notifications += AppNotification(
            id = "n3",
            type = NotificationType.LOW_COINS,
            title = "Coins running low",
            body = "You have enough for one more chapter.",
            seriesId = null,
            createdAt = now - 3 * 24 * 3600_000,
            isRead = true,
        )
    }

    private fun seedReviews() {
        if (reviews.isNotEmpty()) return
        val now = System.currentTimeMillis()
        reviews += Review(
            "r1", "s_aakhri_local", "Neha K.", 5,
            "The chapter where she checks the last coach kept me up till 2am. Narration is excellent.",
            now - 4 * 24 * 3600_000,
        )
        reviews += Review(
            "r2", "s_aakhri_local", "Arun P.", 4,
            "Slow start but chapter 7 onwards it does not let go.",
            now - 9 * 24 * 3600_000,
        )
        reviews += Review(
            "r3", "s_sunday_fast", "Meera R.", 5,
            "Finished it in two sittings. The last chapter is genuinely quiet and devastating.",
            now - 2 * 24 * 3600_000,
        )
        reviews += Review(
            "r4", "s_karna", "Devansh S.", 5,
            "Finally a retelling that doesn't just repeat the same scenes. The language is beautiful.",
            now - 6 * 24 * 3600_000,
        )
        reviews += Review(
            "r5", "s_sixteen_rains", "Farida M.", 4,
            "Sweet without being sentimental. The tea stall descriptions are so specific.",
            now - 11 * 24 * 3600_000,
        )
    }

    private fun newReferralCode(): String {
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { alphabet.random() }.joinToString("")
    }

    private companion object {
        const val WELCOME_COINS = 60
        const val REFERRAL_COINS = 30

        const val KEY_ONBOARDED = "onboarded"
        const val KEY_UI_LANG = "ui_lang"
        const val KEY_CONTENT_LANGS = "content_langs"
        const val KEY_GENRES = "genres"
        const val KEY_COINS = "coins"
        const val KEY_FAMILY_SAFE = "family_safe"
        const val KEY_DATA_SAVER = "data_saver"
        const val KEY_LOW_STORAGE = "low_storage"
        const val KEY_HIGH_CONTRAST = "high_contrast"
        const val KEY_FONT_SIZE = "font_size"
        const val KEY_DAY_MODE = "day_mode"
        const val KEY_ALERT_CHAPTER = "alert_chapter"
        const val KEY_ALERT_EDITORIAL = "alert_editorial"
        const val KEY_ALERT_COINS = "alert_coins"
        const val KEY_UNLOCKED = "unlocked"
        const val KEY_SAVED = "saved"
        const val KEY_FOLLOWED = "followed"
        const val KEY_PROGRESS = "progress"
        const val KEY_RECENT_SEARCH = "recent_search"
        const val KEY_REFERRAL_CODE = "referral_code"
        const val KEY_REFERRAL_CLAIMED = "referral_claimed"
        const val KEY_SEEDED = "seeded"
    }
}

private fun SharedPreferences.readSet(key: String): List<String> =
    getStringSet(key, emptySet())!!.toList()

private fun SharedPreferences.writeSet(key: String, values: List<String>) {
    edit().putStringSet(key, values.toSet()).apply()
}

val LocalStore = staticCompositionLocalOf<KahaniStore> { error("KahaniStore not provided") }
