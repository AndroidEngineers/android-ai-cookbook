# Compatibility and verification

No Android/toolchain/device combination has been verified for a cookbook sample yet. Each future sample must state exact supported versions and configuration.

| Status | Meaning |
| --- | --- |
| draft | Planned or incomplete; excluded from working recipe counts. |
| tested-fixture | Deterministic fixture path was executed; no live-provider claim. |
| tested-live | Documented provider/model path was executed in the stated environment. |
| experimental | Preview or exploratory work; not a verified release. |
| needs-update | Previous evidence may no longer apply; requires maintenance. |
| retired | Retained for history; no longer maintained. |

A tested status requires source, a release tag, owner, tested versions, verification date, and an evidence file. Live evidence must identify the provider and model, and device-dependent evidence must identify the device/API configuration. Record costs and account setup where relevant.

The catalog validator checks required fields and local paths. Maintainers still verify build/test output, release tags, live links, and compatibility claims. Add sample-specific CI as runnable code lands. Recheck maintained examples on dependency/model changes and at least monthly while active.
