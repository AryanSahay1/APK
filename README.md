# NexOS Release Assets

This branch (`releases/v1.0.0`) hosts the pre-built APKs for the [`v1.0.0` tag](https://github.com/AryanSahay1/APK/releases/tag/v1.0.0).

## Direct downloads

| Build | File | Size | Direct download URL |
|---|---|---|---|
| **Release** (recommended) | `NexOS-v1.0.0-release.apk` | 54 MB | [Download](https://github.com/AryanSahay1/APK/raw/releases/v1.0.0/NexOS-v1.0.0-release.apk) |
| Debug | `NexOS-v1.0.0-debug.apk` | 60 MB | [Download](https://github.com/AryanSahay1/APK/raw/releases/v1.0.0/NexOS-v1.0.0-debug.apk) |

Both APKs are signed with APK Signature Scheme v2 and install on any Android 8.0 (API 26) or newer device.

## Install via adb

```bash
adb install NexOS-v1.0.0-release.apk
```

## Install on-device

1. Copy the `.apk` to your phone.
2. Open it from any file manager.
3. Allow "Install unknown apps" for the file manager when prompted.
4. Tap **Install**.

## Verify integrity

```bash
$ apksigner verify --verbose NexOS-v1.0.0-release.apk
Verifies
Verified using v2 scheme (APK Signature Scheme v2): true   ✓
Number of signers: 1
```

## Build from source

Don't trust pre-built binaries? Reproduce them yourself:

```bash
git clone https://github.com/AryanSahay1/APK
cd APK
git checkout v1.0.0
./gradlew :app:assembleRelease
# → app/build/outputs/apk/release/app-release.apk
```

## License & policies

- [LICENSE](https://github.com/AryanSahay1/APK/blob/v1.0.0/LICENSE) — Apache License 2.0
- [PRIVACY.md](https://github.com/AryanSahay1/APK/blob/v1.0.0/PRIVACY.md) — Local-first; no telemetry; no backend
- [UPDATES_POLICY.md](https://github.com/AryanSahay1/APK/blob/v1.0.0/UPDATES_POLICY.md) — Free forever; semver; reproducible builds
- [NOTICE](https://github.com/AryanSahay1/APK/blob/v1.0.0/NOTICE) — Third-party attributions
