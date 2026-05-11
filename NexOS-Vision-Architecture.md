# NexOS — Complete Vision & Architecture Document
### Android AI Orchestration Layer | v1.0 MVP

---

## PART 1 — THE VISION

### What NexOS Actually Is

Most people who build AI apps on Android build a chatbot with a text field and an API call. They wrap GPT, slap a logo on it, and call it an assistant. That is not what NexOS is.

NexOS is an **execution layer that lives on top of Android**. It is not a standalone app that competes with other apps. It is a shadow system that watches, listens, reads, and acts — sitting between the user and every other app on their phone. Think of it the way a conductor thinks about an orchestra. The conductor does not play an instrument. The conductor tells every instrument when to play, at what speed, and in what sequence. NexOS is the conductor. WhatsApp, Notes, Gmail, Chrome, Google Docs — these are the instruments.

The product vision in one sentence: **An Android AI layer that can understand what a user is doing, capture what they are seeing, hear what they are saying, and convert all of it into structured, searchable, actionable knowledge — automatically, privately, and without requiring a subscription.**

This is not a feature. This is a category.

---

### Why This Has Not Been Built Properly Yet

Android is the largest mobile operating system on the planet with over 3 billion active devices. And yet there is no product that truly orchestrates Android at the system level for the average user. Here is why:

**Android is fragmented.** Samsung, Xiaomi, OnePlus, Realme, Motorola — each OEM ships a modified version of Android with different permission behaviors, different background task handling, and different battery optimization strategies. A service that runs perfectly on a Pixel will be silently killed on a Xiaomi within two minutes. This fragmentation is not a bug. It is the moat. Most developers give up. You do not give up — you engineer around it.

**Android is permission-heavy.** To do anything meaningful — read the screen, draw over other apps, capture audio, access the file system, project the display — you need permissions that Android grants reluctantly and revokes aggressively. Most apps never ask for these permissions because they are afraid of user rejection. NexOS asks for them upfront, explains exactly why each one is needed, and builds its entire value proposition on them.

**AI on mobile has no standard execution model.** Google has Gemini. Apple has Siri. But neither of them lets a developer say "understand this screen and take the next action." They are consumer products, not platforms. The developer tools for true on-device AI execution — understanding UI hierarchy, interacting with arbitrary apps, chaining multi-step workflows — are fragmented across Accessibility Services, ML Kit, MediaProjection, and Intent APIs. Nobody has connected them cleanly. That is what NexOS does.

**The privacy problem kills cloud-first approaches.** Any app that sends your screen contents or voice recordings to a server is one data breach away from being uninstalled by every privacy-conscious user. NexOS is local-first. The core functionality — screen capture, OCR, voice transcription, note storage — runs entirely on-device. AI summarization is optional and powered by the user's own API key. NexOS never sees your data. That is not a limitation. That is the feature.

---

### The Three Laws of NexOS

Every product decision, architecture choice, and feature trade-off must be evaluated against these three laws:

**Law 1 — It must work without AI.** The moment a user cannot get value from NexOS without an API key, NexOS has failed. The local mode — screenshot capture, OCR text extraction, voice transcription, note storage, search — must be complete, polished, and genuinely useful. AI is an upgrade, not a requirement.

**Law 2 — It must never crash silently.** An AI agent that fails invisibly is worse than an agent that does not exist. Every operation must emit a clear state — processing, success, or failed with a specific reason. The user must always know what NexOS is doing and why. Silent failures destroy trust permanently.

**Law 3 — It must respect the phone's resources.** Battery life and memory are the user's most valuable mobile assets. NexOS must not drain battery in the background, must not consume excessive RAM, and must not schedule unnecessary network calls. Every background service must justify its existence. When not actively processing, NexOS must go quiet.

---

### The MVP Scope — Productivity Focus

For the first version, NexOS does three things exceptionally well:

**Screenshot to Structured Note.** The user sees something on their screen — a webpage, a WhatsApp message, a PDF, a recipe, a job posting — and taps the floating NexOS button. The screen is captured instantly. ML Kit reads every word on that screen. If an AI API key is configured, the raw text is sent to the AI provider, which returns a clean title, a set of bullet points summarizing the key information, and a one-sentence summary. If no API key is configured, the raw extracted text is saved with an auto-generated title taken from the first meaningful line. The note is saved to the local Room database, a system notification confirms the save, and the floating button returns to its idle state. Total time: under 5 seconds.

