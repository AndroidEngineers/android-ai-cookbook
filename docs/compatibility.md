# Compatibility and verification

The first six recipes share [Recipe Lab](../samples/recipe-lab/README.md), built with SDK 36 and tested on an Android 16/API 36 arm64 emulator. Minimum API is 26 because the published ADK 0.1.0 manifest requires it. Other devices and live cloud paths remain unverified. See [release evidence](verification/v0.1.0-fixtures.md).

| Status | Meaning |
| --- | --- |
| draft | Planned or incomplete; excluded from working recipe counts. |
| tested-fixture | Deterministic fixture path was executed; no live-provider claim. |
| tested-live | Documented provider/model path was executed in the stated environment. |
| experimental | Preview or exploratory work; not a verified release. |
| needs-update | Previous evidence may no longer apply; requires maintenance. |
| retired | Retained for history; no longer maintained. |

A tested status requires source, a release tag, owner, tested versions, verification date, and an evidence file. Live evidence must identify the provider and model, and device-dependent evidence must identify the device/API configuration. Record costs and account setup where relevant.

The catalog validator checks required fields and local paths. Maintainers still verify build/test output, release tags, live links, and compatibility claims. Android CI builds the sample, runs unit tests/lint, and compiles instrumentation. Local emulator results are recorded separately. Recheck maintained examples on dependency/model changes and at least monthly while active.
