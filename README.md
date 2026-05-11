# NexOS v1.1.2 — Weather works out-of-the-box

This branch hosts pre-built APKs for the [`v1.1.2` tag](https://github.com/AryanSahay1/APK/tree/v1.1.2).

## Direct downloads

| Build | File | Size | Direct download |
|---|---|---|---|
| **Release** (recommended) | `NexOS-v1.1.2-release.apk` | 54 MB | [Download](https://github.com/AryanSahay1/APK/raw/releases/v1.1.2/NexOS-v1.1.2-release.apk) |
| Debug | `NexOS-v1.1.2-debug.apk` | 60 MB | [Download](https://github.com/AryanSahay1/APK/raw/releases/v1.1.2/NexOS-v1.1.2-debug.apk) |

Signed with APK Signature Scheme v2. Installs on any Android 8.0 (API 26) or newer device.

## What's new vs v1.1.1

- 🌤️ **Weather works out-of-the-box** — the app now ships with a pre-seeded OpenWeather API key. No copy-pasting required on first launch.
- 🔁 **Dual weather backend** — OpenWeather is the primary provider when the key is set; Open-Meteo (no key, CC-BY) is the transparent fallback. If OpenWeather fails for any reason (network, quota, revoked key) the call automatically falls back so the user always sees data.
- ⚙️ **Settings → Weather provider** new section explains the model.
- 🔒 **Redactor extended** to mask `appid` query parameter values (OpenWeather's auth) before they reach Logcat in debug builds.

## Installation

```bash
adb install NexOS-v1.1.2-release.apk
```

## Upgrade safety

No DB schema change — Room version stays at 2. Existing notes, alarms, and stored API keys all carry over. The OpenWeather seed only writes if the slot is empty, so anyone who set their own key keeps it.

## License & policies

- [LICENSE](https://github.com/AryanSahay1/APK/blob/v1.1.2/LICENSE) — Apache License 2.0
- [PRIVACY.md](https://github.com/AryanSahay1/APK/blob/v1.1.2/PRIVACY.md) — local-first; no telemetry; no backend
- [UPDATES_POLICY.md](https://github.com/AryanSahay1/APK/blob/v1.1.2/UPDATES_POLICY.md) — free forever; semver; reproducible builds
- [NOTICE](https://github.com/AryanSahay1/APK/blob/v1.1.2/NOTICE) — third-party attributions (GNews, OpenWeather, Open-Meteo)