**Voice to Structured Note.** The user has a thought, an idea, a meeting insight, a task they need to remember. They long-press the NexOS floating button. A bottom sheet slides up with an animated microphone waveform. Android's SpeechRecognizer API begins transcribing in real time, showing partial results as the user speaks. When the user stops speaking — or taps to stop — the final transcript is processed through the same AI pipeline as the screenshot flow, producing a clean structured note. The note is saved locally. Total time: under 3 seconds after speaking.

**Manual Note with Search.** A simple, fast manual note entry interface. Title field, content field, optional tags. All notes — regardless of source — are stored in the same Room database and are instantly searchable. Search filters by title and content in real time as the user types. Notes can be deleted with swipe-to-delete, shared via Android's native share sheet, and exported as Markdown or plain text files.

---

## PART 2 — SYSTEM ARCHITECTURE

### The Five Layers

NexOS is structured as five independent layers that communicate through well-defined interfaces. Each layer can be developed, tested, and debugged in isolation. They connect only at the orchestration layer — Module 10 in the build sequence. Understanding each layer independently is critical because Cursor AI will build them one at a time.

---

### LAYER 1 — The Input Layer

This layer is responsible for one thing: getting information from the user into the system. It does not process the information. It does not store it. It captures it and hands it to the next layer.

The Input Layer has three channels:

**Channel A — Visual Input via MediaProjection.** When the user taps the floating button, the ScreenshotService is triggered. This service holds a reference to a MediaProjection object that was obtained when the user first granted screen capture permission. The service creates an ImageReader and a VirtualDisplay, captures a single frame from the display, converts the ImageReader's Image object into a Bitmap, releases the VirtualDisplay and ImageReader immediately to free GPU memory, and returns the Bitmap to the caller. This entire operation happens on a background thread. The main thread is never blocked. The returned Bitmap is the raw pixel data of whatever was on the user's screen at the moment of capture.

The ScreenshotService runs as a ForegroundService with a persistent notification. This is not optional. Android 8.0 and above requires any service that performs long-running operations to show a notification. MediaProjection additionally requires the `foregroundServiceType="mediaProjection"` attribute in the AndroidManifest. Without this attribute, the service crashes on Android 10 and above. The notification reads "NexOS is active" with a subtitle showing the last action taken. This notification is not dismissible by the user — it exists to comply with Android's transparency requirements for screen capture.

**Channel B — Audio Input via SpeechRecognizer.** When the user long-presses the floating button, the VoiceInputManager is activated. This manager uses Android's built-in SpeechRecognizer — not a third-party API, not a cloud service — which means voice transcription works offline and costs nothing. The SpeechRecognizer must be created and destroyed on the main thread. This is an Android requirement that Cursor must be explicitly instructed to follow. Creating the SpeechRecognizer on a background thread produces an immediate crash with no meaningful error message. The VoiceInputManager emits a Flow of VoiceState sealed class instances: Idle, Listening, Partial (with live text), Result (with final text), and Error. The VoiceInputBottomSheet composable observes this flow and updates its UI accordingly — the waveform animation reacts to the user's voice amplitude in real time, and partial transcription results appear as text below the waveform. When the SpeechRecognizer emits a final result, the VoiceInputManager emits VoiceState.Result, the bottom sheet dismisses itself, and the final text is passed to the Orchestrator.

**Channel C — Manual Text Input.** The simplest channel. A Compose screen with two text fields — title and content — and a save button. No special Android APIs required. This channel exists to make NexOS useful even when the floating button and camera are unavailable. The EditNoteScreen uses standard Jetpack Compose TextField components with the NexOS dark theme applied. Auto-save on back navigation prevents accidental data loss.

---

### LAYER 2 — The Intent Engine

The Intent Engine receives raw input from the Input Layer — a Bitmap, a transcribed string, or a typed string — and determines what to do with it. In the MVP, the Intent Engine is simple: all inputs go through OCR or transcription and then to AI summarization. In future versions, the Intent Engine will classify inputs into different action types: save note, send message, set reminder, open app, search the web. For now, the classification is hardcoded to "save as structured note."

