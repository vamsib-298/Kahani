# Kahani — Master Build Spec

> **Motto:** *"Every story, worth staying up for."*
>
> A serialized audio + text story platform for India. KukuFM/Pocket FM-style audio dramas and
> Pratilipi-style chapter reading in one app. AI helps draft at scale; **every published chapter
> passes a real human editorial pass**, and audio is human-narrated. Quality and curation are the
> product — and we say so honestly on every series page.

---

## 0. Status & Scope

| Item | State |
| --- | --- |
| Android app shell (Kotlin, Compose) | In build |
| Design system (colors, type, spacing, components) | In build |
| Consumer screens (onboarding → reader/player → wallet) | In build |
| Firebase (Auth/Firestore/Storage/FCM) | **Blocked — needs your Firebase project + `google-services.json`** |
| Google Play Billing | **Blocked — needs Play Console + configured coin SKUs** |
| Real catalog content | **Blocked — needs editorial/narration pipeline output** |

Until the blocked items land, the app runs on a **local seed catalog** behind a repository
interface, so swapping in Firestore is a one-file change and no screen code moves.

> **AI pipeline is internal.** Story-bible generation and chapter drafting are a *content-production
> tool* run by the editorial team. There is no "AI writes your story live" feature in the consumer
> app. Do not build one.

---

## 1. Design Direction

The warm maroon-and-saffron world works because it doesn't feel like a tech product — it feels like
a **storyteller's room at dusk**: lamp-lit, intimate, a little old-world. Competitors default to
app-blue or loud gradients. A warm, literary palette immediately signals *"this is about stories."*

**The feeling to protect on every new screen:** nothing cold, corporate, or rushed. Generous
spacing, serif type for anything narrative, and one warm accent doing all the work.

---

## 2. Visual Design System

### 2.1 Color tokens

| Token | Hex | Use |
| --- | --- | --- |
| `maroon-950` | `#150A0D` | Reader night background, deepest surface |
| `maroon-900` | `#2A1015` | App shell background, cover art base |
| `maroon-800` | `#3A1820` | Cards, chapter rows |
| `maroon-700` | `#452029` | Elevated cards, format toggles, pressed states |
| `maroon-600` | `#4A2A32` | Borders, dividers, progress track |
| `saffron` | `#F2A93B` | The single accent — CTAs, unlocks, play, progress fill, active states |
| `text-primary` | `#F5EDE7` | Primary text on dark surfaces |
| `text-muted` | `#C7A8A0` | Secondary/meta text |
| `reader-light-bg` | `#F5EDE7` | Reader Day Mode background (warm off-white, never stark white) |
| `reader-light-ink` | `#2A1015` | Reader Day Mode ink — mirrors the dark theme background |

**Rule:** no other hues anywhere. `saffron` stays rare enough that it always means
"actionable or important" — never decorative, never a large fill.

### 2.2 Typography

| Role | Face | Notes |
| --- | --- | --- |
| Narrative (body, synopsis, titles) | **Noto Serif** | Strong Devanagari/Tamil/Bengali coverage — must look native, not translated |
| UI chrome (buttons, nav, labels) | **Inter** | Clean, functional, gets out of the way |

> **Implementation note:** Android's platform `serif` family *is* Noto Serif, and platform
> `sans-serif` is metric-close to Inter. The app maps to platform families so it works offline with
> zero font downloads. Swap point is a single `FontFamily` value in `ui/theme/Type.kt` if we later
> bundle real Inter/Noto Serif files.

**Type scale**

- Series title — 24sp / Serif SemiBold
- Chapter title (reader) — 19sp / Serif SemiBold
- Section label ("18 Chapters", "Editor's Picks") — 13sp / Serif SemiBold, `text-primary`
- Body / chapter text — 15–24sp user-adjustable / Serif Regular, line-height **1.85**
- UI body (buttons, chips, meta) — 12–13.5sp / Inter Regular or Medium
- Micro (word counts, durations) — 10.5–11.5sp / Inter Regular, `text-muted`

### 2.3 Spacing, radius, elevation

