# NexOS — Android AI Orchestration Layer

> An on-device AI execution layer for Android. Capture, transcribe, summarize, schedule,
> read the news, and book a ride — all from one app, all without a subscription,
> all without your data leaving the device.

**Licensed under [Apache 2.0](LICENSE). Free, forever. See [UPDATES_POLICY.md](UPDATES_POLICY.md) and [PRIVACY.md](PRIVACY.md).**

NexOS is not a chatbot wrapper. It's a system that sits between the user and every
other app on their phone: a floating button captures the screen, ML Kit extracts the
text, an optional user-supplied AI key turns it into a structured note, and Room
stores everything locally.

## The Three Laws

1. **It must work without AI.** The local mode is complete and useful on its own.
2. **It must never crash silently.** Every operation emits an explicit state.
3. **It must respect the phone's resources.** Quiet when idle. No drain.

## Architecture

Five independent layers connect only through the orchestrator:

| Layer | Module | Responsibility |
|---|---|---|
| Input | `service/ScreenshotService.kt`, `voice/VoiceInputManager.kt` | Bitmap, transcript, or typed text |
| Intent | `ai/NoteAIHelper.kt` | Prompt builders + JSON parser |
| OCR | `ocr/OcrEngine.kt`, `ocr/TextCleaner.kt` | ML Kit on-device text extraction |
| AI | `ai/AIRouter.kt`, `ai/*Provider.kt`, `ai/SecureStorage.kt` | OpenAI / Gemini / Anthropic / Groq / NoOp |
| Storage | `data/local/*`, `data/repository/*` | Room + DataStore |

The orchestrator (`util/NexosOrchestrator.kt`) is the single component that knows
about every other layer. Services emit broadcasts → `NexosReceiver` calls the
orchestrator → the orchestrator emits `WorkflowState` back to the UI and services.

## Tech stack

- Kotlin 1.9.22, Jetpack Compose, Material 3
- Hilt DI · Room · DataStore · EncryptedSharedPreferences
- ML Kit Text Recognition (on-device, no API key)
- OkHttp + Gson for AI providers
- minSdk 26 · targetSdk 34 · package `com.nexos.ai`

## Building

```bash
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

```bash
./gradlew :app:testDebugUnitTest
```

The first build downloads the Compose BOM (~120 MB) and ML Kit (~40 MB). Subsequent
builds are incremental.

## API keys (optional)

NexOS works without an API key. To enable AI summaries, open **Settings → AI
provider**, pick a provider, and paste your key. Keys are encrypted with the
Android Keystore (`androidx.security:security-crypto`) and never logged or
transmitted to anything but the provider you selected.

Supported providers:

- **Groq** — recommended starting point. Free tier, OpenAI-compatible.
- **OpenAI** — `gpt-4o-mini`
- **Google Gemini** — `gemini-1.5-flash`
- **Anthropic** — `claude-haiku-4-5`
- **None** — local-only mode (raw OCR / transcripts saved verbatim)

## Permissions

| Permission | Why |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Floating button overlay |
| `FOREGROUND_SERVICE_MEDIA_PROJECTION` | Screen capture |
| `RECORD_AUDIO` | Voice notes (on-device transcription) |
| `POST_NOTIFICATIONS` | Save confirmations |
| `INTERNET` | Only used when AI provider is configured |

All permissions are requested at the point of use, with rationale.

## Phase 4 — Super-app features

The Hub tab brings the rest of the device under one roof. All integrations are free; deep
links use only the public URI schemes each target app publishes.

| Feature | Trigger | Underlying tech | Cost |
|---|---|---|---|
| News feed (categorised, searchable, save-as-note with AI summary) | News tab | NewsAPI (you supply free dev key) + ML Kit + your AI provider | Free tier — 100 req/day |
| Alarms & reminders ("remind me at 8am tomorrow") | Hub → Alarms | Local NLP parser + `AlarmManager.setExactAndAllowWhileIdle` + `BootReceiver` | Free, fully on-device |
| Uber ride | Hub → Uber | `uber://?action=setPickup…` deep link | Free |
| Rapido ride | Hub → Rapido | `rapido://book?destination=…` deep link | Free |
| Zomato food | Hub → Zomato | `zomato://search?query=…` deep link | Free |
| Swiggy food | Hub → Swiggy | `swiggy://search?query=…` deep link | Free |

Every deep link gracefully degrades: if the target app is not installed, NexOS opens the
provider's HTTPS site, then falls back to the Play Store listing.

## Repo layout

```
app/src/main/java/com/nexos/ai/
├── MainActivity.kt
├── NexosApp.kt
├── ai/             # AIProvider, AIRouter, *Provider, NoteAIHelper, SecureStorage
├── data/local/     # Room entity + dao + database
├── data/repository/# NoteRepository, SettingsRepository
├── di/             # Hilt modules
├── domain/model/   # AIResponse, ParsedNote, OcrResult, WorkflowState
├── ocr/            # OcrEngine, TextCleaner
├── presentation/   # Compose UI, navigation, ViewModels, theme
├── service/        # FloatingButtonService, ScreenshotService, NexosReceiver
├── util/           # Constants, NexosOrchestrator, NotificationHelper, Extensions
└── voice/          # VoiceInputManager
```

## License

[Apache License 2.0](LICENSE). You may use, modify, and redistribute the source and the APK,
including for commercial purposes, subject to the conditions of the licence. Trademarks for
Uber, Rapido, Zomato, Swiggy, OpenAI, Anthropic, Gemini, and Groq belong to their respective
owners — see [NOTICE](NOTICE). Privacy stance: [PRIVACY.md](PRIVACY.md). Update policy:
[UPDATES_POLICY.md](UPDATES_POLICY.md).