The Intent Engine's primary component for the MVP is the **NoteAIHelper**, which contains the prompt templates. These prompts are carefully engineered to produce consistent, parseable JSON output. The screenshot summary prompt instructs the AI to analyze raw OCR text and return a JSON object with three fields: title (under 8 words), bullets (3 to 6 key points as an array of strings), and summary (one sentence). The voice summary prompt is identical in structure but tuned for transcribed speech, which tends to be more conversational and less structured than screen text.

The JSON constraint is critical. If the AI returns markdown prose, the parsing logic breaks. If it returns a JSON object wrapped in markdown code fences, the parsing logic breaks. The prompts must explicitly state "Respond ONLY with valid JSON. No markdown. No preamble. No explanation. Only the JSON object." Even with this instruction, AI providers occasionally violate it. The ParsedNote parser must strip markdown fences if present before attempting JSON parsing, and must return null rather than crashing if the JSON is malformed.

---

### LAYER 3 — The OCR Engine

The OCR Engine sits between the Input Layer and the Intent Engine for the visual input channel. It receives a Bitmap and returns an OcrResult. It does not make decisions. It does not communicate with AI. It converts pixels to text.

NexOS uses ML Kit Text Recognition — Google's on-device OCR library — for all text extraction. ML Kit runs entirely on the device using a locally downloaded model. There is no network call. There is no API key. There is no cost per recognition. The model handles printed text in Latin scripts with high accuracy. For handwritten text, accuracy degrades significantly — this is acceptable for the MVP.

The OcrEngine class exposes one primary function: `suspend fun extractText(bitmap: Bitmap): OcrResult`. Internally, it creates an InputImage from the Bitmap, calls the TextRecognizer, waits for the result via a coroutine, extracts all text blocks and lines, calculates an average confidence score, and passes the raw text through the TextCleaner utility before returning. TextCleaner removes excessive whitespace, collapses multiple blank lines, trims each line, and handles common OCR artifacts like stray characters at block boundaries.

The OcrResult includes both the raw unprocessed text and the cleaned text. The raw text is stored for debugging. The cleaned text is what gets sent to the AI provider or saved directly to the database in non-AI mode.

One critical implementation detail: ML Kit's TextRecognizer must be explicitly closed after use to release the native model from memory. Forgetting to call `recognizer.close()` creates a memory leak that eventually crashes the app on devices with limited RAM. The OcrEngine must always close the recognizer in a finally block.

---

### LAYER 4 — The AI Orchestration Layer

This is the most architecturally significant layer. It is the component that separates NexOS from every other note-taking app. But it is also the layer that must be most carefully constrained to avoid creating a product that requires a paid subscription to be useful.

**The AI Interface.** All AI providers implement a single interface: `AIProvider`. This interface has two functions: `suspend fun complete(prompt: String, maxTokens: Int): AIResponse` and `suspend fun testConnection(): Boolean`. Every provider — OpenAI, Gemini, Anthropic, Groq — implements this interface identically. The calling code (NoteAIHelper) never knows which provider is active. It calls `AIRouter.getActiveProvider().complete(prompt)` and receives an AIResponse. This abstraction means the user can switch providers without any code change — only the settings screen changes which implementation AIRouter returns.

**The Provider Implementations.** Four providers are implemented for the MVP. Groq is the recommended starting point for development because it offers a free tier with generous limits and uses the same API format as OpenAI, making it the easiest to implement. The Llama 3 8B model on Groq is fast, capable, and free for reasonable usage volumes. OpenAI's GPT-4o Mini is the best balance of cost and quality for users who want to pay for a key. Google Gemini 1.5 Flash is Google's most cost-efficient model. Anthropic's Claude Haiku is Anthropic's fastest and cheapest model. All four are implemented from day one so users can choose based on their existing accounts.

**The NoOpProvider.** This is the most important provider and it costs nothing. When no API key is configured, AIRouter returns a NoOpProvider instance. NoOpProvider implements the AIProvider interface and always returns `AIResponse(text = "", isSuccess = false, error = "No AI provider configured")`. This means the calling code — NoteAIHelper and the Orchestrator — never needs to check whether AI is enabled. It just calls the provider. The Orchestrator checks `response.isSuccess` and, if false, falls back to saving the raw cleaned OCR text with an auto-generated title. The app works. The note is saved. The user has something useful. They just do not have a polished AI summary.

