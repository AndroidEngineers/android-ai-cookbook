# PocketChat verification — initial Android slice

Date: 2026-09-23. Initial implementation verification.

## Environment

- JDK 17.0.19; Gradle 9.1.0 with checked-in distribution checksum.
- AGP 9.0.1; Kotlin/Compose compiler 2.3.20; Compose BOM 2026.03.01.
- Room 2.8.5; KSP 2.3.12; Lifecycle 2.10.0; OkHttp 4.12.0 (debug only).
- Compile/target SDK 36, minimum SDK 26.
- Emulator `emulator-5556`, AVD `PocketCook_API_36_1`, Android 16. Its name predates this app.
- Default model selection: `gemini-2.5-flash`, GenerateContent REST streaming with SSE. Model availability is documented by Google; no live credentialed model request was performed in this pass.

## Results

| Check | Result |
| --- | --- |
| `./gradlew :app:assembleDebug` | Passed; APK installed and app opened on emulator |
| `./gradlew :app:testDebugUnitTest` | 16 tests passed |
| `./gradlew :app:connectedDebugAndroidTest` | 5 tests passed with controlled, explicitly test-only model responses |
| `./gradlew :app:lintDebug` | 0 errors, 15 warnings for newer dependencies/tooling and target API; no suppressions added |
| `./gradlew :app:assembleRelease` | Passed, including R8 shrinking; unsigned release APK |
| APK DEX inspection | Direct Gemini hostname present in debug, absent from release |
| `git diff --check` | Passed for tracked changes |

Unit coverage: context budgeting and complete-pair selection; oversize input; duplicate Send; Stop; retry context without duplicate user turns; interrupted-process recovery; background credential clearing; failed loads preserving data; saved-copy semantics; truncated streams; SSE comments/CRLF/multiline/size caps; thought exclusion; blocked prompts; HTTP payload/header; sanitized quota errors; cancellation of underlying HTTP call.

Emulator coverage: send → stream → Stop → Retry → completed reply → Save → follow-up context → reopen saved source; missing credentials preserve draft; draft survives navigation; actual Activity recreation retains draft; Room data survives close/reopen.

A normal-touch journey initially failed because the connection snackbar could cover the composer. The composer now lives in the Scaffold bottom bar, placing transient messages above it. The final passing journey uses normal taps, not direct callback invocation.

![Actual PocketChat home screen on the emulator](screenshots/home-emulator.png)

## Remaining acceptance

- The author subsequently reported receiving a Gemini response. A recorded live acceptance walkthrough, answer-quality checks, quota behavior, and timing measurements remain outstanding. Enter a learner-owned key in Settings; never paste it into source or chat.
- Thorough TalkBack, 200% font, tablet/foldable, dark theme, keyboard and process-death UI acceptance. Activity recreation and repository recovery tests do not substitute for the full matrix.
- Full Markdown rendering, durable theme preference, Navigation 3 integration, structured takeaways and controlled tools.
- Production backend/authentication, large-history normalized persistence, complete lessons/codelab, pinned source checkpoints, clean learner checkout walkthrough.

The automated checks do not establish live-model quality, production readiness, or a completed curriculum.

## Official references consulted

- [Gemini GenerateContent and streaming](https://ai.google.dev/api/generate-content)
- [Gemini 2.5 Flash model](https://ai.google.dev/gemini-api/docs/models/gemini-2.5-flash)
- [Room releases](https://developer.android.com/jetpack/androidx/releases/room)
- Android CLI `android docs search 'Compose lifecycle StateFlow'` and `android docs fetch kb://android/develop/ui/compose/architecture`.

## Android Engineers assistant update

The default request now includes an explicit AI identity and a curated directory of roadmap, codelab, course, and 1:1 mentorship destinations. Public destination pages were reviewed on 2026-09-23. No course-text retrieval, booking, pricing lookup, or live search is implemented. The home screen exposes direct resource buttons; completed responses can show known resource links.

`assembleDebug`, `testDebugUnitTest`, and `lintDebug` passed after this change: **19 unit tests**, 0 lint errors, 15 existing upgrade warnings. Added link-matching checks and a transport assertion that the actual request includes the branded system instruction. The earlier five emulator journey tests were not rerun for this update; live response adherence still requires checking with a learner-owned key.
