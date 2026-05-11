# NexOS — Updates Policy

NexOS is **forever free**. This document describes how updates work and what you can expect.

## Distribution

The project ships as:

1. **APK** built directly from this repository — `./gradlew :app:assembleDebug` produces `app/build/outputs/apk/debug/app-debug.apk`. Signed-release builds use a signing key you supply locally; no signing key is checked into the repository.
2. **Source** — the entire codebase lives in this repository under the Apache 2.0 license.

There is no paywall, no premium tier, no subscription, and no in-app purchase. There never will be.

## What is free, forever

- The app itself
- Every feature in the app
- All future updates
- Source code, build instructions, and design notes

## What you bring yourself

Some optional features call third-party services. If you want to use them, you supply your own API key — NexOS never proxies your traffic through us and never charges you. The relevant services all offer free tiers sufficient for personal use:

| Service | Free tier | Used for |
|---|---|---|
| Groq | 14,400 requests / day | AI summarisation (recommended starter) |
| Google Gemini | 1,500 requests / day on Flash | AI summarisation |
| OpenAI | Pay-as-you-go credits | AI summarisation |
| Anthropic | Pay-as-you-go credits | AI summarisation |
| NewsAPI | 100 requests / day (developer) | News headlines |
| Android system | Free | OCR (ML Kit), TTS, Speech, AlarmManager, MediaProjection |

NexOS works fully **without any of these keys**. Local mode saves raw OCR text and raw transcripts; alarms work entirely offline.

## How updates are delivered

Because NexOS is not yet on the Play Store, you update by installing a new APK over the existing one. Your Room database (notes + alarms) survives the upgrade — migrations are written defensively so no user data is lost.

The release flow:

1. A new tag is cut on the repository (`vX.Y.Z`).
2. CI builds the signed release APK.
3. The APK is attached to the GitHub release.
4. You install it; Android replaces the existing package while keeping your `/data/data/com.nexos.ai/databases/nexos.db`.

## Semantic versioning

- `MAJOR` — schema-breaking or permission-changing release. We always provide a Room migration path; never destructive.
- `MINOR` — new features (e.g. clipboard listener, additional providers, semantic search).
- `PATCH` — bug fixes only.

## No remote feature flags

NexOS does not call home for feature flags or A/B tests. The behaviour you install is the behaviour you get; nothing changes underneath you.

## Reproducible builds

Every dependency is pinned by exact version in `app/build.gradle.kts`. To verify a release APK, clone the repo at the corresponding tag, run `./gradlew :app:assembleRelease`, and compare the byte-for-byte output (R8 may introduce minor variance — diff the manifest and the merged-class outputs).

## Deprecation policy

If a free third-party service we depend on drops its free tier, we will either:

1. Add an alternative free provider, or
2. Implement an on-device alternative.

We will never charge users to keep using a feature that used to be free.