**The API Key Security Model.** API keys are secrets. They must never be stored in SharedPreferences (unencrypted), DataStore (unencrypted), hardcoded in source code, logged to Logcat, or stored in BuildConfig fields. NexOS uses EncryptedSharedPreferences from the AndroidX Security library. This wraps a standard SharedPreferences file with AES-256-GCM encryption, using a key stored in the Android Keystore — a hardware-backed secure enclave present on all modern Android devices. The encryption and decryption happen transparently. From the developer's perspective, it looks like a regular SharedPreferences. The difference is that the data file on disk is encrypted and cannot be read by other apps or by someone who physically extracts the device storage.

**The Request Timeout Strategy.** AI API calls can hang indefinitely if the network is slow or the provider is experiencing high load. Every API call must be wrapped in a `withTimeout(30_000L)` coroutine scope. If 30 seconds elapse without a response, the coroutine is cancelled and the Orchestrator receives a TimeoutCancellationException, which it catches and converts to a failed AIResponse. The user sees the note saved with raw text rather than waiting indefinitely for a summary that may never arrive.

---

### LAYER 5 — The Storage and Persistence Layer

All data in NexOS is local. There is no backend. There is no authentication. There is no sync. This is an intentional design decision for the MVP that has four advantages: zero infrastructure cost, zero privacy risk from server-side data exposure, zero onboarding friction (no login required), and full offline functionality.

**Room Database.** The Note entity is the only database table in the MVP. It has nine fields: an auto-generated Long primary key, a String title, a String content, a String summary (empty in non-AI mode), a String sourceType (screenshot, voice, or manual), a Long timestamp, a String tags field storing comma-separated tag strings, a Boolean isSynced flag reserved for future cloud sync, and a String rawImagePath storing the cache path to the original screenshot bitmap if applicable.

The NoteDao interface exposes six operations: insert (returning the new row ID), update, deleteById, getAllNotes (as Flow ordered by timestamp descending), searchNotes (as Flow with LIKE query on title and content), and getRecentNotes (as Flow with a limit parameter). All functions that modify data are suspend functions running on Dispatchers.IO. All functions that return data return Flow — a reactive stream that automatically emits new values whenever the underlying data changes. This means the UI updates instantly when a note is saved, deleted, or modified, without any manual refresh logic.

The database is a singleton. A single NexosDatabase instance is shared across the entire app lifecycle. With Hilt, this is provided as a `@Singleton`-scoped binding in the DatabaseModule. Creating multiple Room database instances for the same file causes corruption.

**DataStore for Settings.** Non-sensitive user preferences — which AI provider is selected, whether auto-summarize is enabled, whether the floating button is shown, which screen edge the button snaps to — are stored in Preferences DataStore. DataStore is the modern replacement for SharedPreferences, built on Kotlin coroutines and Flow. Every preference is a typed key defined in a `NexosPreferences` object. The SettingsRepository reads and writes these preferences as suspend functions and exposes them as Flow for the SettingsViewModel to observe.

**Image Cache Management.** When a screenshot is captured, the Bitmap is temporarily saved to the app's cache directory before OCR processing. This path is stored in the Note entity's `rawImagePath` field. The Settings screen provides a "Clear image cache" button that deletes all files in the cache directory. The cache is not essential — it exists only so users can view the original screenshot that generated a note. Future versions could use this for visual note search.

---

### THE ORCHESTRATION LAYER — NexosOrchestrator

The Orchestrator is the nervous system. It is the one component that knows about all other components and coordinates them. Every other component is isolated — the OcrEngine does not know about the AI providers, the ScreenshotService does not know about Room, the FloatingButtonService does not know about the NoteRepository. Only the Orchestrator connects them.

**The Screenshot Flow.** When NexosReceiver receives the `ACTION_CAPTURE_SCREENSHOT` broadcast from the FloatingButtonService, it calls `NexosOrchestrator.handleScreenshotCapture()`. The Orchestrator emits `WorkflowState.Capturing` and signals the FloatingButtonService to enter its processing animation state. It then calls `ScreenshotService.captureScreen()` and awaits the Bitmap. If the Bitmap is null — the screen capture failed — it emits `WorkflowState.Failed("Screen capture failed", "capture")` and exits. The FloatingButtonService enters its error state. The flow is over.

