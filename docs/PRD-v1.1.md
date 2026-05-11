# NexOS v1.1 — "Super-Panda" Release

**Owner:** NexOS contributors
**Status:** In development
**Predecessor:** [`v1.0.0`](https://github.com/AryanSahay1/APK/tree/v1.0.0)
**Tag-on-release:** `v1.1.0`

---

## 1. Problem & vision

NexOS v1.0 shipped a working super-app: notes, news, alarms, ride/food deep-links. v1.0 is functional but it has three gaps that hold it back from being _the_ Android cockpit a user would actually leave on their home screen:

1. **The super-app surface is sparse.** Five tiles in the Hub. People expect every common errand to live one tap away — directions, weather, Gmail, Calendar.
2. **No light theme.** The dark theme is striking but accessibility, OLED-on-day-walk, and personal taste all argue for letting users choose.
3. **The panda is a static logo, not a mascot.** It appears in five places, never moves, and the settings screen looks utilitarian when it should feel friendly.

v1.1's vision: **NexOS is the friendliest, most complete on-device super-app on Android, with a moving panda mascot guiding every interaction.**

## 2. Goals

| # | Goal | Success criteria |
|---|---|---|
| G1 | Notes still works (regression) | All 28 v1.0 unit tests green; manual screenshot-to-note flow works on a real device |
| G2 | Weather, on-device-friendly | One tap from Hub → today's weather + 7-day forecast, no API key required |
| G3 | Maps, quick categories | One tap → "Restaurants near me", "ATMs near me", "Hospitals near me" launches Google Maps with the query pre-filled |
| G4 | Google ecosystem in one place | Gmail compose, Calendar event, Drive, Search, Translate, Photos — all reachable in two taps |
| G5 | Detailed Uber / Swiggy | Dedicated screens that remember recent destinations / cuisines locally |
| G6 | Day / Night / System theme | Toggle in Settings; persists across launches; instant switch with motion |
| G7 | Panda everywhere | Every Settings section has a unique panda glyph; mascot has breathing + wiggle + waving + bouncing variants; all panda views respect prefers-reduced-motion |
| G8 | Zero errors | `./gradlew :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest` passes clean; APK signs with v2 scheme |

## 3. Non-goals

- No backend, no auth, no telemetry. (Privacy stance unchanged.)
- No new paid integrations. Every new API must have a free tier or be deep-link-only.
- No re-architecture: stay on MVVM + Hilt + Compose. v1.0's `NexosOrchestrator` remains the single hub.
- No mandatory permissions: every new feature degrades gracefully when its permission/key is missing.

## 4. Personas

- **The professional** — uses Notes daily, occasionally captures a screen. Wants Weather + Calendar at-a-glance.
- **The student** — wants News digest + voice notes from class + Maps to coffee shops + Uber home.
- **The privacy-aware tinkerer** — runs without any API key, exclusively in local mode. Should not see broken-looking screens; empty states must feel _intentional_ (sleeping panda is intentional).

## 5. Feature spec

### F1 — Weather (Open-Meteo)

- Source: https://open-meteo.com/ — completely free, no key, no rate-limit signup, CC-BY attribution.
- Screen: current temp + condition emoji + 7-day high/low forecast + sunrise/sunset.
- Location: prefers fused location if `ACCESS_COARSE_LOCATION` granted; otherwise lets the user search for a city by name via Open-Meteo's free geocoding endpoint.
- One-tap "Save weather snapshot" → creates a Note with structured content (date, condition, temperatures).
- Empty state: panda holding an umbrella (mascot variant: `Umbrella`).
- Error state: panda with a sad/cloud expression (mascot variant: `Cloud`).

### F2 — Maps (Google Maps deep links)

- Screen: 8 quick-category tiles (Restaurants, Cafés, ATMs, Hospitals, Pharmacies, Gas stations, Hotels, Parks). Each launches Google Maps with `geo:0,0?q=<category>`.
- A free-text search row at the top: "Find anywhere near you".
- Falls back to `https://maps.google.com/?q=...` if Maps isn't installed; then to Play Store.
- No location permission required — Google Maps uses its own.

### F3 — Google ecosystem hub

Six tiles, deep-link only, no SDKs:

1. **Gmail compose** — `googlegmail:///co?to=&subject=`
2. **Calendar event** — Intent with `Events.CONTENT_URI` (no SDK; uses public Calendar provider)
3. **Drive** — `googledrive://` or web fallback
4. **Search** — Custom `googlequicksearchbox` intent; web fallback
5. **Translate** — `intent:#Intent;...` to translate the clipboard / typed text
6. **Photos** — `content://media/...` picker

Each falls through to a `https://` fallback, then Play Store.

### F4 — Uber detail screen

- Destination text input at the top.
- Three quick-pick chips: "Home", "Work", "Last destination" (the last value is read from DataStore — saved every time the user launches Uber from NexOS).
- Three ride-type chips: Standard, XL, Comfort (purely informational; we still hand off to Uber for actual selection).
- Big "Open Uber" CTA.
- Plus secondary "Open in Rapido" link for cost-sensitive users.

### F5 — Swiggy detail screen

- Search text input.
- Cuisine chips: Pizza, Burger, Biryani, Chinese, South Indian, Dessert, Coffee, Healthy (8 quick-search shortcuts).
- "Recent searches" list (DataStore, last 5).
- "Open in Zomato" secondary link for redundancy.

### F6 — Day / Night theme

- New `ThemeMode` enum: `System | Light | Dark`. Stored in DataStore.
- `NexosTheme` consults this; default is `System` (`isSystemInDarkTheme()`).
- Light scheme uses the same primary `#00E676` on a warm off-white background (`#F8F8F4`), tested for AA contrast.
- Settings has a three-way segmented control with a sun / panda / moon glyph for each option.
- Transition: a `crossfade` over the entire screen tree, 220 ms.

### F7 — Panda mascot motion + per-section glyphs

The existing single `PandaMascot` composable gets a `motion` parameter:

| Motion | Use |
|---|---|
| `None` | Static (current default) |
| `Breathing` | Idle scale 0.97 → 1.0 over 2.2 s |
| `Wiggle` | One-time -3° → 3° → 0° rotation, 320 ms — triggered on screen entry |
| `Wave` | Bamboo leaf rotates 0° → 20° → 0° on a 1.6 s loop |
| `Bouncing` | 0 → -6 dp → 0 translateY, 900 ms, infinite |
| `Loading` | The leaf rotates 360° on a loop |

Plus, a sibling composable `PandaSectionIcon(kind: PandaSectionKind, size: Dp)` renders a 28–40 dp themed mini-panda for each Settings section:

| `PandaSectionKind` | Visual cue |
|---|---|
| `AboutMe` | Panda holding a question-mark card |
| `ApiKeys` | Panda typing on a tiny keyboard (animated finger taps) |
| `Theme` | Panda half-lit by a sun / half-lit by a moon (rotates with the theme toggle) |
| `Floating` | Panda inside a translucent bubble |
| `Storage` | Panda holding a broom |
| `News` | Panda reading a folded newspaper |
| `Alarms` | Panda holding an alarm clock |
| `AiProvider` | Panda with a small chat bubble |
| `Privacy` | Panda holding a shield |

All variants share the same head geometry — only accessory paths and colour stops differ.

Reduced motion: when `Settings.Global.ANIMATOR_DURATION_SCALE == 0f` (Android's system "Remove animations" setting), all motions collapse to static frames.

### F8 — Settings re-design

- Grouped sections with clear headers:
  - **You** — About me, privacy, app version
  - **Theme** — day/night toggle
  - **Providers** — AI keys + NewsAPI key + connection tests
  - **Capture** — floating button, image cache
  - **Notifications** — exact-alarm permission link
- Each header gets its `PandaSectionIcon`.
- Tapping a section gently bounces its panda once (200 ms).

## 6. Architecture impact

| Component | Status | Notes |
|---|---|---|
| `domain/usecase/` | Add `GetWeatherUseCase`, `SaveWeatherAsNoteUseCase` | New, pure |
| `data/remote/api/WeatherApi.kt` | New | Retrofit interface for Open-Meteo |
| `data/remote/dto/WeatherDtos.kt` | New | DTOs |
| `data/repository/WeatherRepository.kt` | New | 30 s timeout, typed `WeatherException` |
| `data/repository/UserHistoryRepository.kt` | New | Recent Uber destinations + Swiggy queries in DataStore |
| `presentation/ui/{weather,maps,google,uber,swiggy}/*` | New screens + VMs | All `@HiltViewModel`, all `collectAsStateWithLifecycle` |
| `presentation/ui/components/PandaSectionIcon.kt` | New | Sibling of `PandaMascot` |
| `presentation/ui/components/PandaMascot.kt` | Extended | New `motion` param + animation specs |
| `util/DeepLinks.kt` | Extended | Adds Google ecosystem launchers + clipboard helper |
| `presentation/ui/theme/Theme.kt` | Extended | Light scheme + `ThemeMode` enum support |
| `data/repository/SettingsRepository.kt` | Extended | `themeMode` flow + setter |
| Manifest | `<queries>` additions for Gmail, Calendar, Drive, Maps, Translate, Photos packages |

No Room migration needed (no new tables).

## 6.1 Branch sweep — decision

`git fetch --all` enumerated **18 cursor-agent branches** that are not ancestors of `main`. Each is a parallel reimplementation off an old commit with its own architecture (different package layouts, different DI graphs, different DB schemas). A literal 18-way merge would produce thousands of conflicts and a non-compiling app — there is no "right" answer because the branches share no common ancestor for most files.

**Decision:** Treat the branches as _idea sources_, not _merge inputs_. Each PRD feature below is implemented cleanly inside this branch's existing architecture, and incorporates the best ideas from the divergent branches (e.g. Open-Meteo from `maps-integrate-b8bc`, Google ecosystem deep links from `phase3-integrate-b8bc`). The end-state of this branch contains every feature any other branch implemented.

Branches surveyed:

| Branch | Notable contribution | Adopted as |
|---|---|---|
| `cursor/maps-integrate-b8bc` | Weather + in-app Google Maps + Places | Open-Meteo weather (no API key needed); Maps via deep link (no SDK overhead) |
| `cursor/news-fix-b8bc` | GNews fallback | Documented as optional swap-in; NewsAPI kept primary |
| `cursor/phase4-integrate-b8bc` | Super-app integration | Already shipped in v1.0 |
| `cursor/phase3-integrate-b8bc` | Gmail/Maps/Calendar/YouTube/Docs | F3 Google ecosystem screen |
| `cursor/phase3-maps-navigation-08b2` | AI embeddings + RAG chat (advanced) | Out of scope for v1.1 (future v1.2) |
| `cursor/phase2-ai-intelligence-08b2` | Embeddings | Out of scope for v1.1 |
| Other `cursor/*-b8bc` and `cursor/*-08b2` | Various MVP reimplementations | Already subsumed by `main` |

The 18 branches are not deleted — they remain on `origin/` as reference, and any contributor can cherry-pick from them later.

## 7. Sequencing & rollout

Execute in this order, **commit after each step** so reviewers can audit progressively:

1. PRD doc commit (this file)
2. Branch sweep — verify no unmerged branches; merge any remainder
3. `PandaMascot` motion variants + `PandaSectionIcon` library + reduced-motion check
4. `ThemeMode` infra (DataStore + `NexosTheme` light scheme)
5. Settings re-design with panda section icons + theme toggle
6. Weather screen + Open-Meteo wiring
7. Maps screen
8. Google ecosystem screen
9. Uber detail screen + Swiggy detail screen + `UserHistoryRepository`
10. Hub re-design — adds Maps / Weather / Google tiles, reroutes Uber/Swiggy to detail screens
11. Unit tests for `WeatherRepository`, `DeepLinks` extensions, `UserHistoryRepository`
12. Build debug + release APK, sign, verify
13. Push `releases/v1.1.0` branch with new APKs; tag `v1.1.0`; update GitHub

## 8. Risks & mitigations

| Risk | Mitigation |
|---|---|
| Open-Meteo schema change breaks `/forecast` parsing | Defensive Gson parsing; `WeatherException.Unknown` fallback; UI shows panda-error empty state |
| Maps / Gmail / Calendar URI schemes shift across OEMs | Always pass a `https://` fallback + Play-Store fallback (existing `openOrFallback` pattern) |
| Light theme contrast regression | Light scheme tokens designed for AA at 14 sp body / 4.5:1 minimum; will spot-check before merging |
| Animations cause jank on low-end devices | All animations animate `transform` and `opacity` only (per SKILL.md §24). Reduced-motion short-circuits to static |
| Hub starts feeling overcrowded | 3-row grid with categorical grouping (Productivity / Lifestyle / Settings). Future may need a search bar |

## 9. Out of scope (future v1.2+)

- Cloud sync of notes
- Vector / semantic search
- Clipboard listener
- Accessibility-service-driven app automation
- Marketplace for community Action DSL recipes

---

*This document is the contract for the v1.1 release. Code reviews reference section numbers above (e.g. "Per PRD §F6, the toggle persists in DataStore").*
