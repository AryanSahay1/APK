# NexOS v1.1.0 — Super-Panda Release

This branch hosts pre-built APKs for the [`v1.1.0` tag](https://github.com/AryanSahay1/APK/tree/v1.1.0). See [`docs/PRD-v1.1.md`](https://github.com/AryanSahay1/APK/blob/v1.1.0/docs/PRD-v1.1.md) for the release plan.

## Direct downloads

| Build | File | Size | Direct download |
|---|---|---|---|
| **Release** (recommended) | `NexOS-v1.1.0-release.apk` | 54 MB | [Download](https://github.com/AryanSahay1/APK/raw/releases/v1.1.0/NexOS-v1.1.0-release.apk) |
| Debug | `NexOS-v1.1.0-debug.apk` | 60 MB | [Download](https://github.com/AryanSahay1/APK/raw/releases/v1.1.0/NexOS-v1.1.0-debug.apk) |

Signed with APK Signature Scheme v2. Installs on any Android 8.0 (API 26) or newer device.

## Install via adb

```bash
adb install NexOS-v1.1.0-release.apk
```

## What's new vs v1.0.0

- 🌤️ **Weather** — free Open-Meteo, no API key, 7-day forecast, save-as-note
- 🗺️ **Maps quick categories** — Restaurants, Cafés, ATMs, Hospitals, Pharmacies, Gas stations, Hotels, Parks
- 🅖 **Google ecosystem** screen — Gmail, Calendar, Drive, Search, Translate, Photos, YouTube
- 🚗 **Uber detail screen** with Home / Work / Last destination quick chips
- 🍽️ **Swiggy detail screen** with cuisine chips and recent searches
- 🌗 **Day / Night / System theme** toggle (persistent)
- 🐼 **Animated panda** mascot with Breathing / Wiggle / Wave / Bouncing / Loading variants
- 🐼 **Per-section panda glyphs** for every Settings section (sun-and-moon, keyboard, broom, bubble, …)

Reduced-motion users get the same UI without animations (Android's "Remove animations" setting is respected).

## Upgrading from v1.0.0

No action needed — Room migration `v1 → v2` preserves every existing note. Just install over the previous APK; your data stays.

## License & policies

- [LICENSE](https://github.com/AryanSahay1/APK/blob/v1.1.0/LICENSE) — Apache License 2.0
- [PRIVACY.md](https://github.com/AryanSahay1/APK/blob/v1.1.0/PRIVACY.md) — local-first; no telemetry; no backend
- [UPDATES_POLICY.md](https://github.com/AryanSahay1/APK/blob/v1.1.0/UPDATES_POLICY.md) — free forever; semver; reproducible builds
- [NOTICE](https://github.com/AryanSahay1/APK/blob/v1.1.0/NOTICE) — third-party attributions (now includes Open-Meteo CC-BY 4.0)
- [docs/PRD-v1.1.md](https://github.com/AryanSahay1/APK/blob/v1.1.0/docs/PRD-v1.1.md) — the v1.1 product requirements doc
