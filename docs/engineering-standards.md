# Engineering standards

These are cookbook defaults, informed by [Android architecture recommendations](https://developer.android.com/topic/architecture/recommendations). Record justified deviations in a short decision note beside the app.

## MVVM and state ownership

Compose renders state and sends user actions to a screen ViewModel. The ViewModel coordinates work through repositories and exposes read-only `StateFlow<UiState>`. Collect with lifecycle awareness. Keep SDK calls, persistence, and business rules out of composables. Keep Activity, UI Context, lifecycle owners, and Resources out of ViewModels; inject the appropriate data-layer capability instead.

Use coroutine scopes with explicit owners. Preserve cancellation, avoid GlobalScope, and prevent late responses from updating a newer request. UI state must represent empty, loading, partial, successful, failed, and unavailable results as relevant. Represent durable outcomes in state rather than relying on lossy one-off event channels. Save only the state needed to restore the experience; never persist secrets or large model buffers through SavedStateHandle.

Repositories own data access and coordination. Add domain use cases for reused or complex business operations, not as a mandatory wrapper around every method. Separate SDK DTOs from app models when the boundary needs validation or translation. Keep network and disk operations main-safe.

## SOLID in practice

- Single responsibility: split UI state coordination, storage, inference, and tool execution where they change for different reasons.
- Open/closed: add provider adapters behind a small capability contract when provider substitution is a real teaching or testing need.
- Liskov substitution: real and fake implementations respect the same cancellation, error, and result semantics; test those contracts.
- Interface segregation: expose the operations a consumer needs rather than a universal AI manager.
- Dependency inversion: policy and ViewModels depend on injected capabilities, not SDK singletons. Avoid interfaces with no useful boundary.

Start with one app module and feature packages. Add modules for independent runtime boundaries, meaningful reuse, or build isolation. Prefer composition to inheritance. Manual constructor wiring is suitable for small apps; document when Hilt becomes worthwhile.

## UI quality

Use accessible Material 3 components with readable contrast, semantics, scalable text, and adequate touch targets. Verify dark/light themes, large font, keyboard/IME behavior, edge-to-edge insets, Back, rotation, and compact/expanded windows. Camera and microphone screens need explicit permission, denial, and lifecycle behavior. Multi-screen apps should evaluate Navigation 3 against their pinned toolchain. Do not introduce preview APIs solely because a skill demonstrates them.

## AI boundaries

Distinguish cloud, system-managed on-device models, and app-managed models. Display capability, download, model loading, cancellation, and unsupported-device states. Never silently send local-only data to the cloud. Keep privileged API credentials on the server; use documented mobile SDK protections where appropriate. App attestation does not replace user authorization.

Treat retrieved documents and model output as untrusted data. Validate structured results before persistence and tool arguments before execution. Require confirmation for consequential actions, prevent duplicate effects, and bound retries, timeouts, tool loops, and resource budgets. Redact sensitive prompt content from logs. Explain model terms, download size, removal, and storage use for distributed weights.

A demo mode can help tests and setup, but the app's claimed AI feature needs separate live-model/device evidence. Evaluate quality with representative successes, unsupported inputs, and failure cases. Record model/version and distinguish performance measured on real hardware from emulator results.

## Builds and quality gates

Each project pins a compatible JDK, Gradle wrapper/checksum, AGP, Kotlin, Compose, and SDK dependency set. Use a project-local version catalog when helpful. Choose min/target SDK from current distribution and runtime requirements; do not fix one minimum across all 24 apps.

Before release: clean build, meaningful unit tests, lint, main-flow UI/device verification, and model task evaluation. Exercise cancellation, errors, lifecycle recreation, and permissions where applicable. Test release shrinking for apps using SDK reflection/native code. Measure startup, memory, latency, and battery where central to the lesson; do not promise universal benchmarks.

Add app-scoped CI when actual projects exist. Pull requests must not need production secrets. Compile-only checks must not be reported as emulator runs. Fix relevant errors and explain warnings rather than suppressing checks broadly.

## Learning delivery

Use [the learning contract](learning-contract.md). Keep source and instructional text aligned. Technical polish and clear teaching are both release criteria.
