# NexOS — APK downloads

This branch (`apk`, no slash in name → guaranteed-stable URLs) hosts the **latest** signed APKs for [NexOS](https://github.com/AryanSahay1/APK).

## ⬇ Latest stable downloads

| Build | File | Size | Direct download |
|---|---|---|---|
| **Release** (recommended) | `NexOS-latest-release.apk` | 54 MB | **[Download](https://github.com/AryanSahay1/APK/raw/apk/NexOS-latest-release.apk)** |
| Debug | `NexOS-latest-debug.apk` | 60 MB | **[Download](https://github.com/AryanSahay1/APK/raw/apk/NexOS-latest-debug.apk)** |

## Pinned to v1.2.0 (won't change)

| Build | Size | Direct download |
|---|---|---|
| Release v1.2.0 | 54 MB | https://github.com/AryanSahay1/APK/raw/apk/NexOS-v1.2.0-release.apk |
| Debug v1.2.0 | 60 MB | https://github.com/AryanSahay1/APK/raw/apk/NexOS-v1.2.0-debug.apk |

## What's new in v1.2.0 — "Pandas Everywhere"

### Fixes
- 🌤️ **Weather works for everyone, with or without a key** — removed the invalid OpenWeather seed that returned HTTP 401, now falls back to Open-Meteo (free, no key) automatically on every error path.
- 🛵 **Rapido** actually opens now — destination copied to clipboard, Rapido app launched, falls back to Google Maps if Rapido isn't installed.
- 🍽️ **Zomato** receives the search query — switched to HTTPS App-Link path which Zomato's app actually handles.

### New
- 📧 **In-app Gmail composer** — fill To / Cc / Subject / Body inside NexOS, tap Send to hand off to Gmail prefilled.
- 📅 **Calendar month grid** — tap any day → sheet with title/time/duration/notes → goes to your phone's Calendar.
- ⏰ **In-built clock alarm** — TimePicker + day-of-week chips, recurring alarms re-arm automatically.
- 📸 **One-shot capture** — Notes → Capture → app backgrounds → floating panda appears over any app → tap to screenshot → note created → floating button vanishes.
- 🐼 **30 panda variants** scattered behind the Hub and Welcome screens (sleepy, coffee, headphones, sunglasses, crown, balloon, chef, wizard, detective, runner, pirate, birthday, … 30 in total). Random-but-stable positions, never clustered, gentle bob animation.

### Removed
- Google Search, Photos, YouTube tiles from the Google ecosystem hub (per request — kept Gmail, Calendar, Drive, Translate).

## Install

```bash
adb install NexOS-latest-release.apk
```

## All releases

- [`v1.0.0`](https://github.com/AryanSahay1/APK/tree/v1.0.0) — MVP
- [`v1.1.0`](https://github.com/AryanSahay1/APK/tree/v1.1.0) — Super-Panda
- [`v1.1.1`](https://github.com/AryanSahay1/APK/tree/v1.1.1) — GNews seeded
- [`v1.1.2`](https://github.com/AryanSahay1/APK/tree/v1.1.2) — OpenWeather attempt
- [`v1.2.0`](https://github.com/AryanSahay1/APK/tree/v1.2.0) — Pandas Everywhere (current)

## Licence

Apache License 2.0. See [LICENSE](https://github.com/AryanSahay1/APK/blob/main/LICENSE).
