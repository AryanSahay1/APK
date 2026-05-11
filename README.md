# NexOS — Android AI Orchestration MVP

An Android AI orchestration layer that lives on top of the OS. Tap the
floating button → it captures the screen, runs OCR, optionally summarises
through your own AI key, and saves a structured note. Long-press the button
to dictate a voice note instead. Manual notes have full-text search.

Everything runs **locally** by default — no backend, no login, no telemetry.
AI summarisation is an optional upgrade that uses your own API key (OpenAI,
Anthropic, Gemini, or Groq). NexOS itself never sees the key or the note.

This repo implements the MVP described in
[`NexOS-Vision-Architecture.md`](NexOS-Vision-Architecture.md) under the
governance rules in [`SKILL.md`](SKILL.md).

---

## Requirements

| Tool            | Version |
|-----------------|---------|
| Android Studio  | Hedgehog (2023.1.1) or newer |
| JDK             | 17 (Temurin recommended) |
| Android SDK     | Platform 34, Build-Tools 34.0.0 |
| Min SDK         | 26 (Android 8.0) |
| Target SDK      | 34 (Android 14) |

The Gradle wrapper at `./gradlew` pins Gradle 8.4.

---

## Build & run

```bash
# Debug APK
./gradlew :app:assembleDebug

# Install on a connected device / emulator
./gradlew :app:installDebug

# Run JVM unit tests
./gradlew :app:testDebugUnitTest

# Android Lint
./gradlew :app:lintDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

> **Important:** `MediaProjection` and the floating overlay button do not
> work on the standard Android emulator in a reliable way. Test on a real
> device (any phone running Android 8+ will do).

---

## Permissions

NexOS asks for these permissions on first launch (or from the **Settings**
screen):

- **Display over other apps** (`SYSTEM_ALERT_WINDOW`) — for the floating
  capture bubble.
- **Microphone** (`RECORD_AUDIO`) — for voice notes.
- **Notifications** (`POST_NOTIFICATIONS`, API 33+) — for "Note saved"
  confirmations.
- **Ignore battery optimisations** (recommended) — prevents OEMs from
  killing the floating-button service.

Screen capture uses Android's standard `MediaProjection` permission, which
is granted via the OS confirmation dialog the first time you tap the
camera FAB or the floating button.

---

## Configuring AI providers (optional)

1. Open **Settings** from the top-bar gear icon.
2. Pick a provider under **AI**. **Groq** has the most generous free tier
   (Llama 3 8B) and is the recommended starting point.
3. Paste your API key into the input field and tap **Save key**.
4. Tap **Test connection** to verify.

API keys are persisted only via `EncryptedSharedPreferences` (AES-256-GCM)
keyed by the Android Keystore. They never appear in plain text storage,
log output, or build artefacts.

Toggle **Auto-summarize with AI** off if you want to keep the raw OCR /
transcript text instead of running it through the model.

---

## Architecture

NexOS is organised into five independent layers (full breakdown in
[`NexOS-Vision-Architecture.md`](NexOS-Vision-Architecture.md)):

```
                              ┌─────────────────────────────┐
                              │   FloatingButtonService     │   overlay bubble
                              │   ScreenshotService         │   MediaProjection
                              │   VoiceInputManager         │   SpeechRecognizer
                              └─────────────┬───────────────┘
                                            │ broadcasts / coroutines
                                            ▼
                              ┌─────────────────────────────┐
                              │   NexosOrchestrator         │ WorkflowState
                              └──────┬──────────┬───────────┘
                                     │          │
                ┌────────────────────┘          └──────────────────┐
                ▼                                                  ▼