- **Spacing scale:** 4 / 8 / 12 / 14 / 16 / 18 / 22 / 26 / 32 dp
- **Radius:** 12dp rows & chips · 16–18dp cards & covers · pill (20dp+) for coin balance, unlock
  buttons, format toggles
- **Elevation:** flat. One soft `1dp solid maroon-600` border instead of shadows. Cards are objects
  on a warm surface, not floating panels. **Borders over shadows** is the visual grammar — do not
  add drop shadows.

### 2.4 Motion

- Standard transitions: 180–220ms fade/slide, no bounce.
- **Signature animation:** chapter unlock → a warm saffron shimmer sweeps across the chapter row
  (600–800ms), then settles. This is the one place to spend extra polish. Everything else stays quiet.
- Scrubber fill animates in real time during playback, no jank.
- Reader chapter change: soft horizontal slide. Modern, not a skeuomorphic 3D page flip.

### 2.5 Component library

| Component | Spec |
| --- | --- |
| Primary button | `saffron` fill, `maroon-950` text, pill or 12dp radius, bold Inter |
| Secondary / ghost | transparent fill, `maroon-600` border, `text-primary` text |
| Chip / tag | `maroon-800` fill, `text-muted` text, fully rounded |
| Coin pill | `maroon-800` fill, `maroon-600` border, saffron coin glyph + bold count |
| Card | `maroon-800` fill, `maroon-600` 1dp border, 16dp radius, 14–16dp padding |
| Input | `maroon-800` fill, `maroon-600` border, `saffron` border on focus (visible focus always) |
| Progress / scrubber | `maroon-600` track, `saffron` fill, 4dp height, 2dp radius |

---

## 3. Broad-Appeal Features (first-class, not bolted on)

A story app only earns mass adoption if it works beyond the urban, tech-comfortable user.

- **Reader font size + line spacing + high-contrast mode** — accessibility, not a cosmetic toggle.
- **Data-saver mode** — lower audio bitrate on mobile data, full quality on Wi-Fi, manual override.
  Text chapters load near-instantly even on 2G.
- **Full UI localization** per selected language — menus, buttons, onboarding copy, not just stories.
- **Offline-first** — downloaded text and audio work with zero connectivity.
- **Large touch targets** — minimum 44×44dp everywhere.
- **Family / Safe filter** — hides mature genres (horror, hard crime) from discovery entirely.
- **Low-storage mode** — compressed audio downloads for budget devices.

---

## 4. The AI + Human Production Pipeline (internal)

This determines whether the app feels genuinely good or like generic AI filler. It is core product
architecture, not a backend detail.

1. **Story bible (AI-drafted, human-approved).** Characters, speech patterns, setting, full arc,
   twists, emotional beats — approved by a human editor *before* any chapter is generated. A flawed
   premise produces flawed chapters no matter how good the prose gets later. The approved bible is
   fed into every chapter step, which is what fixes "AI forgets the character by chapter 20."
2. **Chapter drafting (AI-assisted).** Grounded in the bible plus the *full text* of the previous
   2–3 chapters. Written directly in the target language, never translated from an English draft.
3. **Human editorial pass (non-negotiable gate).** Every chapter, checked for repetitive/hollow
   prose, emotional pacing, whether the cliffhanger actually lands, dialect authenticity, and
   continuity. Editors **edit**, they don't rubber-stamp. Budget real people and real time.
4. **Narration.** Real human voice artists at launch. AI TTS in regional Indian languages still
   sounds synthetic and voice quality drives retention. TTS is a Phase 3 catalog-expansion option
   only, always labeled honestly.
5. **Limited curated release.** 10–20 genuinely strong series across 2–3 genres and 2 languages.
   Catalog size must never outrun editorial capacity.

**Biggest risk:** quality drift. It is easy to start excellent and slowly slip while chasing volume.
Protect the editorial gate even when it's tempting to publish faster. Also run similarity/plagiarism
checks on bibles and chapters — models can reproduce recognizable copyrighted elements.

---

## 5. Every Screen, In Full

