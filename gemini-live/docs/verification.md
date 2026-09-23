# PocketCook verification — first implementation slice

Date: 2026-09-23. Status: **Building**, not App verified or Released.

## Environment

- macOS ARM64; OpenJDK 17.0.19.
- Gradle 9.1.0 with distribution checksum; AGP 9.0.1; Kotlin/Compose compiler 2.3.20; Compose BOM 2026.03.01.
- compile/target SDK 36, minimum SDK 26; AndroidX lifecycle 2.10.0, activity 1.13.0; OkHttp 4.12.0; JSON serialization 1.9.0.
- Dedicated PocketCook_API_36_1 emulator, Android 16 / API 36.1, arm64-v8a. Initial checks used the emulator; the playback regression test subsequently passed on a physical DN2101.
- Default model field: gemini-3.8-live, from the current official direct-WebSocket guide. The user subsequently reported seeing Gemini replies in the transcript. The author later confirmed voice works; the complete acceptance journey remains unverified.

## Commands and results

Run from gemini-live/:

```sh
./gradlew :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest :app:lintDebug :app:connectedDebugAndroidTest
```

- Debug APK: passed.
- Release APK with R8: passed. Release is unsigned and offline-only pending the token-service chapter.
- Unit tests: 15 passed (10 protocol/progress/configuration tests; 5 session lifecycle tests).
- Instrumented Compose tests: 3 passed on the dedicated emulator: offline last-step completion, explicit credential setup, and current-step retention across activity recreation.
- Lint: no errors. Existing pinned-version update advisories and target-SDK upgrade advice remain; this slice deliberately uses the compatible cookbook toolchain. This is not a claim of Android 17 verification.
- Final refinements include exact sample-rate validation, no-density image resources to avoid density upscaling, and explicit legacy backup configuration. These were followed by builds, unit tests and lint. The three emulator journeys were run again against the final resources.
- CI workflow added for this topic; remote CI has not run because these changes have not been pushed.

The first instrumented attempt failed in Espresso initialization (InputManager API mismatch). Explicit AndroidX Espresso 3.7.0 and test-core 1.7.0 dependencies resolved it. Initial emulator storage was full; a new dedicated emulator was created without clearing existing device data.

## Visual checks

Inspected actual library, recipe details, cooking light/dark and expanded cooking screenshots. The expanded viewport was 1920×1200 pixels at 240 dpi; normal phone viewport 1080×2400 at 420 dpi. Restored emulator overrides afterward.

At 200% font scale, verified that the cooking area scrolls and voice/transcript controls remain reachable. This is a focused layout check, not a complete accessibility audit. Full TalkBack, keyboard and device-matrix checks remain pending.

[Library](screenshots/library.png) · [Details](screenshots/details.png) · [Cooking](screenshots/cooking.png) · [Dark](screenshots/cooking-dark.png) · [Expanded](screenshots/cooking-expanded.png)

## Credential and publication checks

Local SDK path is ignored. No google-services.json or Firebase dependency is present. API keys are entered at runtime in a screenshot-protected debug dialog, stored only in memory, and cleared when the app backgrounds. No key is included in BuildConfig or source. Source/config scans found no credential pattern or developer-local absolute path in publishable app files.

## Not yet verified / not yet implemented

- Independently recorded live acceptance evidence and interruption behavior; the author has confirmed a working voice conversation.
- Physical-device mic/speaker quality, echo cancellation, wired/Bluetooth routing, focus loss, latency, battery or network-drop recovery with a real session.
- No session resumption: retry starts a fresh conversation with recipe context.
- Camera sharing, function calling, timers and a protected ephemeral-token service are not implemented.
- Ingredient checks and transcripts are memory-only. Recipe step progress is persistent.
- No independent clean-machine walkthrough. The project was built from its own directory on a machine with an existing Android SDK/Gradle cache.
- Recipes and illustrative generated photos need a kitchen/content review before a public learning release.
- PocketCook website lessons, source checkpoints and codelab are planned; none are advertised as published.

## Next real-device acceptance run

Enter a project-owned key in the app (never in chat or source), select a supported Live model, and test: connect → speak → hear answer → interrupt → mute/unmute → change step → ask follow-up → end. Then deny permission, background, disconnect network and retry. Record actual model/device versions and measured outcomes; do not substitute fake responses for live evidence.

## Streaming playback correction

The user reported Gemini transcript replies with no audible response. The PCM writer waited for each chunk to drain before consuming the next, which could stall below AudioTrack's startup threshold. It now feeds chunks continuously, tracks playback completion separately, and lowers the startup threshold on API 31+. Speaker selection also handles a missing communication device.

Validation after the fix: debug build, 15 unit tests, lint, release build, and all four emulator instrumented tests passed. The new audio regression test feeds 20 small silent PCM chunks and checks that the playback head consumes all 4,800 frames. This test also passed on the physical DN2101, and the updated debug APK was installed there. The test uses existing microphone permission when available and neither transmits nor stores recorded audio.

The regression test verifies frame consumption, not audible Gemini speech. A real spoken reply, interruption, and audio-route quality still require the acceptance run above.

## Author confirmation and publication capture

The author subsequently confirmed voice is working on the physical phone. This is user-reported live success, separate from the automated playback evidence. An earlier “Audio capture exceeded its buffer” report remains an intermittent reliability issue to investigate; a successful retry does not establish its resolution.

Three full-height physical-device dark screenshots were captured and visually inspected: library-device-dark.png, details-device-dark.png and cooking-device-dark.png. Voice is idle in these captures; no key or conversation content is shown.

Release remains Building pending sustained conversation, repeated interruption/start/stop, network/background/permission checks and a clean-checkout walkthrough. See release-plan.md for the ordered completion work.

## Final core commit checks

The microphone fragmentation issue now has a targeted fix: fixed-duration PCM batching, clearing of pending input at mute/end, and an epoch guard for an already-delivered chunk crossing mute/unmute. Tests reproduce bounded overload and check recovery without replaying old queued input. A deterministic ten-minute PCM simulation verifies sample order and preservation across 60,000 partial reads; it does not represent a ten-minute cloud conversation.

Final command: `./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug :app:connectedDebugAndroidTest` with the dedicated emulator selected. Passed: 19 unit tests, all five emulator tests, debug and release builds, and lint. Device tests include two 15-second capture/playback sessions with mute/resume and a fresh audio engine between sessions.

A separate temporary export containing only publishable source (no local.properties, Gradle project cache or build outputs) passed `./gradlew :app:assembleDebug :app:testDebugUnitTest`, using JDK 17 and ANDROID_HOME. This is an author-run clean-source check using an existing SDK and dependency cache, not an independent fresh-machine setup.

Remaining evidence: extended cloud voice/interruption tests on the final build, real permission-denial and network/background transitions, Bluetooth/wired routes and remote CI. Existing unit checks cover background credential clearing, simulated network failure/restart and interruption dispatch. These must not be described as a complete real-device acceptance run. The local core commit is suitable for review; the full learning journey remains Building.

The final AudioPlaybackTest also passed on the physical DN2101: both tests passed in 47.673 seconds. The updated debug APK is installed. Captured microphone bytes in this test were counted and validated in memory only, never stored or sent to Gemini.
