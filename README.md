# NexOS — APK downloads

This branch (`apk`, no slash in name → guaranteed-stable URLs) hosts the **latest** signed APKs for [NexOS](https://github.com/AryanSahay1/APK).

## ⬇ Latest stable downloads

| Build | File | Size | Direct download |
|---|---|---|---|
| **Release** (recommended) | `NexOS-latest-release.apk` | 54 MB | **[Download](https://github.com/AryanSahay1/APK/raw/apk/NexOS-latest-release.apk)** |
| Debug | `NexOS-latest-debug.apk` | 60 MB | **[Download](https://github.com/AryanSahay1/APK/raw/apk/NexOS-latest-debug.apk)** |

## Pinned to v1.5.0 (won't change)

| Build | Size | Direct download |
|---|---|---|
| Release v1.5.0 | 54 MB | https://github.com/AryanSahay1/APK/raw/apk/NexOS-v1.5.0-release.apk |
| Debug v1.5.0 | 60 MB | https://github.com/AryanSahay1/APK/raw/apk/NexOS-v1.5.0-debug.apk |

## What's new in v1.5.0 — "Notepad Reimagined"

A ground-up rewrite of the note editor inspired by the Vivo Notes layout — focused, tactile, and full of texture.

**New editor layout**
- Large **Title** field, then a meta row showing **day · date · time · character count** that ticks live as you type.
- Plain-language body with a **bottom toolbar** of seven actions: `Aa` text style · alignment & lists · checkbox · reminder · image · mic · more (`⋮`).
- Top bar gains **undo**, **redo**, and **save** with proper enabled/disabled tints.

**Rich formatting (markdown-flavoured)**
- The `Aa` sheet exposes a font-size slider (12–28 sp) and chips for **Bold**, **Italic**, **Underline**, **Strikethrough**, and **Highlight**. The toolbar wraps the current selection (or the caret) in the matching token. Saved notes render the formatting back when you open them.
- The alignment sheet handles **left / center / right** for the whole body plus quick-insert chips for **bulleted** and **numbered** lists.
- The `⋮` menu inserts a **markdown table** at the caret.
- Type `[ ]` at the start of a line for a **checkbox**; saved notes render it as a real `Checkbox` composable.

**Doodle pad**
- Tap `⋮ → Doodle / Drawing` for a freehand canvas with five colours, a stroke-width slider, undo, and clear.
- Save attaches the doodle as a PNG inside the note.

**Camera capture**
- `⋮ → Take photo` fires the system camera into a `FileProvider`-backed URI; the result lands in the note as a normal image attachment.

**30 panda-themed backgrounds**
- `⋮ → Background theme` opens a 3-column grid of **30 hand-drawn, pure-Compose surfaces**: sleeping panda, bamboo grove, paw trails, leaves, hearts, dots, diagonals, circles, triangles, starry sky, ocean waves, zen rings, panda tile, micro pandas, soft gradients, sunset horizon, confetti, falling leaves, moon-and-stars, sun rays, rain, snowflakes, cherries, clouds, boba, panda parade, and more.
- Each design is drawn from primitives at note-display time — zero PNG assets, zero added APK weight.
- Backgrounds persist per-note and follow the note into the detail view.

**Detail view alignment**
- The detail view now respects the note's chosen **alignment**, **font size**, and **background**, and renders the body through the new markdown engine (headings, lists, checkboxes, tables, inline styling).

**Under the hood**
- Room v4 → v5: `notes` table gains `backgroundId`, `textAlignment`, `bodyTextSizeSp` (all default to a no-op).
- New `util/MarkdownRenderer.kt` — a small, dependency-free renderer covering the exact token set the editor emits, so round-trips are perfect by construction.
- New `presentation/ui/components/PandaBackgrounds.kt` — 30 deterministic `DrawScope` renderers (seeded by id, identical pixels every launch).
- New `presentation/ui/components/DoodlePad.kt` — freehand drawing + raster-to-PNG for attachment.
- Single-undo + single-redo body history capped at 50 steps in `EditNoteViewModel`.

**Carried over from v1.4.0**
- Live location weather strip above the panda on the Notes home.
- Image / voice / location attachments per note.
- Notebook mode with cover + back designer and PDF export.
- 5 user-pickable Google Fonts.
- Panda assistant FAB hidden on the Notes tab (no overlap with `+ New note`).

**Known limitations**
- Inline rich-text styling round-trips through markdown tokens (so the underlying text contains `**`, `*`, `__`, etc.). That's intentional — keeps undo/redo and IME compatibility intact — and a future release can add an opt-in WYSIWYG overlay.
- Region-only screenshot capture is still deferred; the next release will pair a transparent drag-to-crop activity with the existing MediaProjection stream.

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
- [`v1.4.0`](https://github.com/AryanSahay1/APK/tree/v1.4.0) — Rich Notes & Notebooks
- [`v1.5.0`](https://github.com/AryanSahay1/APK/tree/v1.5.0) — Notepad Reimagined (current)

## Licence

Apache License 2.0. See [LICENSE](https://github.com/AryanSahay1/APK/blob/main/LICENSE).
