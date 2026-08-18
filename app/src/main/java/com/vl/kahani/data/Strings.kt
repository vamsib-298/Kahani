package com.vl.kahani.data

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Full UI localization. Essential strings only.
 */
data class Strings(
    val appName: String = "Kahani",
    val tagline: String = "Every story, worth staying up for.",

    // Onboarding
    val chooseLanguageTitle: String = "Which languages do you read in?",
    val chooseLanguageBody: String = "The whole app switches to your first choice.",
    val chooseGenreTitle: String = "What do you like to get lost in?",
    val chooseGenreBody: String = "Pick at least three.",
    val starterPicksTitle: String = "Start with one of these",
    val starterPicksBody: String = "Hand-picked for you.",
    val continueLabel: String = "Continue",
    val pickAtLeastThree: String = "Pick at least 3",
    val enterKahani: String = "Enter Kahani",

    // Nav
    val navHome: String = "Home",
    val navSearch: String = "Search",
    val navLibrary: String = "Library",
    val navWallet: String = "Wallet",

    // Home
    val continueReading: String = "Continue",
    val editorsPicks: String = "Editor's Picks",
    val newThisWeek: String = "New This Week",
    val forYou: String = "For You",
    val pullToRefresh: String = "Pull to refresh",
    val notifications: String = "Notifications",

    // Search
    val searchHint: String = "Search stories, genres, narrators",
    val recentSearches: String = "Recent searches",
    val filterGenre: String = "Genre",
    val filterLanguage: String = "Language",
    val filterFreeToStart: String = "Free to start",
    val filterCompleted: String = "Completed only",
    val noResultsTitle: String = "No stories found",
    val noResultsBody: String = "Try a different word.",
    val browseGenres: String = "Browse genres",
    val clearFilters: String = "Clear filters",

    // Series detail
    val startSeries: String = "Start Series",
    val continueChapter: String = "Continue Chapter",
    val chaptersLabel: String = "Chapters",
    val ongoing: String = "Ongoing",
    val completed: String = "Completed",
    val narratedBy: String = "Narrated by",
    val save: String = "Save",
    val saved: String = "Saved",
    val follow: String = "Follow",
    val following: String = "Following",
    val freeLabel: String = "Free",
    val readLabel: String = "Read",
    val listenLabel: String = "Listen",
    val chapterLabel: String = "Chapter",
    val wordsLabel: String = "words",

    // Unlock
    val unlockChapter: String = "Unlock chapter",
    val unlockCostLabel: String = "Cost",
    val balanceAfter: String = "Balance after",
    val confirmUnlock: String = "Unlock",
    val cancel: String = "Cancel",
    val notEnoughCoins: String = "Not enough coins",
    val notEnoughCoinsBody: String = "Top up to keep reading.",
    val topUp: String = "Top up",
    val unlocked: String = "Unlocked",

    // Reader
    val dayMode: String = "Day",
    val nightMode: String = "Night",
    val textSize: String = "Text size",
    val chapterComplete: String = "Chapter complete",
    val nextChapter: String = "Next chapter",
    val previousChapter: String = "Previous chapter",
    val swipeForNext: String = "Swipe left for next",
    val switchToAudio: String = "Switch to audio",
    val switchToText: String = "Switch to text",

    // Player
    val nowPlaying: String = "Now playing",
    val sleepTimer: String = "Sleep timer",
    val playbackSpeed: String = "Speed",
    val endOfChapter: String = "End of chapter",
    val timerOff: String = "Off",
    val minutesShort: String = "min",
    val download: String = "Download",
    val downloaded: String = "Downloaded",
    val aiNarrated: String = "AI narrated",
    val humanNarrated: String = "Human narrated",

    // Wallet
    val coinBalance: String = "Coin balance",
    val buyCoins: String = "Buy coins",
    val bestValue: String = "Best value",
    val bonusCoins: String = "bonus",
    val transactionHistory: String = "Transaction history",
    val noTransactions: String = "No activity yet.",
    val purchaseLabel: String = "Purchase",
    val spendLabel: String = "Unlock",
    val referralLabel: String = "Bonus",
    val welcomeLabel: String = "Welcome",
    val billingNotConfigured: String = "Billing not connected.",

    // Reviews
    val ratingsReviews: String = "Ratings & reviews",
    val writeReview: String = "Write a review",
    val editReview: String = "Edit review",
    val yourRating: String = "Your rating",
    val reviewHint: String = "What did you think?",
    val postReview: String = "Post",
    val noReviews: String = "No reviews yet.",

    // Referral
    val inviteFriend: String = "Invite a friend",
    val inviteBody: String = "Earn rewards for every friend who joins.",
    val yourCode: String = "Your code",
    val copyCode: String = "Copy",
    val copiedCode: String = "Copied",
    val haveCode: String = "Have a code?",
    val enterCode: String = "Enter code",
    val claimCode: String = "Claim",
    val referralCredited: String = "Bonus added",
    val referralAlreadyClaimed: String = "Code already used.",
    val referralInvalid: String = "Invalid code.",

    // Library
    val inProgress: String = "In Progress",
    val savedTab: String = "Saved",
    val downloads: String = "Downloads",
    val libraryEmptyTitle: String = "Nothing here yet",
    val libraryEmptyBody: String = "Stories will appear here.",
    val storageUsed: String = "used on device",

    // Profile/Account
    val uiLanguage: String = "App language",
    val contentLanguages: String = "Story languages",
    val dataSaver: String = "Data saver",
    val dataSaverBody: String = "Stream audio at lower quality.",
    val helpSupport: String = "Help & support",
    val logOut: String = "Log out",
    val account: String = "Account",
    val notificationPrefs: String = "Notifications",
    val newChapterAlerts: String = "New chapters",
    val editorialAlerts: String = "Editor's picks",
    val lowCoinAlerts: String = "Low coins",

    // States
    val loading: String = "Loading",
    val errorTitle: String = "Error",
    val errorBody: String = "Check connection.",
    val retry: String = "Retry",
    val offlineReady: String = "Offline ready",
    val markAllRead: String = "Mark all read",
    val noNotificationsTitle: String = "Nothing new",
    val noNotificationsBody: String = "New chapters will appear here.",

    // Genres
    val genreRomance: String = "Romance",
    val genreThriller: String = "Thriller",
    val genreMythology: String = "Mythology",
    val genreComedy: String = "Comedy",
    val genreHorror: String = "Horror",
    val genreFamilyDrama: String = "Family Drama",
    val genreCrime: String = "Crime",
    val genreLove: String = "Love",
) {
    fun genre(genre: Genre): String = when (genre) {
        Genre.ROMANCE -> genreRomance
        Genre.THRILLER -> genreThriller
        Genre.MYTHOLOGY -> genreMythology
        Genre.COMEDY -> genreComedy
        Genre.HORROR -> genreHorror
        Genre.FAMILY_DRAMA -> genreFamilyDrama
        Genre.CRIME -> genreCrime
        Genre.LOVE -> genreLove
    }
}

