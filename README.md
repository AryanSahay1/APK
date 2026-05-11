# NexOS — APK downloads

This branch (`apk`, no slash in name → guaranteed-stable URLs) hosts the **latest** signed APKs for [NexOS](https://github.com/AryanSahay1/APK).

## ⬇ Latest stable downloads

| Build | File | Size | Direct download |
|---|---|---|---|
| **Release** (recommended) | `NexOS-latest-release.apk` | 54 MB | **[Download](https://github.com/AryanSahay1/APK/raw/apk/NexOS-latest-release.apk)** |
| Debug | `NexOS-latest-debug.apk` | 60 MB | **[Download](https://github.com/AryanSahay1/APK/raw/apk/NexOS-latest-debug.apk)** |

## Pinned to v1.4.0 (won't change)

| Build | Size | Direct download |
|---|---|---|
| Release v1.4.0 | 54 MB | https://github.com/AryanSahay1/APK/raw/apk/NexOS-v1.4.0-release.apk |
| Debug v1.4.0 | 60 MB | https://github.com/AryanSahay1/APK/raw/apk/NexOS-v1.4.0-debug.apk |

## What's new in v1.4.0 — "Rich Notes & Notebooks"

A focused upgrade of the Notes experience plus a live-weather header and a typeface picker.

**Notes upgrade**
- **Image attachments** — system photo picker; original URI persisted (no copy bloat).
- **Voice memos** — record AAC/M4A through a fresh `MediaRecorder` pipeline; inline play/pause card on the note detail view.
- **Location pins** — one tap drops your current coarse location into a note, reverse-geocoded via Nominatim so it reads as a friendly address, not coords.
- **Notebooks** — flip any note into a notebook cover, attach other notes as pages, design the cover + back page (background swatch, accent swatch, motif: panda+leaf / gridlines / confetti / plain), then **export the whole thing as a PDF** to `Documents/NexOS/`.

**Live weather strip**
- Pinned at the top of the Notes home **above the panda mascot**, refreshes every 10 minutes.
- Uses the device's coarse location when granted; falls back to your last-saved city otherwise. Tap "Use my location" to enable.

**Typeface picker**
- Pick from 5 Google Fonts — Inter (default), Lora, Roboto Slab, Quicksand, JetBrains Mono.
- Fonts download on-demand via Google Play Services; nothing extra bundled in the APK.

**Layout + cleanup**
- Panda assistant FAB hidden on the Notes tab (no more overlap with the "+ New note" button); reach it by tapping the panda mascot in the Notes top bar.
- Removed Rapido and Zomato from the Hub.

**Under the hood**
- Room v3 → v4: `notes` table gains `attachmentsJson`, `isNotebook`, `notebookId`, `coverDesignJson`, `isNotebookCompleted` — all backward-compatible.
- New `NoteAttachment` sealed model (Image / Audio / Location), hand-rolled JSON codec for forward-compat.
- New `LocationProvider` wraps system `LocationManager` (no Play Services FusedLocation dep).
- New `NotebookPdfExporter` uses `android.graphics.pdf.PdfDocument` — no third-party PDF library.
- M3 Expressive theme refresh: tertiary palette (warm peach + burnt-orange) populated, secondary/tertiary container roles wired up, scrim + surfaceTint added, shape scale aligned to spec.

**Known limitations (deferred to v1.5)**
- Region-only screenshot capture: still captures the full screen. A custom drag-to-crop overlay would need a dedicated transparent Activity on top of the projection — tracked for the next release.

## Installation

```bash
adb install NexOS-latest-release.apk
```

## All NexOS releases

- [`v1.0.0`](https://github.com/AryanSahay1/APK/tree/v1.0.0) — MVP
- [`v1.1.0`](https://github.com/AryanSahay1/APK/tree/v1.1.0) — Super-Panda
- [`v1.1.1`](https://github.com/AryanSahay1/APK/tree/v1.1.1) — GNews seeded
- [`v1.1.2`](https://github.com/AryanSahay1/APK/tree/v1.1.2) — OpenWeather attempt
- [`v1.2.0`](https://github.com/AryanSahay1/APK/tree/v1.2.0) — Pandas Everywhere
- [`v1.3.0`](https://github.com/AryanSahay1/APK/tree/v1.3.0) — Fluffy Panda Assistant
- [`v1.4.0`](https://github.com/AryanSahay1/APK/tree/v1.4.0) — Rich Notes & Notebooks (current)

## Licence

Apache License 2.0. See [LICENSE](https://github.com/AryanSahay1/APK/blob/main/LICENSE).
