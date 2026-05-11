# NexOS v1.1.1 — News works out-of-the-box

This branch hosts pre-built APKs for the [`v1.1.1` tag](https://github.com/AryanSahay1/APK/tree/v1.1.1).

## Direct downloads

| Build | File | Size | Direct download |
|---|---|---|---|
| **Release** (recommended) | `NexOS-v1.1.1-release.apk` | 54 MB | [Download](https://github.com/AryanSahay1/APK/raw/releases/v1.1.1/NexOS-v1.1.1-release.apk) |
| Debug | `NexOS-v1.1.1-debug.apk` | 60 MB | [Download](https://github.com/AryanSahay1/APK/raw/releases/v1.1.1/NexOS-v1.1.1-debug.apk) |

Signed with APK Signature Scheme v2. Installs on any Android 8.0 (API 26) or newer device.

## What's new vs v1.1.0

- 📰 **News works out-of-the-box** — the app now ships with a pre-seeded GNews API key. No copy-pasting required on first launch.
- 🔁 **Backend switched** from NewsAPI.org to GNews (gnews.io). Same UX, free developer tier (100 req/day), better image URLs in returned articles.
- 🔒 **Custom log redactor** masks `apikey` / `key` / `token` query parameters before they can reach Logcat in debug builds. SKILL §15 security still holds even though GNews requires query-string auth.

## Installation

```bash
adb install NexOS-v1.1.1-release.apk
```

Or download the `.apk` to your device and tap to install (grant "Install unknown apps" for the file manager when prompted).

## Upgrade safety

No DB schema change — Room version stays at 2. Existing notes, alarms, and stored API keys all carry over from v1.1.0. The GNews seed only writes if your news-key slot is currently empty, so anyone who set their own key keeps it.

## License & policies

- [LICENSE](https://github.com/AryanSahay1/APK/blob/v1.1.1/LICENSE) — Apache License 2.0
- [PRIVACY.md](https://github.com/AryanSahay1/APK/blob/v1.1.1/PRIVACY.md) — local-first; no telemetry; no backend
- [UPDATES_POLICY.md](https://github.com/AryanSahay1/APK/blob/v1.1.1/UPDATES_POLICY.md) — free forever; semver; reproducible builds
- [NOTICE](https://github.com/AryanSahay1/APK/blob/v1.1.1/NOTICE) — third-party attributions (GNews + Open-Meteo)
