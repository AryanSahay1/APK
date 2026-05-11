# NexOS — APK downloads

This branch (`apk`, no slash in name → guaranteed-stable URLs) hosts the **latest** signed APKs for [NexOS](https://github.com/AryanSahay1/APK).

## ⬇ Latest stable downloads

| Build | File | Size | Direct download |
|---|---|---|---|
| **Release** (recommended) | `NexOS-latest-release.apk` | 54 MB | **[Download](https://github.com/AryanSahay1/APK/raw/apk/NexOS-latest-release.apk)** |
| Debug | `NexOS-latest-debug.apk` | 60 MB | **[Download](https://github.com/AryanSahay1/APK/raw/apk/NexOS-latest-debug.apk)** |

## Pinned to v1.3.0 (won't change)

| Build | Size | Direct download |
|---|---|---|
| Release v1.3.0 | 54 MB | https://github.com/AryanSahay1/APK/raw/apk/NexOS-v1.3.0-release.apk |
| Debug v1.3.0 | 60 MB | https://github.com/AryanSahay1/APK/raw/apk/NexOS-v1.3.0-debug.apk |

## What's new in v1.3.0 — "Fluffy Panda Assistant"

A new floating-action button on every tab opens a giant fluffy panda holding a tablet. The tablet IS the chat box; the per-context glyph in the centre (sun ☀ / cloud ☁ / rain 🌧 / newspaper 📰 / envelope ✉ / alarm clock ⏰ / map pin 📍 / food bowl 🍜 / chat 💬) tells you what mode the panda is in.

**Specialised handlers** (all reachable from the same chat — paste anything, the panda figures out what to do):

| Say to the panda | What happens |
|---|---|
| "Weather" | Calls Open-Meteo, shows current temperature + condition, updates the tablet glyph to match (sun / cloud / rain / etc.) |
| "Top news" or "news about [topic]" | Calls GNews, returns top 3 headlines inline |
| "Help me email mom about Christmas plans" | AI drafts `{ to, subject, body }`, you tap "Open Gmail with this draft" |
| "Remind me at 8am tomorrow to call doctor" | Parses time + title locally, schedules via AlarmManager (survives reboot) |
| Paste a `https://maps.app.goo.gl/...` or `https://api.whatsapp.com/...location=lat,lng` link | Extracts coords, reverse-geocodes via OpenStreetMap Nominatim (free, no key), returns a clean written address with "Copy address" and "Open in Maps" buttons |
| "I'm feeling tired and stressed" (in Food context) | AI suggests a comfort cuisine, you tap "Open Swiggy with 'ramen'" — falls back to a local mood→cuisine map if no AI key is configured |

**Visual changes**:
- New `FluffyPanda` composable: bigger, fuzzy ring of "fur" spokes around the head, visible body, two paws gripping a tablet, context-aware glyph inside the tablet.
- Global panda FAB on Notes / News / Hub.
- Context auto-derived from the current screen: tap the FAB on Weather → Weather panda, on Alarms → Alarm panda, on Swiggy → Food panda, etc.

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
- [`v1.3.0`](https://github.com/AryanSahay1/APK/tree/v1.3.0) — Fluffy Panda Assistant (current)

## Licence

Apache License 2.0. See [LICENSE](https://github.com/AryanSahay1/APK/blob/main/LICENSE).