If the Bitmap is not null, the Orchestrator emits `WorkflowState.ExtractingText` and calls `OcrEngine.extractText(bitmap)`. If OCR fails — returns `isSuccess = false` — the Orchestrator saves the note with an empty content field and a title of "Screenshot [timestamp]" rather than failing entirely. A partial save is better than no save.

If OCR succeeds, the Orchestrator checks `AIRouter.isAiEnabled()`. If AI is enabled, it emits `WorkflowState.AiProcessing` and calls the AI provider with the summary prompt. If the AI response is successful, it parses the JSON into a ParsedNote and uses its fields for the note title, content (bullets formatted as a string), and summary. If AI fails, it falls back to the cleaned OCR text. In either case, it emits `WorkflowState.Saving` and calls `NoteRepository.insertNote(note)`. On successful save, it emits `WorkflowState.Done(note)`. The FloatingButtonService receives Done and enters its success animation. A system notification fires: "Note saved: [title]" with a tap action that opens the NoteDetailScreen for that specific note.

**The Voice Flow.** Nearly identical to the screenshot flow, but starts with the transcribed text string instead of a Bitmap. There is no OCR step. The text goes directly to the AI provider (if enabled) or is saved as raw text (if disabled). The VoiceInputBottomSheet dismisses itself when the Orchestrator emits Done.

---

### BACKGROUND SERVICE ARCHITECTURE

Android's background service system is one of the most complex and fragmentation-prone parts of the platform. NexOS uses two services: FloatingButtonService and ScreenshotService.

**FloatingButtonService** manages the floating overlay button using WindowManager. It adds a custom View to the window using `TYPE_APPLICATION_OVERLAY` — the correct type for API 26 and above. The button is draggable using MotionEvent.ACTION_MOVE in a touch listener. On release (ACTION_UP), it calculates which screen edge the button is closest to and animates to that edge using a spring animation. The last position is saved to DataStore and restored on service restart. The service is a ForegroundService that must call `startForeground()` within five seconds of starting. Its notification is low-priority (no sound, no vibration) to avoid annoying the user.

The FloatingButtonService communicates with the Orchestrator through LocalBroadcastManager. When the user taps the button, the service sends a local broadcast with action `ACTION_CAPTURE_SCREENSHOT`. When the user long-presses, it sends `ACTION_START_VOICE`. NexosReceiver picks up these broadcasts and calls the Orchestrator. The Orchestrator sends state updates back to the FloatingButtonService through a SharedFlow that the service observes in a coroutine started in `onStartCommand`. This one-way communication model keeps the service and the business logic fully decoupled.

**ScreenshotService** holds the MediaProjection reference. This service is started when the user first grants screen capture permission. The permission grant dialog fires automatically the first time the user attempts a screenshot. The `onActivityResult` callback in MainActivity receives the `resultCode` and `resultData` from the MediaProjection permission dialog and passes them to ScreenshotService via an Intent extra. The service stores these values and uses them to create MediaProjection instances on demand. Each screenshot capture creates a new ImageReader, a new VirtualDisplay, captures one frame, and immediately destroys both. The MediaProjection itself persists between captures.

---

### HILT DEPENDENCY INJECTION GRAPH

Hilt manages the instantiation and lifetime of all major components. The dependency graph for the MVP looks like this:

The `NexosDatabase` is provided as a `@Singleton` by `DatabaseModule`. The `NoteDao` is provided by calling `database.noteDao()`, also as a `@Singleton`. The `NoteRepository` depends on `NoteDao` and is provided as a `@Singleton` by `RepositoryModule`. The `NoteAIHelper` is provided as a `@Singleton`. The `AIRouter` depends on `SecureStorage` and is provided as a `@Singleton`. The `OcrEngine` depends on `@ApplicationContext` and is provided as a `@Singleton`. The `NexosOrchestrator` depends on `NoteRepository`, `OcrEngine`, `AIRouter`, and `NoteAIHelper`, and is provided as a `@Singleton`. The `SettingsRepository` depends on `DataStore` and is provided as a `@Singleton`.

All ViewModels use `@HiltViewModel` and receive their dependencies via constructor injection. `NotesViewModel` depends on `NoteRepository`. `SettingsViewModel` depends on `SettingsRepository` and `AIRouter`. Services use `@AndroidEntryPoint` and field injection.