private val English = Strings()

private val Hindi = Strings(
    appName = "कहानी",
    tagline = "हर कहानी, रात जागने लायक।",
    navHome = "होम",
    navSearch = "खोजें",
    navLibrary = "लाइब्रेरी",
    navWallet = "वॉलेट",
    continueReading = "जारी रखें",
    editorsPicks = "संपादक की पसंद",
    newThisWeek = "इस हफ़्ते नया",
    startSeries = "कहानी शुरू करें",
    chaptersLabel = "अध्याय",
    ongoing = "जारी",
    completed = "पूरी",
    save = "सहेजें",
    saved = "सहेजा गया",
    follow = "फ़ॉलो करें",
    following = "फ़ॉलो कर रहे हैं",
    freeLabel = "मुफ़्त",
    chapterLabel = "अध्याय",
    dayMode = "दिन",
    nightMode = "रात",
    chapterComplete = "अध्याय पूरा",
    nextChapter = "अगला अध्याय",
    coinBalance = "सिक्के",
    buyCoins = "सिक्के खरीदें",
    transactionHistory = "लेन-देन",
    purchaseLabel = "खरीद",
    spendLabel = "अध्याय खोला",
    ratingsReviews = "रेटिंग और समीक्षाएँ",
    writeReview = "समीक्षा लिखें",
    helpSupport = "मदद",
    logOut = "लॉग आउट",
    markAllRead = "सब पढ़ा हुआ करें",
    noNotificationsTitle = "कुछ नया नहीं",
    noNotificationsBody = "नए अध्याय यहाँ दिखेंगे।",
    genreRomance = "प्रेम",
    genreThriller = "थ्रिलर",
    genreMythology = "पौराणिक",
    genreComedy = "हास्य",
    genreHorror = "डरावनी",
    genreFamilyDrama = "पारिवारिक",
    genreCrime = "अपराध",
    genreLove = "प्रेम कहानी",
)

private val Telugu = Strings(
    appName = "కహానీ",
    tagline = "ప్రతి కథ, నిద్ర మేల్కొనేలా చేస్తుంది.",
    navHome = "హోమ్",
    navSearch = "శోధించండి",
    navLibrary = "లైబ్రరీ",
    navWallet = "వాలెట్",
    continueReading = "కొనసాగించు",
    editorsPicks = "ఎడిటర్ ఎంపికలు",
    newThisWeek = "ఈ వారం కొత్తవి",
    startSeries = "కథను ప్రారంభించండి",
    chaptersLabel = "అధ్యాయాలు",
    ongoing = "కొనసాగుతోంది",
    completed = "పూర్తయింది",
    save = "సేవ్ చేయి",
    saved = "సేవ్ చేయబడింది",
    follow = "ఫాలో",
    following = "ఫాలో అవుతున్నారు",
    freeLabel = "ఉచితం",
    chapterLabel = "అధ్యాయం",
    dayMode = "పగలు",
    nightMode = "రాత్రి",
    chapterComplete = "అధ్యాయం పూర్తయింది",
    nextChapter = "తర్వాతి అధ్యాయం",
    coinBalance = "నాణేల బ్యాలెన్స్",
    buyCoins = "నాణేలు కొనండి",
    transactionHistory = "లావాదేవీల చరిత్ర",
    purchaseLabel = "కొనుగోలు",
    spendLabel = "అధ్యాయం అన్‌లాక్",
    ratingsReviews = "రేటింగ్స్ & రివ్యూలు",
    writeReview = "రివ్యూ రాయండి",
    helpSupport = "సహాయం & మద్దతు",
    logOut = "లాగ్ అవుట్",
    markAllRead = "అన్నీ చదివినట్లు మార్క్ చేయండి",
    noNotificationsTitle = "కొత్తగా ఏమీ లేదు",
    noNotificationsBody = "కొత్త అధ్యాయాలు ఇక్కడ కనిపిస్తాయి.",
    genreRomance = "ప్రేమ",
    genreThriller = "థ్రిల్లర్",
    genreMythology = "పౌరాణిక",
    genreComedy = "హాస్యం",
    genreHorror = "హారర్",
    genreFamilyDrama = "కుటుంబ నాటకం",
    genreCrime = "క్రైమ్",
    genreLove = "ప్రేమ కథ",
)

fun stringsFor(language: AppLanguage): Strings = when (language) {
    AppLanguage.HINDI -> Hindi
    AppLanguage.TELUGU -> Telugu
    else -> English
}

val LocalStrings = staticCompositionLocalOf { English }
