package com.vl.kahani.data

import kotlin.math.abs

/**
 * Placeholder catalog for building and testing the UI. Real series come from the editorial
 * pipeline via Firestore; the shape here matches the `series` and `chapters` collections exactly
 * so swapping the source does not touch any screen.
 */
object SeedCatalog {

    private const val PRODUCTION_NOTE_HUMAN = "AI-assisted story, human-edited and human-narrated."
    private const val PRODUCTION_NOTE_TEXT = "AI-assisted story, human-edited."

    val series: List<Series> = listOf(
        Series(
            id = "s_aakhri_local",
            title = "आख़िरी लोकल",
            synopsis = "रोज़ की 11:42 लोकल में एक आदमी चढ़ता है और कभी उतरता नहीं। टिकट चेकर मीरा ने चार महीने तक इसे नज़रअंदाज़ किया। पाँचवें महीने उसने उसका चेहरा पहचान लिया — और वह चेहरा दो साल पहले जल चुका था।",
            genre = Genre.THRILLER,
            language = AppLanguage.HINDI,
            totalChapters = 22,
            status = SeriesStatus.ONGOING,
            isEditorsPick = true,
            isNewThisWeek = false,
            ratingAvg = 4.7f,
            ratingCount = 3184,
            narratorName = "Rukmini Deshpande",
            narrationType = NarrationType.HUMAN,
            productionNote = PRODUCTION_NOTE_HUMAN,
            isMature = false,
        ),
        Series(
            id = "s_sixteen_rains",
            title = "Sixteen Rains",
            synopsis = "They met the year the monsoon came late, and again every year after, always at the same tea stall, always for exactly one afternoon. Sixteen monsoons. One conversation neither of them has finished.",
            genre = Genre.ROMANCE,
            language = AppLanguage.ENGLISH,
            totalChapters = 18,
            status = SeriesStatus.ONGOING,
            isEditorsPick = true,
            isNewThisWeek = false,
            ratingAvg = 4.6f,
            ratingCount = 2210,
            narratorName = "Aditi Rao",
            narrationType = NarrationType.HUMAN,
            productionNote = PRODUCTION_NOTE_HUMAN,
            isMature = false,
        ),
        Series(
            id = "s_karna",
            title = "कर्ण का आख़िरी सवाल",
            synopsis = "युद्ध के अठारहवें दिन, रथ का पहिया धँसा हुआ है और आकाश शांत है। कर्ण के पास अब सिर्फ़ एक सवाल बचा है — और वह किसी देवता से नहीं, अपनी माँ से पूछना है।",
            genre = Genre.MYTHOLOGY,
            language = AppLanguage.HINDI,
            totalChapters = 26,
            status = SeriesStatus.ONGOING,
            isEditorsPick = true,
            isNewThisWeek = false,
            ratingAvg = 4.8f,
            ratingCount = 5642,
            narratorName = "Devdatt Mishra",
            narrationType = NarrationType.HUMAN,
            productionNote = PRODUCTION_NOTE_HUMAN,
            isMature = false,
        ),
        Series(
            id = "s_landlords_daughter",
            title = "The Landlord's Daughter",
            synopsis = "Four tenants, one crumbling house in Alleppey, and a will that names none of them. When the landlord's daughter comes back to sell it, everyone discovers how much of a family you can become by accident.",
            genre = Genre.FAMILY_DRAMA,
            language = AppLanguage.ENGLISH,
            totalChapters = 20,
            status = SeriesStatus.ONGOING,
            isEditorsPick = false,
            isNewThisWeek = false,
            ratingAvg = 4.4f,
            ratingCount = 1187,
            narratorName = "Susan Varghese",
            narrationType = NarrationType.HUMAN,
            productionNote = PRODUCTION_NOTE_HUMAN,
            isMature = false,
        ),
        Series(
            id = "s_bargad",
            title = "बरगद के नीचे",
            synopsis = "गाँव के लोग कहते हैं कि बरगद के नीचे कुछ नहीं है। फिर वे यह भी कहते हैं कि रात को उधर से मत जाना। दोनों बातें एक साथ सच नहीं हो सकतीं।",
            genre = Genre.HORROR,
            language = AppLanguage.HINDI,
            totalChapters = 15,
            status = SeriesStatus.ONGOING,
            isEditorsPick = false,
            isNewThisWeek = true,
            ratingAvg = 4.5f,
            ratingCount = 902,
            narratorName = "Shailesh Yadav",
            narrationType = NarrationType.HUMAN,
            productionNote = PRODUCTION_NOTE_HUMAN,
            isMature = true,
        ),
        Series(
            id = "s_chai_twice",
            title = "Chai, Twice a Day",
            synopsis = "Ramesh runs the worst tea stall in Indiranagar and the best listening post in the city. Every customer thinks their secret is safe. Ramesh has been keeping a ledger.",
            genre = Genre.COMEDY,
            language = AppLanguage.ENGLISH,
            totalChapters = 16,
            status = SeriesStatus.ONGOING,
            isEditorsPick = false,
            isNewThisWeek = false,
            ratingAvg = 4.3f,
            ratingCount = 1440,
            narratorName = "Vikram Iyer",
            narrationType = NarrationType.HUMAN,
            productionNote = PRODUCTION_NOTE_HUMAN,
            isMature = false,
        ),
        Series(
            id = "s_saheb_diary",
            title = "साहेब की डायरी",
            synopsis = "थाने के मालखाने में तीस साल पुरानी एक डायरी मिली है। उसमें लिखे हर नाम का आदमी आज ज़िंदा है, अमीर है, और सम्मानित है। सिर्फ़ लिखने वाला मर चुका है।",
            genre = Genre.CRIME,
            language = AppLanguage.HINDI,
            totalChapters = 24,
            status = SeriesStatus.ONGOING,
            isEditorsPick = false,
            isNewThisWeek = false,
            ratingAvg = 4.6f,
            ratingCount = 2760,
            narratorName = "Imran Qureshi",
            narrationType = NarrationType.HUMAN,
            productionNote = PRODUCTION_NOTE_HUMAN,
            isMature = true,
        ),
        Series(
            id = "s_sunday_fast",
            title = "The Sunday Fast",
            synopsis = "Amma has fasted every Sunday for forty-one years and has never once said why. The Sunday she breaks it, her three children finally ask — and get an answer none of them wanted.",
            genre = Genre.FAMILY_DRAMA,
            language = AppLanguage.ENGLISH,
            totalChapters = 14,
            status = SeriesStatus.COMPLETED,
            isEditorsPick = true,
            isNewThisWeek = false,
            ratingAvg = 4.9f,
            ratingCount = 4021,
            narratorName = "Lakshmi Menon",
            narrationType = NarrationType.HUMAN,
            productionNote = PRODUCTION_NOTE_HUMAN,
            isMature = false,
        ),
        Series(
            id = "s_mazhaikku_mun",
            title = "மழைக்கு முன்",
            synopsis = "கடல் அருகே ஒரு நூலகம், ஒரு மழைக்காலம், இரண்டு பேர் — ஒருவர் கடிதம் எழுதுகிறார், மற்றவர் பதில் எழுதுவதில்லை. பதினாறு வருடங்கள் கழித்து பதில் வருகிறது.",
            genre = Genre.ROMANCE,
            language = AppLanguage.TAMIL,
            totalChapters = 19,
            status = SeriesStatus.ONGOING,
            isEditorsPick = false,
            isNewThisWeek = true,
            ratingAvg = 4.5f,
            ratingCount = 764,
            narratorName = "Kavitha Subramanian",
            narrationType = NarrationType.HUMAN,
            productionNote = PRODUCTION_NOTE_HUMAN,
            isMature = false,
        ),
        Series(
            id = "s_ninth_floor",
            title = "Ninth Floor, No Lift",
            synopsis = "The building has nine floors and the lift has been broken since 2019. On the night of the power cut, someone walks all the way up — and every neighbour swears they heard the footsteps stop at their door.",
            genre = Genre.THRILLER,
            language = AppLanguage.ENGLISH,
            totalChapters = 21,
            status = SeriesStatus.ONGOING,
            isEditorsPick = false,
            isNewThisWeek = true,
            ratingAvg = 4.4f,
            ratingCount = 1533,
            narratorName = "Farhan Ali",
            narrationType = NarrationType.HUMAN,
            productionNote = PRODUCTION_NOTE_HUMAN,
            isMature = false,
        ),
        Series(
            id = "s_devi_ka_karz",
            title = "देवी का कर्ज़",
            synopsis = "गाँव ने देवी से एक बरस की बारिश माँगी थी और बदले में एक वादा किया था। बारिश आ गई। वादा अब सत्तर साल पुराना है, और देवी को गिनती याद है।",
            genre = Genre.MYTHOLOGY,
            language = AppLanguage.HINDI,
            totalChapters = 17,
            status = SeriesStatus.ONGOING,
            isEditorsPick = false,
            isNewThisWeek = false,
            ratingAvg = 4.2f,
            ratingCount = 618,
            narratorName = "Sunita Pathak",
            narrationType = NarrationType.HUMAN,
            productionNote = PRODUCTION_NOTE_HUMAN,
            isMature = false,
        ),
        Series(
            id = "s_half_ticket",
            title = "Half Ticket",
            synopsis = "A twelve-year-old buys a half ticket to a city he has never seen, to deliver a tiffin box to a father he has never met. Everyone he meets on the way assumes he is somebody else's problem.",
            genre = Genre.COMEDY,
            language = AppLanguage.ENGLISH,
            totalChapters = 12,
            status = SeriesStatus.COMPLETED,
            isEditorsPick = false,
            isNewThisWeek = false,
            ratingAvg = 4.7f,
            ratingCount = 2094,
            narratorName = "Pooja Shetty",
            narrationType = NarrationType.HUMAN,
            productionNote = PRODUCTION_NOTE_TEXT,
            isMature = false,
        ),
    )