The `@ApplicationContext` annotation is critical throughout. Services and repositories must always hold a reference to the Application context — never an Activity context. Storing an Activity context in a long-lived object like a Repository or ViewModel causes a memory leak because the Activity cannot be garbage collected as long as something holds a reference to it.

---

### NAVIGATION ARCHITECTURE

NexOS uses Jetpack Navigation Compose with a simple flat navigation graph. There are four destinations: `noteList` (home), `noteDetail/{noteId}`, `editNote/{noteId}` (with -1 indicating a new note), and `settings`. The NavHost is defined in MainActivity with spring-physics enter and exit transitions on all routes. There is no bottom navigation bar in the MVP — the single screen serves as the home, and Settings is accessed via a TopBar icon.

Deep links are registered for the system notification: tapping the "Note saved" notification navigates directly to `noteDetail/{noteId}` where `noteId` is the ID of the newly created note. This deep link is handled in MainActivity's `onNewIntent` callback, which calls `navController.navigate("noteDetail/$noteId")`.

---

### TESTING STRATEGY

**Unit testing** covers: OcrResult parsing, TextCleaner string manipulation, NoteAIHelper JSON prompt construction, ParsedNote JSON deserialization, and AIResponse error handling. These are pure Kotlin functions with no Android dependencies and can be tested with JUnit4 on the JVM without an emulator.

**Instrumentation testing** covers: Room database operations (insert, query, delete, search), EncryptedSharedPreferences read/write, and WorkflowState emissions from NexosOrchestrator with mocked dependencies.

**Manual testing on real device** covers: ScreenshotService MediaProjection capture, FloatingButtonService overlay visibility and drag behavior, SpeechRecognizer activation and transcription, end-to-end screenshot-to-note flow, and OEM-specific battery optimization behavior. The minimum test devices are a stock Android (Pixel), a Samsung (OneUI), and a Xiaomi (MIUI) — these three cover the majority of real-world OEM behavior differences.

---

## PART 3 — WHAT COMES AFTER MVP

The MVP is not the product. The MVP is the proof. Once the screenshot-to-note and voice-to-note flows work reliably on real devices, the architecture already supports expanding in three directions:

**Direction 1 — More Input Channels.** Clipboard monitoring (save anything you copy), notification listener (save incoming messages), share sheet integration (receive content from any app). All of these slot into the Input Layer without changing the Intent Engine, OCR Engine, or Storage Layer.

**Direction 2 — App-Specific Automation.** The Accessibility Service layer — which is already declared in the manifest but not used in MVP — enables NexOS to read the UI hierarchy of any app and interact with it programmatically. This is how "send the note to WhatsApp" or "create a task in Notion from this note" gets built. The Action DSL — a JSON format for executable step sequences — makes these automations debuggable, shareable, and eventually marketplace-ready.

**Direction 3 — Semantic Search.** Once notes accumulate, simple LIKE search becomes insufficient. A vector embedding of each note's content — stored locally using SQLite-Vec or a similar on-device vector store — enables semantic search: "notes about the marketing meeting last week" finds relevant notes even if they do not contain the exact word "marketing." This is where on-device AI models (via ONNX Runtime or MediaPipe) become relevant.

None of these directions require rewriting the MVP. They require extending it. That is the correct architecture.

---

## CURSOR AI USAGE INSTRUCTIONS FOR THIS DOCUMENT

When starting a new Cursor session to implement any component described in this document, begin your prompt with:

> "I am building NexOS — an Android AI orchestration app. The package name is com.nexos.ai. The language is Kotlin. The UI framework is Jetpack Compose. The minimum SDK is 26. The architecture is MVVM with Repository pattern and Hilt for dependency injection. I am about to implement [COMPONENT NAME]. Here is what it needs to do based on my architecture document: [paste the relevant section from this document]."

This gives Cursor the full context it needs to generate code that fits the existing architecture rather than inventing its own patterns. Never start a Cursor session with "build me an Android app" — always start with the specific component, the specific interface it needs to implement, and the specific constraints it must follow. Precision in the prompt produces precision in the code.

---

*NexOS Architecture Document v1.0*
*For use with Cursor AI — paste relevant sections into each build session*