┌───────────────────────────┐                       ┌────────────────────────────┐
│ OcrEngine (ML Kit)        │                       │ AIRouter → AIProvider      │
│ TextCleaner               │                       │  • NoOpProvider (default)  │
└──────────┬────────────────┘                       │  • OpenAI / Groq / Gemini  │
           │                                        │  • Anthropic               │
           ▼                                        │  + NoteAIHelper            │
                                                    │  + ParsedNoteParser        │
                                                    └─────────────┬──────────────┘
                                                                  │
                                                                  ▼
                                              ┌───────────────────────────────────┐
                                              │ NoteRepository → NoteDao          │
                                              │  → Room (NexosDatabase)           │
                                              │ SettingsRepository → DataStore    │
                                              │ SecureStorage → Keystore-backed   │
                                              └───────────────────────────────────┘
```

**The Three Laws** of NexOS, enforced everywhere:

1. **It must work without AI.** The `NoOpProvider` returns
   `isSuccess = false` whenever no key is set; the orchestrator falls back
   to saving the cleaned OCR/transcript with an auto-generated title.
2. **It must never crash silently.** Every operation emits a
   `WorkflowState` so the floating button and in-app HUD can show
   progress — including failures with a reason and step name.
3. **It must respect the phone's resources.** Foreground services use
   `IMPORTANCE_LOW` channels; `MediaProjection` is torn down after every
   single capture in the documented order; `ImageReader` row-stride
   padding is cropped immediately so bitmaps don't keep extra memory.

---

## Project layout

```
app/src/main/java/com/nexos/ai/
├── MainActivity.kt                 single-Activity host + NavHost
├── NexosApp.kt                     @HiltAndroidApp, creates notif channels
├── ai/                             AI abstraction (interface, prompts, router)
│   └── providers/                  OpenAI / Groq / Gemini / Anthropic / NoOp
├── data/
│   ├── local/                      Room: NexosDatabase + NoteDao + entity
│   ├── repository/                 NoteRepository, SettingsRepository
│   ├── secure/                     SecureStorage (EncryptedSharedPreferences)
│   ├── settings/                   NexosPreferences (DataStore keys)
│   └── mapper/                     Entity ↔ domain mappers
├── domain/model/                   Pure-Kotlin models: Note, OcrResult,
│                                   AIResponse, ParsedNote, WorkflowState
├── di/                             Hilt modules (DatabaseModule)
├── ocr/                            OcrEngine, TextCleaner
├── voice/                          VoiceInputManager, VoiceState
├── service/                        FloatingButtonService, ScreenshotService,
│                                   NexosReceiver
├── orchestrator/                   NexosOrchestrator
├── presentation/
│   ├── navigation/                 NexosDestinations
│   ├── viewmodel/                  Notes / NoteDetail / EditNote / Settings VMs
│   └── ui/
│       ├── notes/                  NoteListScreen, NoteDetailScreen, EditNoteScreen
│       ├── settings/               SettingsScreen
│       ├── components/             NoteCard, StatusHud, VoiceInputBottomSheet
│       └── theme/                  Color, Type, Theme
└── util/                           Extensions, NexosActions, NexosChannels,
                                    ScreenCaptureBridge, NavBridge
```

---

## Testing

JVM unit tests cover the pieces with no Android dependencies — exactly
what `SKILL.md §16` calls for:

- `TextCleanerTest` — whitespace, blank-line, and zero-width handling.
- `ParsedNoteParserTest` — JSON parsing, markdown fences, malformed input.
- `AIPromptsTest` — prompt templates contain raw text and JSON-only
  guard-rails.
- `ExtensionsTest` — `toAutoTitle` and `toRelativeTimeString`.

Run with `./gradlew :app:testDebugUnitTest`.

Manual testing on a real device is still required for the
`MediaProjection`, overlay, and `SpeechRecognizer` paths.

---

## What's intentionally out of scope (per the architecture doc)

- Cloud sync, accounts, server-side anything.
- AccessibilityService automation, the Action DSL, marketplace.
- Semantic search / on-device vector embeddings.
- Clipboard monitor and notification-listener input channels.

The layers are structured so each of these slots in without rewriting the
MVP — see Part 3 of the architecture document.

---

## License

This repository is the personal NexOS implementation by the project owner.
No license is granted for redistribution; treat it as private source.