    private val chapterTitlePool = listOf(
        "The 11:42", "What the Ledger Said", "Nobody Gets Off", "A Name in Chalk",
        "The Long Platform", "Second Warning", "Ash and Paper", "The Fourth Month",
        "What Amma Kept", "Rain, Late Again", "The Wheel Sinks", "One More Question",
        "Nine Flights Up", "The Power Cut", "A Half Ticket", "Return Journey",
        "The Broken Fast", "Everything She Owed", "Under the Banyan", "The Last Word",
        "A Promise, Counted", "Before the Rain", "The Ledger Closes", "Homecoming",
        "The Second Letter", "What He Left Behind",
    )

    fun chaptersFor(seriesId: String): List<Chapter> {
        val s = series.first { it.id == seriesId }
        val seed = abs(seriesId.hashCode())
        return (1..s.totalChapters).map { n ->
            val free = n <= 3
            val words = 1200 + (seed + n * 137) % 800
            Chapter(
                id = "${seriesId}_ch$n",
                seriesId = seriesId,
                chapterNumber = n,
                title = chapterTitlePool[(seed + n * 7) % chapterTitlePool.size],
                textContent = sampleChapterText(s, n),
                durationSeconds = 8 * 60 + (seed + n * 53) % (7 * 60),
                wordCount = words,
                unlockCost = if (free) 0 else if (n % 5 == 0) 12 else 8,
                isFreePreview = free,
            )
        }
    }

