# PocketCards verification

First implementation, 22 September 2026. Status: **Building**, not a published course or production release.

## Environment

- JDK 17.0.19; Gradle 9.1.0 with distribution checksum; Android Gradle Plugin 9.0.1.
- Kotlin Compose/serialization plugins 2.3.20; Compose BOM 2026.03.01; Navigation 3 1.0.1.
- Firebase BOM 34.19.0; configured model name `gemini-3.8-flash`.
- Compile/target SDK 36, minimum 26. Device checks use Medium Phone API 36.1, Android 16, arm64 emulator.

## Reproduce

From `ai-fundamentals`, with JDK 17 and a local Android SDK:

```sh
./gradlew --max-workers=2 :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest :app:lintDebug
# With an emulator running:
./gradlew --max-workers=2 :app:connectedDebugAndroidTest
```

No Firebase configuration or production secrets are needed for these checks. Instrumented AI tests explicitly inject a fake generator; they do not verify cloud inference.

## Results

- Debug APK and unsigned release APK: passed.
- 13 JVM unit tests: passed. Covers structured-output validation, generation, cancellation, missing configuration, retry, save failures and edit/delete behavior.
- 5 instrumented tests: passed. Covers sample reveal/rating, manual creation when AI is unavailable, injected generation/review/save, persistence across repository recreation, and refusing to overwrite corrupt storage.
- Android lint: passed, no errors (warnings explained below).
- Visually inspected the light-theme library and study screens, plus the dark-theme library at 1.5× font scale, on the emulator. Fixed decorative-card text clipping at larger font sizes. Screenshots in `images/` show the actual running app.
- An initial device run found the Create deck button missing its accessible label. Added an explicit semantic label and reran all checks successfully.

CI builds both variants, runs unit tests and lint, and compiles instrumented tests; CI does not run an emulator.

## Remaining release work

- Live Firebase generation, App Check enforcement, unsupported inputs, provider failures, latency and task-level factuality evaluation require an actual project. No live model quality claims are made.
- Unsaved notes and drafts survive configuration changes through the ViewModel, but are not persisted across process death. Saved decks use atomic local storage. Session ratings are not a spaced-repetition algorithm.
- Release APK is unsigned and shrinking is disabled. Play distribution, release attestation and a shrunk release need separate verification.
- API 26 devices, physical devices, tablets, TalkBack, landscape, process recreation and the full large-font matrix need broader coverage before release.
- Lint has no errors; warnings identify newer dependency/target versions and a redundant v26 icon resource qualifier. This first implementation retains the Android CLI template's pinned toolchain; a tested upgrade is separate work.
- The README walkthrough is available. Website lessons, a build-from-starter codelab, and source checkpoints are still pending and must align before the learning journey is released.