### 5.1 Splash & Onboarding
- **Splash:** wordmark in serif on `maroon-900`, brief saffron glow on load.
- **Language:** grid of chips (multi-select). UI localizes immediately on selection.
- **Genres:** chip grid (Romance, Thriller, Mythology, Comedy, Horror, Family Drama, Crime), min 3.
- **Curated starter picks:** 3–4 hand-picked series from the selections — cold start solved by real
  curation, not an empty library.
- **States:** loading (skeleton chips), error (retry if language list fails).

### 5.2 Home
- Top bar: wordmark left · coin pill + search right.
- **Continue** row — horizontal cards with a thin saffron progress bar along the cover's bottom edge.
- **Editor's Picks** row — explicitly labeled; reinforces curation-over-volume.
- Genre rows (horizontally scrollable), **New This Week** row.
- **Empty state:** brand-new user → Continue row doesn't render at all; Editor's Picks becomes top.
- **Loading:** skeleton cards matching final dimensions (no layout shift). Pull-to-refresh.

### 5.3 Search / Discover
- Search field with live results: series, genres, narrators.
- Filter chips: Genre · Language · Free-to-start · Completed only.
- Recent searches before typing.
- **Empty:** "No stories found — try a different word, or browse genres below" + genre grid fallback.

### 5.4 Series Detail
- Cover, title, genre/language/rating meta row, synopsis.
- Chapter list with lock states and per-chapter Text/Audio format icons.
- "Start Series" / "Continue Chapter X" CTA pinned near the top, not buried in the list.
- **Production note:** "AI-assisted story, human-edited and narrated." Transparency builds trust.
- Loading skeleton; error state with retry, never a blank screen.

### 5.5 Text Reader
- Distraction-free; tap to reveal controls (font size, day/night, chapter nav).
- Reading position auto-saves continuously, not just on exit.
- **High-contrast toggle** alongside day/night.
- **Swipe left/right** between chapters, with a "Chapter complete" micro-moment (small saffron check)
  before advancing.

### 5.6 Audio Player
- Full controls, 15s skip, human-narrator credit shown honestly.
- **Sleep timer** (15/30/45/60 min or end-of-chapter).
- **Playback speed** (0.8×–2×) as a real control, not a static label.
- Swipe down to minimize into a persistent mini-player while browsing.

### 5.7 Coin Wallet & Purchase
- Balance prominent at top, transaction history below.
- Tiers (50/150/500) with bonus messaging, "Best value" on the largest, via Google Play Billing.
- **Unlock confirmation modal** shows chapter title, cost, and resulting balance. Never silently deduct.