    fun seriesById(id: String): Series? = series.firstOrNull { it.id == id }

    private fun sampleChapterText(s: Series, n: Int): String {
        val pool = if (s.language == AppLanguage.HINDI) hindiParagraphs else englishParagraphs
        val seed = abs(s.id.hashCode() + n * 31)
        return (0 until 9).joinToString("\n\n") { i -> pool[(seed + i * 5) % pool.size] }
    }

    private val englishParagraphs = listOf(
        "The platform emptied the way it always did, in one long exhale, and then there was only the sound of the fan turning over the ticket counter and the particular silence a city keeps for itself after midnight.",
        "She had learned to read people by their hands. Hands told the truth long after the face had been trained out of it — the way a thumb worried at a wedding ring, the way a fist stayed a fist even when the voice had gone soft.",
        "Nobody in the building called it a mystery. Mysteries were for other people, for the television, for cities with better lighting. Here it was simply a thing that had happened, and the correct response to a thing that had happened was to not talk about it at breakfast.",
        "Outside, the rain had started the way it starts here — without any warning at all, as if someone had been holding it back with both arms and had suddenly let go.",
        "He counted the steps out of habit. Nine flights, one hundred and forty-four steps, and on the ninth landing a bulb that had been dead so long that no one remembered who was supposed to replace it.",
        "\"You want the truth,\" she said, \"or you want the version that lets you sleep? Because I have both, and only one of them is free.\"",
        "There is a kind of tiredness that sleep does not touch. It settles behind the eyes and stays there for years, and the people who carry it recognise each other instantly, across a room, without a word.",
        "The tea came in a glass too hot to hold properly, so you held it at the rim with two fingers and waited, and the waiting was half the point of the tea.",
        "Later, everyone would remember a different detail, and every one of those details would be true, and not one of them would help.",
        "She had promised herself she would not ask. She had made that promise every morning for four months, and she broke it the way she broke every promise she made to herself — quietly, completely, and without much surprise.",
    )

