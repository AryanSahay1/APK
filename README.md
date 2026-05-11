# NexOS — Android AI Orchestration Layer

> An on-device AI execution layer for Android. Capture, transcribe, summarize — all
> without a subscription, all without your data leaving the device.

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

Source-available for evaluation. No commercial license granted by this repository.