### 5.8 Profile & Library
- Tabs: In Progress · Completed · Saved.
- Downloaded content manager (what's stored offline, free up space).
- Notification preferences per followed series.
- Accessibility settings surfaced here as real, discoverable controls.

### 5.9 Notifications
- New chapter for followed series · low coin balance (one line, gentle, dismissible) · editorial picks.

### 5.10 Settings
- Language (content + UI) · accessibility · data-saver · family-safe filter · account/logout · help.

---

## 6. Firestore Schema

```text
users            id, phone_hash, displayName, uiLanguage, contentLanguages[], genreInterests[],
                 coinBalance, familySafeMode, dataSaverMode, fontSizeDefault, highContrastMode,
                 createdAt

series           id, title, synopsis, coverUrl, genre, language, totalChapters,
                 status(ongoing|completed), isEditorsPick, ratingAvg, productionNote,
                 maturityRating, createdAt

chapters         id, seriesId, chapterNumber, title, textContent, audioUrl, narratorName,
                 narrationType(human|ai_tts), durationSeconds, wordCount, unlockCost,
                 isFreePreview, createdAt

progress         id, userId, seriesId, chapterId, format(text|audio),
                 positionSeconds | positionCharIndex, lastAccessedAt

unlocks          id, userId, chapterId, coinsCost, unlockedAt

coinTransactions id, userId, type(purchase|spend|referral_bonus), amount, createdAt

reviews          id, seriesId, userId, rating, reviewText, createdAt

notifications    id, userId, type(new_chapter|low_coins|editors_pick), payload, isRead, createdAt

downloads        id, userId, chapterId, format, localSizeBytes, downloadedAt

storyBibles      id, seriesId, characters[], plotArc, toneNotes, approvedBy, approvedAt
                 -- INTERNAL production tooling only, never read by the consumer app
```

**Security rules to write before launch:** `coinBalance`, `unlocks`, and `coinTransactions` must be
writable **only** by Cloud Functions. A client that can write its own coin balance is a client that
can mint free chapters. Chapter `textContent` / `audioUrl` must be readable only when a matching
`unlocks` doc exists or `isFreePreview` is true — enforce server-side, not in the UI.

---

## 7. Tech Stack

- **Client:** Android native · Kotlin · Jetpack Compose · Material 3
- **Auth:** Firebase Phone OTP
- **Data:** Firestore (catalog, user, progress) + Cloud Functions (coin spend, unlock grants)
- **Audio:** Firebase Storage + CDN (audio is small enough; no video-style transcoding pipeline)
- **Payments:** Google Play Billing
- **Push:** Firebase Cloud Messaging
- **Generation pipeline:** server-side/offline editorial tool, entirely separate from the app

---

## 8. Build Order

**Phase 1**
1. Firebase setup, Phone OTP auth
2. Onboarding: language (UI localizes from here on), genres, curated starter picks
3. **Design system as a reusable Compose theme — before any screen**
4. Home: Continue, Editor's Picks, genre rows, New This Week + skeletons + empty states
5. Search/Discover with filter chips and genre-grid fallback
6. Series Detail: cover, meta, synopsis, chapter list, lock states, format toggles, production note
7. Text Reader: font size, high-contrast, day/night, auto-save, chapter-complete moment, swipe nav
8. Audio Player: controls, sleep timer, speed picker, narrator credit, mini-player
9. Coin Wallet: balance, Play Billing tiers, unlock confirmation modal

**Phase 2**
10. Profile/Library tabs + download manager
11. Notifications screen + FCM wiring
12. Settings: accessibility, data-saver, family-safe, language
13. Ratings/reviews
14. Referral coin rewards

---

## 9. Quality Bar

- Every screen handles **loading, empty, and error** explicitly. Never a blank screen with no
  explanation.
- Every spend action shows exact cost and resulting balance before confirming.
- Font-size / high-contrast / data-saver settings apply **globally**, not only inside the Reader.
- Test on 3 screen sizes and both reader themes before calling a screen done.
- Serif for narrative, Inter for chrome — no mixing.
- Touch targets ≥ 44dp. Focus states always visible.

---

## 10. Business Context

**Monetization:** coin-based chapter unlocks (primary) · coin packages via Play Billing ·
subscription tier once catalog justifies it (Phase 3) · referral coin bonuses.

**Content team minimum:** 1–2 editors/writers per active language · 2–4 narrators per language
(freelance to start) · 1 person owning the generation pipeline and prompts.
Realistic pace: ~1 chapter per series per editor per day once smooth — which is exactly why the
launch target is 10–20 series, not 200.

**Differentiators vs. KukuFM / Pocket FM / Pratilipi:** curated catalog over content-farm volume ·
dual format (same story as text chapter *and* narrated episode, switchable mid-story) · story-bible
continuity tracking built into generation · per-language dialect/idiom editorial pass so vernacular
content reads native rather than translated.

**Honest risks:** quality drift as catalog scales · copyright similarity in generated drafts ·
well-funded incumbents with large catalogs (our wedge is quality + dual format, not volume) ·
narration is a real recurring per-chapter operating cost · a polished MVP with 10–15 produced series
in 2 languages is realistically 4–6 months with app and content built in parallel.

---

## 11. What You Must Set Up (not code)

1. **Firebase project** with Firestore, Auth (Phone), Storage, Functions, Cloud Messaging enabled →
   drop `google-services.json` into `app/`.
2. **Google Play Console** developer account with Play Billing and coin SKUs configured.
3. **Android SDK location** for local builds — set `ANDROID_HOME` or add `sdk.dir` to
   `local.properties` (keep that file out of git).
4. **Content production team** producing the first 10–15 series in parallel with the app build, so
   launch isn't an empty library.
