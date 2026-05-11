# NexOS — APK downloads

This branch (`apk`, no slash in name → guaranteed-stable URLs) hosts the **latest** signed APKs for [NexOS](https://github.com/AryanSahay1/APK).

## ⬇ Latest stable downloads

| Build | File | Size | Direct download |
|---|---|---|---|
| **Release** (recommended) | `NexOS-latest-release.apk` | 54 MB | **[Download](https://github.com/AryanSahay1/APK/raw/apk/NexOS-latest-release.apk)** |
| Debug | `NexOS-latest-debug.apk` | 60 MB | **[Download](https://github.com/AryanSahay1/APK/raw/apk/NexOS-latest-debug.apk)** |

## Pinned to v1.1.2 (won't change)

| Build | Size | Direct download |
|---|---|---|
| Release v1.1.2 | 54 MB | https://github.com/AryanSahay1/APK/raw/apk/NexOS-v1.1.2-release.apk |
| Debug v1.1.2 | 60 MB | https://github.com/AryanSahay1/APK/raw/apk/NexOS-v1.1.2-debug.apk |

## Why a flat `apk` branch?

The previous `releases/v1.1.2`-style URL has a slash in the branch name. Some browsers (and mobile chromium caches) get confused parsing those URLs because they can't tell the branch boundary from a path separator. The flat `apk` branch eliminates that ambiguity — every character after `/raw/apk/` is the filename.

```
https://github.com/AryanSahay1/APK/raw/apk/NexOS-latest-release.apk
                                    ^^^                 ^^^^^^^^^^^^^^^^^^^^^^^
                                  branch name           filename
```

## Install

```bash
adb install NexOS-latest-release.apk
```

Or copy the `.apk` to your phone, tap it, allow "Install unknown apps" when prompted, tap **Install**.

## Verify integrity

```bash
apksigner verify --verbose NexOS-latest-release.apk
# APK Signature Scheme v2: true
```

## Build from source

```bash
git clone https://github.com/AryanSahay1/APK
cd APK
git checkout v1.1.2
./gradlew :app:assembleRelease
```

## All NexOS releases

- [`v1.0.0`](https://github.com/AryanSahay1/APK/tree/v1.0.0) — MVP
- [`v1.1.0`](https://github.com/AryanSahay1/APK/tree/v1.1.0) — Super-Panda (Weather, Maps, Google ecosystem, Settings re-design)
- [`v1.1.1`](https://github.com/AryanSahay1/APK/tree/v1.1.1) — GNews API key seeded
- [`v1.1.2`](https://github.com/AryanSahay1/APK/tree/v1.1.2) — OpenWeather API key seeded (current)

## Licence

Apache License 2.0. See [LICENSE](https://github.com/AryanSahay1/APK/blob/main/LICENSE) on the main branch.
