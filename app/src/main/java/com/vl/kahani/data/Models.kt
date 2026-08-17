package com.vl.kahani.data

enum class Genre(val key: String) {
    ROMANCE("romance"),
    THRILLER("thriller"),
    MYTHOLOGY("mythology"),
    COMEDY("comedy"),
    HORROR("horror"),
    FAMILY_DRAMA("family_drama"),
    CRIME("crime"),
    LOVE("love");

    /** Genres hidden when the Family/Safe filter is on. */
    val isMature: Boolean get() = this == HORROR || this == CRIME
}

enum class AppLanguage(val code: String, val nativeName: String, val englishName: String) {
    HINDI("hi", "हिन्दी", "Hindi"),
    ENGLISH("en", "English", "English"),
    TAMIL("ta", "தமிழ்", "Tamil"),
    BENGALI("bn", "বাংলা", "Bengali"),
    MARATHI("mr", "मराठी", "Marathi"),
    TELUGU("te", "తెలుగు", "Telugu"),
    KANNADA("kn", "ಕನ್ನಡ", "Kannada"),
    MALAYALAM("ml", "മലയാളം", "Malayalam");

    companion object {
        fun fromCode(code: String): AppLanguage = entries.firstOrNull { it.code == code } ?: HINDI
    }
}

enum class Format { TEXT, AUDIO }

enum class SeriesStatus { ONGOING, COMPLETED }

enum class NarrationType { HUMAN, AI_TTS }

data class Series(
    val id: String,
    val title: String,
    val synopsis: String,
    val genre: Genre,
    val language: AppLanguage,
    val totalChapters: Int,
    val status: SeriesStatus,
    val isEditorsPick: Boolean,
    val isNewThisWeek: Boolean,
    val ratingAvg: Float,
    val ratingCount: Int,
    val narratorName: String,
    val narrationType: NarrationType,
    val productionNote: String,
    val isMature: Boolean,
    val uploaderId: String? = null,
    val uploaderName: String? = null,
    val coverUrl: String? = null,
    val videoUrl: String? = null,
    val publishStatus: String = "PENDING", // PENDING, PUBLISHED, REJECTED
    val rejectionReason: String? = null,
    val playCount: Int = 0,
    val readCount: Int = 0,
    val watchCount: Int = 0,
    val onboardingRank: Int? = null, // 1, 2, 3, or 4
)

data class Chapter(
    val id: String,
    val seriesId: String,
    val chapterNumber: Int,
    val title: String,
    val textContent: String,
    val audioUrl: String? = null,
    val durationSeconds: Int,
    val wordCount: Int,
    val unlockCost: Int,
    val isFreePreview: Boolean,
    val isLive: Boolean = true, // Control weekly releases
)

data class ReadingProgress(
    val seriesId: String,
    val chapterId: String,
    val format: Format,
    /** 0f..1f through the chapter. Shared between text and audio for the same series. */
    val fraction: Float,
    val lastAccessedAt: Long,
)

enum class TransactionType { PURCHASE, SPEND, REFERRAL_BONUS, WELCOME_BONUS }

data class CoinTransaction(
    val id: String,
    val type: TransactionType,
    val amount: Int,
    val label: String,
    val createdAt: Long,
)

enum class NotificationType { NEW_CHAPTER, LOW_COINS, EDITORS_PICK }

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val seriesId: String?,
    val createdAt: Long,
    val isRead: Boolean,
)

data class DownloadedChapter(
    val chapterId: String,
    val seriesId: String,
    val format: Format,
    val sizeBytes: Long,
    val downloadedAt: Long,
)

data class Review(
    val id: String,
    val seriesId: String,
    val authorName: String,
    val rating: Int,
    val text: String,
    val createdAt: Long,
    val isMine: Boolean = false,
)

data class CoinPackage(
    val id: String,
    val coins: Int,
    val bonusCoins: Int,
    val priceLabel: String,
    val isBestValue: Boolean,
)

val CoinPackages = listOf(
    CoinPackage("coins_50", 50, 0, "₹49", false),
    CoinPackage("coins_150", 150, 15, "₹129", false),
    CoinPackage("coins_500", 500, 90, "₹399", true),
)
