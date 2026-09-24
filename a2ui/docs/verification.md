# PocketCommunity verification — 2026-09-24

Status: first working vertical slice, not a production/learning release.

## Passed
- Debug APK, Android test APK, JVM tests, debug lint and release R8 APK built successfully.
- 2 JVM tests: unknown event fields and sponsorship preservation.
- 3 instrumentation tests on the connected OnePlus DN2101 physical phone: real A2UI render → venue action → preparation → checked value forwarded on refinement → session save; discovery failure/dismissal; no endpoint requires setup. These tests use a fake repository and the real renderer.
- 10 Node tests: output validation, graph restrictions, action restrictions, checkbox initialization and preservation by exact item label.
- Live Gemini on the physical phone via ADB reverse: generated DevEarth event cards, venue details, preparation checklist, and follow-up recognizing a checked item as 1 of 4 complete. Retested after the fix: the new surface retained the checked value and labelled completion as self-reported. This is genuine server inference; deterministic fixtures are only in androidTest sources.
- Lint has no errors. Alpha/dependency and existing manifest warnings remain; no baseline suppressions added.
- Server `.env` and local build configuration remain ignored. No credentials are embedded in the APK.

## Boundaries
- Transport is a complete JSON response, not token/SSE streaming.
- No production hosting, authentication, quotas or rate limiting. The server binds to localhost for development.
- No embedded maps, durable saves or process-death conversation restoration. Endpoint persists; conversation stays in memory.
- Context includes last 12 text turns, last 3 component trees and up to 6 surface data models. Checklist carry-over on refinement matches exact labels; changed/new items start unchecked.
- Model output can vary. Protocol validation does not guarantee factual accuracy or consistently polished layouts. Self-reported completed items do not verify registration.
- Tablet, large-font and full accessibility evaluation remain. Roadmap/codelab are not published for this preview.

## Toolchain
Java 25 Gradle daemon (pinned daemon criteria), Java 17 app compiler toolchain; Gradle 9.3.1 with distribution checksum; AGP 9.1.1; Kotlin Compose plugin 2.3.20; compile SDK 37 / target SDK 36 / min SDK 26. Official AndroidX A2UI 1.0.0-alpha01 requires Compose 1.13.0-alpha03 and Material 3 1.5.0-alpha28, so older cookbook toolchain versions could not be reused unchanged.

The source and generated artwork are public-safe. `local.properties`, build outputs and server `.env` are ignored.
