# Android + AI Cookbook engineering instructions

## Product and scope
- Build one dedicated app per topic. Each top-level topic folder owns an independent project; do not restore a combined demo app or a root Gradle build.
- Follow [the implementation order](docs/app-roadmap.md). The AI Fundamentals app is PocketCards. Other app brand names are undecided; keep their topic slugs until selected by the user.
- Complete one app, its roadmap lessons, and its codelab before declaring its learning journey released. Keep planned topics labeled Planned.
- Kotlin JVM agent topics still get an Android client; place server code in the same topic's `server/` directory, with independent setup and tests.

## Before implementation
1. Read [engineering standards](docs/engineering-standards.md) and the topic README.
2. Create `docs/app-brief.md` within the topic using [the app brief template](docs/app-brief-template.md). Identify the core interaction, learning objectives, dependencies, device access, and verification needed.
3. Read only relevant skills from `.agents/skills/`; consult [the skill map](docs/skills.md). Official skills guide specific tasks; cookbook skills define architecture, AI boundaries, and learning delivery.
4. Verify current SDK documentation and published artifact compatibility. Pin tested versions per project; avoid `latest.release`, `+`, or copying unverified version combinations.

## Implementation defaults
- Kotlin, Jetpack Compose, Material 3, screen-level ViewModels, immutable UI state, unidirectional data flow, and lifecycle-aware collection.
- Repository boundary for data and model services. Constructor injection; manual wiring for small projects, Hilt when its scope benefits justify it.
- Keep UI, data, and optional domain responsibilities clear. Use SOLID to improve contracts and testability, not to create unused interfaces, base classes, or pass-through use cases.
- Keep dependencies and Gradle wrappers local to each app. Source lives in `<topic>/app/`, optional codelab starter in `<topic>/starter/`, and topic documentation in `<topic>/docs/`.
- Never introduce shared cross-topic build dependencies solely to remove a little duplication. Each app should be understandable and runnable independently.
- Prefer a small working vertical slice. Do not label a fake response as a working model integration.

## Verification and publication
- Add behavior-focused repository/ViewModel tests and UI coverage for the main interaction and a meaningful failure. Run build, unit tests, lint, and device checks appropriate to the change.
- Record actual commands, versions, devices, results, and unverified paths in the topic's verification report. Model outputs need task-level evaluation in addition to ordinary tests.
- Keep provider secrets outside the APK and repository. Validate model outputs and tool arguments; authorize actions in application code.
- Keep README, source checkpoints, academy roadmap, and codelab aligned to the same release. Do not invent website lesson links.
- A clean checkout must reproduce the documented setup. Version checkpoints and preserve published lesson targets.
- Report changed files, checks, and remaining limitations. Never imply Google endorsement or GDE certification.
