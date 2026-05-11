# NexOS — Privacy Policy

_Last updated: 2025-01-01_

## Summary

NexOS is a local-first Android app. Everything you capture stays on your device unless you explicitly choose to send it to an AI provider you have configured with your own API key.

| Data type | Where it lives | Sent off-device? |
|---|---|---|
| Notes (text + summary) | Room DB on device | **No** |
| Screenshots | App cache directory | **No** (deleted on demand) |
| Voice audio | Discarded after transcription | **No** |
| Transcripts | Room DB on device | Only if AI summarisation is enabled |
| API keys | EncryptedSharedPreferences (Android Keystore, AES-256-GCM) | **No** |
| Alarms / reminders | Room DB on device | **No** |
| News articles you save | Room DB on device | Only the body is sent to your AI provider _if_ summarisation is enabled |
| Analytics / telemetry | _None_ | **No** |
| Crash reports | _None_ | **No** |
| Advertising | _None_ | **No** |

## What we never do

- We do not run a backend server.
- We do not collect analytics, telemetry, or crash reports.
- We do not embed advertising or third-party SDKs that track you.
- We do not have an account system. There is nothing to sign up for.
- We do not sell, rent, or otherwise share data — because we never receive any.

## Third-party services you may enable

These are optional. NexOS works fully without any of them.

### AI summarisation providers

When you paste an API key for any of the supported providers, NexOS sends the cleaned OCR text or voice transcript to that provider so it can return a structured summary. NexOS receives only the summary back; the provider's own privacy policy governs how they handle the prompt:

- OpenAI — https://openai.com/policies/privacy-policy
- Google Gemini — https://policies.google.com/privacy
- Anthropic — https://www.anthropic.com/legal/privacy
- Groq — https://groq.com/privacy-policy/

Auth uses headers (`Authorization`, `x-api-key`, `x-goog-api-key`) — keys never appear in URLs, query strings, or log output.

### NewsAPI (news feed)

When you supply a NewsAPI key in Settings → News and use the News tab, NexOS calls https://newsapi.org over HTTPS to fetch headlines for your selected category. NexOS does not pass any personally identifying information.

### Deep-link integrations (Uber, Rapido, Zomato, Swiggy)

The Hub tab launches these apps via their public deep-link URIs. NexOS does not communicate with their servers; it hands off control to the app you tap. The destination text you type is passed to that app as a URI parameter. If the app is not installed, NexOS opens its Play Store page.

## Permissions we request — and why

| Permission | Reason |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Draw the floating capture button over other apps |
| `FOREGROUND_SERVICE_MEDIA_PROJECTION` | Screen capture |
| `RECORD_AUDIO` | On-device voice transcription |
| `POST_NOTIFICATIONS` | Save confirmations + alarm rings |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | Fire reminder alarms at the exact time you scheduled |
| `INTERNET` + `ACCESS_NETWORK_STATE` | Used **only** if you enable an AI provider or NewsAPI |

## Your rights

Because we never hold your data, the standard rights of access / deletion / portability are effectively built into the app:

- **Access**: every note is visible to you in the Notes tab.
- **Export**: use Share / Export to copy a note as Markdown.
- **Delete**: swipe-to-delete on any note; "Clear image cache" in Settings; uninstalling the app removes everything.

## Contact

NexOS is open source under the Apache License 2.0. Issues, questions, and disclosures are handled in the GitHub repository.
