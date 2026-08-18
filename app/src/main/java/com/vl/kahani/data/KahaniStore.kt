package com.vl.kahani.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

/**
 * Single source of truth for user state. Backed by SharedPreferences and synced with Firestore.
 */
class KahaniStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("kahani_state", Context.MODE_PRIVATE)
        
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    val catalog = mutableStateListOf<Series>()
    val chaptersCache = mutableStateMapOf<String, List<Chapter>>()
    private val reviewCache = mutableStateMapOf<String, List<Review>>()
    
    var isCatalogLoading by mutableStateOf(false)
        private set
        
    private var catalogListener: ListenerRegistration? = null

    var isOnboarded by mutableStateOf(prefs.getBoolean(KEY_ONBOARDED, false))
        private set

    var onboardingStep by mutableIntStateOf(prefs.getInt(KEY_ONBOARDING_STEP, 1))
        private set

    var isAuthenticated by mutableStateOf(auth.currentUser != null)
        private set

    var userIdentifier by mutableStateOf(prefs.getString(KEY_USER_ID, "") ?: "")
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

    var hasRequestedPlayReview by mutableStateOf(prefs.getBoolean(KEY_REVIEW_REQUESTED, false))
        private set

    var lastBonusClaimedAt by mutableLongStateOf(prefs.getLong(KEY_LAST_BONUS, 0L))
        private set

    var todayListenSeconds by mutableIntStateOf(prefs.getInt("today_listen_seconds", 0))
        private set
    
    var lastListenDate by mutableStateOf(prefs.getString("last_listen_date", "") ?: "")
        private set

    var listenSubSeconds by mutableFloatStateOf(prefs.getFloat("listen_sub_seconds", 0f))
        private set

    var lastProgressUpdate by mutableLongStateOf(0L)

    var rewardMessage by mutableStateOf<String?>(null)

    val unlockedChapterIds = mutableStateListOf<String>().apply {
        addAll(prefs.readSet(KEY_UNLOCKED))
    }

    val savedSeriesIds = mutableStateListOf<String>().apply { addAll(prefs.readSet(KEY_SAVED)) }

    val followedSeriesIds = mutableStateListOf<String>().apply { addAll(prefs.readSet(KEY_FOLLOWED)) }

    val followedAuthorIds = mutableStateListOf<String>().apply { addAll(prefs.readSet("followed_authors")) }

    val recentSearches = mutableStateListOf<String>().apply {
        addAll(prefs.getString(KEY_RECENT_SEARCH, "")!!.split("\u001F").filter { it.isNotBlank() })
    }

    val progress = mutableStateMapOf<String, ReadingProgress>().apply {
        prefs.readSet(KEY_PROGRESS).forEach { encoded ->
            val p = encoded.split("|")
            if (p.size == 5) {
                put(p[0], ReadingProgress(p[0], p[1], Format.valueOf(p[2]), p[3].toFloatOrNull() ?: 0f, p[4].toLongOrNull() ?: 0L))
            }
        }
    }

    val transactions = mutableStateListOf<CoinTransaction>()
    val downloads = mutableStateListOf<DownloadedChapter>()
    val notifications = mutableStateListOf<AppNotification>()

    val referralCode: String = prefs.getString(KEY_REFERRAL_CODE, null) ?: newReferralCode().also {
        prefs.edit().putString(KEY_REFERRAL_CODE, it).apply()
    }

    var referralClaimed by mutableStateOf(prefs.getBoolean(KEY_REFERRAL_CLAIMED, false))
        private set

    init {
        fetchCatalog()
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                isAuthenticated = true
                userIdentifier = user.email ?: user.phoneNumber ?: user.uid
                
                val userData = mutableMapOf(
                    "email" to user.email,
                    "phone" to user.phoneNumber,
                    "displayName" to (user.displayName ?: user.email?.substringBefore("@") ?: "User"),
                    "lastLogin" to com.google.firebase.Timestamp.now()
                )
                db.collection("users").document(user.uid).set(userData, SetOptions.merge())
                syncFromFirestore()
                fetchLiveNotifications()
                fetchTransactions()
                fetchDownloads()
            } else {
                isAuthenticated = false
                userIdentifier = ""
                notifications.clear()
                transactions.clear()
                downloads.clear()
            }
        }
    }

    private fun fetchLiveNotifications() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val fetched = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        AppNotification(
                            id = doc.id,
                            type = NotificationType.valueOf(doc.getString("type") ?: "NEW_CHAPTER"),
                            title = doc.getString("title") ?: "",
                            body = doc.getString("body") ?: "",
                            seriesId = doc.getString("seriesId"),
                            createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                            isRead = doc.getBoolean("isRead") ?: false
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                notifications.clear()
                notifications.addAll(fetched)
            }
    }

    private fun fetchTransactions() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("transactions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val fetched = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        CoinTransaction(
                            id = doc.id,
                            type = TransactionType.valueOf(doc.getString("type") ?: "PURCHASE"),
                            amount = doc.getLong("amount")?.toInt() ?: 0,
                            label = doc.getString("label") ?: "",
                            createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                transactions.clear()
                transactions.addAll(fetched)
            }
    }

    private fun fetchDownloads() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("downloads")
            .addSnapshotListener { snapshot, _ ->
                val fetched = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        DownloadedChapter(
                            chapterId = doc.getString("chapterId") ?: "",
                            seriesId = doc.getString("seriesId") ?: "",
                            format = Format.valueOf(doc.getString("format") ?: "TEXT"),
                            sizeBytes = doc.getLong("sizeBytes") ?: 0L,
                            downloadedAt = doc.getTimestamp("downloadedAt")?.toDate()?.time ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                downloads.clear()
                downloads.addAll(fetched)
            }
    }

    val unreadNotificationCount: Int get() = notifications.count { !it.isRead }
    fun markAllNotificationsRead() {
        for (i in notifications.indices) notifications[i] = notifications[i].copy(isRead = true)
    }
    fun dismissNotification(id: String) {
        notifications.removeAll { it.id == id }
    }

    fun fetchCatalog() {
        isCatalogLoading = true
        catalogListener?.remove()
        catalogListener = db.collection("series").addSnapshotListener { result, error ->
            if (error != null) {
                isCatalogLoading = false
                return@addSnapshotListener
            }
            val liveSeries = result?.documents?.mapNotNull { doc ->
                try {
                    Series(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        synopsis = doc.getString("synopsis") ?: "",
                        genre = Genre.valueOf(doc.getString("genre")?.uppercase() ?: "THRILLER"),
                        language = AppLanguage.fromCode(doc.getString("language") ?: "hi"),
                        totalChapters = doc.getLong("totalChapters")?.toInt() ?: 0,
                        status = SeriesStatus.valueOf(doc.getString("status")?.uppercase() ?: "ONGOING"),
                        isEditorsPick = doc.getBoolean("isEditorsPick") ?: false,
                        isNewThisWeek = doc.getBoolean("isNewThisWeek") ?: false,
                        ratingAvg = doc.getDouble("ratingAvg")?.toFloat() ?: 0f,
                        ratingCount = doc.getLong("ratingCount")?.toInt() ?: 0,
                        narratorName = doc.getString("narratorName") ?: "Author",
                        narrationType = NarrationType.valueOf(doc.getString("narrationType")?.uppercase() ?: "HUMAN"),
                        productionNote = doc.getString("productionNote") ?: "",
                        isMature = doc.getBoolean("isMature") ?: false,
                        uploaderId = doc.getString("uploaderId"),
                        uploaderName = doc.getString("uploaderName"),
                        coverUrl = doc.getString("coverUrl"),
                        videoUrl = doc.getString("videoUrl"),
                        publishStatus = doc.getString("publishStatus") ?: "PENDING",
                        rejectionReason = doc.getString("rejectionReason"),
                        playCount = doc.getLong("playCount")?.toInt() ?: 0,
                        readCount = doc.getLong("readCount")?.toInt() ?: 0,
                        watchCount = doc.getLong("watchCount")?.toInt() ?: 0,
                        onboardingRank = doc.getLong("onboardingRank")?.toInt()
                    )
                } catch (e: Exception) { null }
            } ?: emptyList()
            
            catalog.clear()
            catalog.addAll(liveSeries)
            isCatalogLoading = false
        }
    }

    private fun syncFromFirestore() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                (doc.get("genres") as? List<String>)?.let { list ->
                    genreInterests.clear()
                    genreInterests.addAll(list.mapNotNull { key -> Genre.entries.firstOrNull { it.key == key } })
                }
                (doc.get("unlocked") as? List<String>)?.let { list ->
                    unlockedChapterIds.clear()
                    unlockedChapterIds.addAll(list)
                }
                (doc.get("followed") as? List<String>)?.let { list ->
                    followedSeriesIds.clear()
                    followedSeriesIds.addAll(list)
                }
                (doc.get("saved") as? List<String>)?.let { list ->
                    savedSeriesIds.clear()
                    savedSeriesIds.addAll(list)
                }
                (doc.get("followedAuthors") as? List<String>)?.let { list ->
                    followedAuthorIds.clear()
                    followedAuthorIds.addAll(list)
                }
                (doc.get("languages") as? List<String>)?.let { list ->
                    contentLanguages.clear()
                    contentLanguages.addAll(list.map { AppLanguage.fromCode(it) })
                }
                doc.getString("uiLanguage")?.let { code ->
                    uiLanguage = AppLanguage.fromCode(code)
                }
                coinBalance = doc.getLong("coins")?.toInt() ?: coinBalance
                isOnboarded = doc.getBoolean("onboarded") ?: isOnboarded
            } else {
                // First time user, give welcome bonus
                recordTransaction(TransactionType.WELCOME_BONUS, WELCOME_COINS, "Welcome bonus")
            }
        }
    }

    private fun pushToFirestore() {
        val user = auth.currentUser ?: return
        val data = mapOf(
            "genres" to genreInterests.map { it.key },
            "languages" to contentLanguages.map { it.code },
            "unlocked" to unlockedChapterIds.toList(),
            "followed" to followedSeriesIds.toList(),
            "saved" to savedSeriesIds.toList(),
            "followedAuthors" to followedAuthorIds.toList(),
            "coins" to coinBalance,
            "onboarded" to isOnboarded,
            "uiLanguage" to uiLanguage.code
        )
        db.collection("users").document(user.uid).set(data, SetOptions.merge())
    }

    private fun recordTransaction(type: TransactionType, amount: Int, label: String) {
        val user = auth.currentUser ?: return
        val txData = mapOf(
            "type" to type.name,
            "amount" to amount,
            "label" to label,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        db.collection("users").document(user.uid).collection("transactions").add(txData)
        if (type != TransactionType.SPEND) {
             coinBalance += amount
             pushToFirestore()
        }
    }

    // ---- Onboarding ----------------------------------------------------------------
    fun applyUiLanguage(language: AppLanguage) {
        uiLanguage = language
        prefs.edit().putString(KEY_UI_LANG, language.code).apply()
        pushToFirestore()
    }

    fun toggleContentLanguage(language: AppLanguage) {
        if (!contentLanguages.remove(language)) contentLanguages.add(language)
        prefs.writeSet(KEY_CONTENT_LANGS, contentLanguages.map { it.code })
        pushToFirestore()
    }

    fun toggleGenreInterest(genre: Genre) {
        if (!genreInterests.remove(genre)) genreInterests.add(genre)
        prefs.writeSet(KEY_GENRES, genreInterests.map { it.key })
        pushToFirestore()
    }

    fun updateOnboardingStep(step: Int) {
        onboardingStep = step
        prefs.edit().putInt(KEY_ONBOARDING_STEP, step).apply()
    }

    fun completeOnboarding() {
        isOnboarded = true
        onboardingStep = 1
        prefs.edit().putBoolean(KEY_ONBOARDED, true).putInt(KEY_ONBOARDING_STEP, 1).apply()
        pushToFirestore()
    }

    // ---- Authentication -----------------------------------------------------------
    fun login(identifier: String) {
        isAuthenticated = true
        userIdentifier = identifier
        prefs.edit().putBoolean(KEY_AUTHENTICATED, true).putString(KEY_USER_ID, identifier).apply()
        pushToFirestore()
    }

    fun logout() {
        auth.signOut()
        isAuthenticated = false
        userIdentifier = ""
        isOnboarded = false
        prefs.edit().putBoolean(KEY_AUTHENTICATED, false).putString(KEY_USER_ID, "").putBoolean(KEY_ONBOARDED, false).apply()
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
    fun markPlayReviewRequested() {
        hasRequestedPlayReview = true
        prefs.edit().putBoolean(KEY_REVIEW_REQUESTED, true).apply()
        pushToFirestore()
    }

    // ---- Catalog access ------------------------------------------------------------
    fun visibleSeries(): List<Series> {
        val user = auth.currentUser
        val selectedCodes = contentLanguages.map { it.code }
        return catalog.filter { 
            val isMine = user != null && it.uploaderId == user.uid
            
            // Always show your own stories for easy testing
            if (isMine) return@filter true

            // Strict Filtering for others: Only show Published + Selected Languages + Safe Content
            val isPublished = it.publishStatus == "PUBLISHED"
            val langMatch = selectedCodes.isEmpty() || it.language.code in selectedCodes
            val safeMatch = !familySafeMode || (!it.isMature && !it.genre.isMature)
            
            isPublished && langMatch && safeMatch
        }
    }

    fun chapters(seriesId: String): List<Chapter> {
        db.collection("series").document(seriesId).collection("chapters")
            .orderBy("chapterNumber")
            .get()
            .addOnSuccessListener { snapshot ->
                val fetched = snapshot.documents.mapNotNull { doc ->
                    try {
                        Chapter(
                            id = doc.id,
                            seriesId = seriesId,
                            chapterNumber = doc.getLong("chapterNumber")?.toInt() ?: 1,
                            title = doc.getString("title") ?: "",
                            textContent = doc.getString("textContent") ?: "",
                            audioUrl = doc.getString("audioUrl"),
                            durationSeconds = doc.getLong("durationSeconds")?.toInt() ?: 300,
                            wordCount = doc.getLong("wordCount")?.toInt() ?: 1000,
                            unlockCost = doc.getLong("unlockCost")?.toInt() ?: 0,
                            isFreePreview = doc.getBoolean("isFreePreview") ?: (doc.getLong("chapterNumber") ?: 1L <= 3L),
                            isLive = doc.getBoolean("isLive") ?: true
                        )
                    } catch (e: Exception) { null }
                }
                if (fetched.isNotEmpty()) chaptersCache[seriesId] = fetched
            }

        val user = auth.currentUser
        val series = catalog.find { it.id == seriesId }
        val allChapters = chaptersCache[seriesId] ?: emptyList()
        
        // Filter: Regular users only see 'Live' chapters. Author/Admin see all.
        return if (user != null && (user.uid == series?.uploaderId || user.email == "dutypein@gmail.com")) {
            allChapters
        } else {
            allChapters.filter { it.isLive }
        }
    }

    fun isUnlocked(chapter: Chapter): Boolean = (chapter.unlockCost <= 0) || chapter.isFreePreview || unlockedChapterIds.contains(chapter.id)

    // ---- Coins ---------------------------------------------------------------------
    fun canAfford(cost: Int): Boolean = coinBalance >= cost
    fun unlockChapter(chapter: Chapter, seriesTitle: String): Boolean {
        if (isUnlocked(chapter)) return true
        if (!canAfford(chapter.unlockCost)) return false
        
        coinBalance -= chapter.unlockCost
        unlockedChapterIds += chapter.id
        
        recordTransaction(TransactionType.SPEND, -chapter.unlockCost, "$seriesTitle · ${chapter.chapterNumber}")
        
        persistCoins()
        pushToFirestore()
        return true
    }

    fun creditPurchase(pkg: CoinPackage) {
        val total = pkg.coins + pkg.bonusCoins
        recordTransaction(TransactionType.PURCHASE, total, "${pkg.coins} + ${pkg.bonusCoins} coins")
    }

    fun canClaimDailyBonus(): Boolean {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = now
        val currentDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        
        val lastCalendar = java.util.Calendar.getInstance()
        lastCalendar.timeInMillis = lastBonusClaimedAt
        val lastDay = lastCalendar.get(java.util.Calendar.DAY_OF_YEAR)
        val lastYear = lastCalendar.get(java.util.Calendar.YEAR)
        
        return (currentDay != lastDay || currentYear != lastYear)
    }

    fun claimDailyBonus(): Boolean {
        if (!canClaimDailyBonus()) return false
        
        val bonus = 10
        lastBonusClaimedAt = System.currentTimeMillis()
        recordTransaction(TransactionType.REFERRAL_BONUS, bonus, "Daily Reward")
        
        prefs.edit().putLong(KEY_LAST_BONUS, lastBonusClaimedAt).apply()
        return true
    }

    fun persistCoins() {
        prefs.edit().putInt(KEY_COINS, coinBalance).apply()
        prefs.writeSet(KEY_UNLOCKED, unlockedChapterIds)
    }

    fun addListenTime(seconds: Float) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        if (today != lastListenDate) {
            todayListenSeconds = 0
            listenSubSeconds = 0f
            lastListenDate = today
            prefs.edit().putString("last_listen_date", today).putFloat("listen_sub_seconds", 0f).apply()
        }
        
        val oldSeconds = todayListenSeconds
        val newAccumulated = listenSubSeconds + seconds
        val addedFullSeconds = newAccumulated.toInt()
        
        if (addedFullSeconds > 0) {
            todayListenSeconds += addedFullSeconds
            listenSubSeconds = newAccumulated - addedFullSeconds
            prefs.edit().putInt("today_listen_seconds", todayListenSeconds).putFloat("listen_sub_seconds", listenSubSeconds).apply()
        } else {
            listenSubSeconds = newAccumulated
        }
        
        // Listen & Earn: 15 minutes = 900 seconds
        if (oldSeconds < 900 && todayListenSeconds >= 900) {
            recordTransaction(TransactionType.REFERRAL_BONUS, 10, "Listen & Earn (15m)")
            rewardMessage = "You've earned 10 coins for listening!"
        }
    }

    fun incrementStat(seriesId: String, type: String) {
        val field = when(type) {
            "play" -> "playCount"
            "read" -> "readCount"
            "watch" -> "watchCount"
            else -> return
        }
        db.collection("series").document(seriesId).update(field, com.google.firebase.firestore.FieldValue.increment(1))
    }

    fun toggleFollowAuthor(authorId: String) {
        if (!followedAuthorIds.remove(authorId)) followedAuthorIds.add(authorId)
        prefs.writeSet("followed_authors", followedAuthorIds)
        pushToFirestore()
    }

    // ---- Library -------------------------------------------------------------------
    fun toggleSaved(seriesId: String) {
        if (!savedSeriesIds.remove(seriesId)) savedSeriesIds.add(seriesId)
        prefs.writeSet(KEY_SAVED, savedSeriesIds)
        pushToFirestore()
    }
    fun toggleFollowed(seriesId: String) {
        if (!followedSeriesIds.remove(seriesId)) followedSeriesIds.add(seriesId)
        prefs.writeSet(KEY_FOLLOWED, followedSeriesIds)
        pushToFirestore()
    }
    fun recordProgress(seriesId: String, chapterId: String, format: Format, fraction: Float) {
        val f = fraction.coerceIn(0f, 1f)
        progress[seriesId] = ReadingProgress(seriesId, chapterId, format, f, System.currentTimeMillis())
        prefs.writeSet(KEY_PROGRESS, progress.values.map { "${it.seriesId}|${it.chapterId}|${it.format}|${it.fraction}|${it.lastAccessedAt}" })
        lastProgressUpdate = System.currentTimeMillis()
    }
    fun inProgressSeries(): List<Series> = progress.values
        .sortedByDescending { it.lastAccessedAt }
        .mapNotNull { prog -> catalog.firstOrNull { it.id == prog.seriesId } }
        .filter { it.publishStatus == "PUBLISHED" && (!familySafeMode || !it.isMature) }
        .filter { (progress[it.id]?.fraction ?: 0f) < 0.99f }

    fun completedSeries(): List<Series> = progress.values
        .sortedByDescending { it.lastAccessedAt }
        .filter { it.fraction >= 0.99f }
        .mapNotNull { prog -> catalog.firstOrNull { it.id == prog.seriesId } }
        .filter { it.publishStatus == "PUBLISHED" && (!familySafeMode || !it.isMature) }

    // ---- Downloads -----------------------------------------------------------------
    fun isDownloaded(chapterId: String): Boolean = downloads.any { it.chapterId == chapterId }

    fun toggleDownload(chapter: Chapter, format: Format) {
        val user = auth.currentUser ?: return
        val existing = downloads.firstOrNull { it.chapterId == chapter.id }
        if (existing != null) {
            db.collection("users").document(user.uid).collection("downloads").document(chapter.id).delete()
            return
        }
        val perSecond = if (lowStorageMode || dataSaverMode) 4_000L else 12_000L
        val data = mapOf(
            "chapterId" to chapter.id,
            "seriesId" to chapter.seriesId,
            "format" to format.name,
            "sizeBytes" to if (format == Format.AUDIO) (chapter.durationSeconds * perSecond) else (chapter.wordCount * 6L),
            "downloadedAt" to com.google.firebase.Timestamp.now()
        )
        db.collection("users").document(user.uid).collection("downloads").document(chapter.id).set(data)
    }

    fun totalDownloadBytes(): Long = downloads.sumOf { it.sizeBytes }

    // ---- Search --------------------------------------------------------------------
    fun rememberSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        recentSearches.remove(trimmed); recentSearches.add(0, trimmed)
        while (recentSearches.size > 6) recentSearches.removeAt(recentSearches.lastIndex)
        prefs.edit().putString(KEY_RECENT_SEARCH, recentSearches.joinToString("\u001F")).apply()
    }
    fun clearRecentSearches() { recentSearches.clear(); prefs.edit().remove(KEY_RECENT_SEARCH).apply() }

    // ---- Reviews -------------------------------------------------------------------
    fun reviewsFor(seriesId: String): List<Review> {
        val cached = reviewCache[seriesId]
        if (cached != null) return cached
        db.collection("series").document(seriesId).collection("reviews")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val fetched = snapshot.documents.mapNotNull { doc ->
                    try {
                        Review(doc.id, seriesId, doc.getString("authorName") ?: "Anonymous", doc.getLong("rating")?.toInt() ?: 5, doc.getString("text") ?: "", doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(), doc.id == auth.currentUser?.uid)
                    } catch (e: Exception) { null }
                }
                if (fetched.isNotEmpty()) reviewCache[seriesId] = fetched
            }
        return emptyList()
    }
    fun myReview(seriesId: String): Review? = reviewCache[seriesId]?.firstOrNull { it.isMine }
    fun displayRating(series: Series): Float {
        val reviews = reviewCache[series.id] ?: return series.ratingAvg
        return (series.ratingAvg * series.ratingCount + reviews.sumOf { it.rating }) / (series.ratingCount + reviews.size)
    }
    fun ratingCount(series: Series): Int = series.ratingCount + (reviewCache[series.id]?.size ?: 0)
    fun submitReview(seriesId: String, rating: Int, text: String) {
        val user = auth.currentUser ?: return
        val reviewData = mapOf("seriesId" to seriesId, "authorId" to user.uid, "authorName" to (user.displayName ?: "User"), "rating" to rating.coerceIn(1, 5), "text" to text.trim(), "createdAt" to com.google.firebase.Timestamp.now())
        db.collection("series").document(seriesId).collection("reviews").document(user.uid).set(reviewData, SetOptions.merge()).addOnSuccessListener {
            reviewCache.remove(seriesId)
            reviewsFor(seriesId)
        }
    }

    fun reportSeries(seriesId: String, reason: String) {
        val user = auth.currentUser ?: return
        val reportData = mapOf(
            "seriesId" to seriesId,
            "reporterId" to user.uid,
            "reason" to reason,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        db.collection("reports").add(reportData)
    }

    // ---- Referrals -----------------------------------------------------------------
    enum class ReferralResult { CREDITED, ALREADY_CLAIMED, INVALID }
    fun claimReferral(code: String): ReferralResult {
        if (referralClaimed) return ReferralResult.ALREADY_CLAIMED
        val cleaned = code.trim().uppercase()
        if (cleaned.length != 6 || cleaned == referralCode || !cleaned.all { it.isLetterOrDigit() }) return ReferralResult.INVALID
        referralClaimed = true
        prefs.edit().putBoolean(KEY_REFERRAL_CLAIMED, true).apply()
        recordTransaction(TransactionType.REFERRAL_BONUS, REFERRAL_COINS, cleaned)
        return ReferralResult.CREDITED
    }

    private fun newReferralCode(): String {
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { alphabet.random() }.joinToString("")
    }

    private companion object {
        const val WELCOME_COINS = 60
        const val REFERRAL_COINS = 30
        const val KEY_ONBOARDED = "onboarded"
        const val KEY_USER_ID = "user_id"
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
        const val KEY_REVIEW_REQUESTED = "review_requested"
        const val KEY_ONBOARDING_STEP = "onboarding_step"
        const val KEY_AUTHENTICATED = "authenticated"
        const val KEY_LAST_BONUS = "last_bonus_claim"
    }
}

private fun SharedPreferences.readSet(key: String): List<String> = getStringSet(key, emptySet())!!.toList()
private fun SharedPreferences.writeSet(key: String, values: List<String>) { edit().putStringSet(key, values.toSet()).apply() }

val LocalStore = staticCompositionLocalOf<KahaniStore> { error("KahaniStore not provided") }