    private val hindiParagraphs = listOf(
        "प्लेटफ़ॉर्म एक लंबी साँस की तरह ख़ाली हुआ, और फिर सिर्फ़ टिकट खिड़की के ऊपर घूमते पंखे की आवाज़ बची, और वह चुप्पी जो शहर आधी रात के बाद अपने लिए बचाकर रखता है।",
        "उसने लोगों को हाथों से पढ़ना सीखा था। चेहरा झूठ बोलना सीख जाता है, हाथ नहीं — अँगूठा जब अँगूठी को बार-बार घुमाता है, या मुट्ठी तब भी मुट्ठी रहती है जब आवाज़ नरम पड़ चुकी हो।",
        "इमारत में किसी ने इसे रहस्य नहीं कहा। रहस्य दूसरों के लिए होते हैं, टीवी के लिए, बेहतर रोशनी वाले शहरों के लिए। यहाँ यह बस एक बात थी जो हो गई थी, और हो चुकी बात का सही जवाब यही था कि नाश्ते पर उसका ज़िक्र न किया जाए।",
        "बाहर बारिश उसी तरह शुरू हुई जैसे यहाँ होती है — बिना किसी चेतावनी के, जैसे किसी ने उसे दोनों हाथों से रोक रखा हो और अचानक छोड़ दिया हो।",
        "उसने आदतन सीढ़ियाँ गिनीं। नौ मंज़िल, एक सौ चौवालीस सीढ़ियाँ, और नौवें मोड़ पर वह बल्ब जो इतने बरसों से बुझा था कि अब किसी को याद नहीं कि उसे बदलना किसका काम था।",
        "\"तुम्हें सच चाहिए,\" उसने कहा, \"या वह बात जिससे नींद आ जाए? दोनों मेरे पास हैं, पर मुफ़्त सिर्फ़ एक है।\"",
        "एक थकान ऐसी होती है जिस तक नींद नहीं पहुँचती। वह आँखों के पीछे बैठ जाती है और बरसों वहीं रहती है, और जो लोग उसे ढोते हैं वे कमरे के आर-पार, बिना एक शब्द कहे, एक-दूसरे को पहचान लेते हैं।",
        "चाय ऐसे गिलास में आई जिसे ठीक से पकड़ा नहीं जा सकता था, इसलिए उसे किनारे से दो उँगलियों में थामकर इंतज़ार करना पड़ा — और वह इंतज़ार ही आधी चाय था।",
        "बाद में हर किसी को एक अलग बात याद रही, और वे सब बातें सच थीं, और उनमें से एक भी किसी काम की नहीं थी।",
        "उसने ख़ुद से वादा किया था कि वह नहीं पूछेगी। चार महीने से हर सुबह यही वादा करती थी, और तोड़ती भी उसी तरह जैसे ख़ुद से किया हर वादा तोड़ती थी — चुपचाप, पूरी तरह, और बिना ज़्यादा हैरानी के।",
    )
}
