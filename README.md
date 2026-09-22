# Android + AI Cookbook

Practical Android + AI engineering with Kotlin and Jetpack Compose: learn how a feature works, how it fails, and how to build it responsibly.

**Six runnable recipes, verified with deterministic fixtures.** One Compose app demonstrates streaming, Firebase integration setup, cancellation, validation, bounded retries, and ADK tool approval. Fixture mode needs no cloud account. Live Firebase inference and real-model ADK reasoning are not verified.

[Run Recipe Lab](samples/recipe-lab/README.md) · [Download the fixture prerelease](https://github.com/AndroidEngineers/android-ai-cookbook/releases/tag/v0.1.0-fixtures) · [Verification evidence](docs/verification/v0.1.0-fixtures.md)

<img src="docs/images/streaming.png" alt="Android + AI Cookbook running on an emulator" width="320" />

[Start learning at Android Engineers Academy](https://www.androidengineers.in/roadmap?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=readme) · [Getting started](docs/getting-started.md) · [Contribute](CONTRIBUTING.md)

## First six recipes

| Recipe | What you will learn | Status |
| --- | --- | --- |
| [Fake model, real Compose streaming UI](recipes/01-cloud-basics/fixture-streaming-ui/README.md) | Show loading, incremental text, completion, and a recoverable error without a provider account. | Tested fixture |
| [First cloud feature with Firebase AI Logic](recipes/01-cloud-basics/firebase-first-feature/README.md) | Configure a cloud-backed feature and explain how access is protected. | Tested fixture |
| [Cancel a response when the user changes tasks](recipes/01-cloud-basics/lifecycle-cancel-stream/README.md) | Cancel work when the request changes and reject late output from the old request. | Tested fixture |
| [Validate structured extraction](recipes/02-structured-output/validated-extraction/README.md) | Parse structured output, reject invalid fields, and separate schema validity from factual accuracy. | Tested fixture |
| [Retry, timeout, and offline behavior](recipes/01-cloud-basics/bounded-recovery/README.md) | Recover from transient failures within a bounded retry budget without duplicating actions. | Tested fixture |
| [One safe tool call with ADK on Android](recipes/04-adk-agents/adk-safe-tool/README.md) | Separate an agent tool proposal from the application decision to execute it. | Tested fixture |

## Browse the tracks

- [Cloud Basics](recipes/01-cloud-basics/README.md) — Cloud requests, streaming, lifecycle ownership, and failure recovery.
- [Structured Output](recipes/02-structured-output/README.md) — Parse and validate model output before using it in application state.
- [On Device](recipes/03-on-device/README.md) — Device capabilities, model readiness, local inference, and fallback behavior.
- [Adk Agents](recipes/04-adk-agents/README.md) — Agent tools, sessions, user approval, and cancellation on Android.
- [Mcp Appfunctions](recipes/05-mcp-appfunctions/README.md) — Tool servers, app capabilities, caller identity, and permissions.
- [Voice Multimodal](recipes/06-voice-multimodal/README.md) — Audio, camera input, interruptions, and reconnect behavior.
- [Retrieval](recipes/07-retrieval/README.md) — Retrieve evidence, inspect citations, and handle unsupported answers.
- [Production](recipes/08-production/README.md) — Evaluation, observability, model changes, cost controls, and rollout.

## What makes a recipe ready?

A ready recipe includes runnable code, prerequisites, tested versions, expected behavior, failure cases, a small exercise, and a next lesson. Fixture-mode and live-provider verification are separate claims. A successful build is not evidence of a real device or model run.

See [compatibility and verification](docs/compatibility.md), the [recipe template](docs/recipe-template.md), and the [machine-readable catalog](catalog/recipes.json).

## Repository layout

- `recipes/`: explanations and acceptance criteria, grouped by topic.
- `samples/`: the shared runnable Recipe Lab with pinned toolchain requirements.
- `fixtures/`: the inventory of deterministic inputs and expected outcomes.
- `experimental/`: future isolated examples using preview APIs.
- `resources/`: annotated primary documentation.
- `scripts/`: dependency-free catalog validation.

## Validate the catalog

Python 3.11 or newer is required. No Android SDK or model account is needed for this check.

```sh
git clone https://github.com/AndroidEngineers/android-ai-cookbook.git
cd android-ai-cookbook
python3 scripts/validate_catalog.py
```

This checks documentation/catalog consistency. To build the Android sample and run its tests, follow [Recipe Lab setup](samples/recipe-lab/README.md). CI builds debug/release APKs, runs unit tests and lint, and compiles instrumented tests. Emulator test evidence is recorded separately.

## Contribute

Read [CONTRIBUTING.md](CONTRIBUTING.md). Start with one learning outcome, discuss its scope, and include evidence rather than promises. Do not include credentials, private data, or uncredited third-party code.

Maintained by [Android Engineers](https://github.com/AndroidEngineers). Licensed under [Apache 2.0](LICENSE). This is a community project, not an official Google, Android, or JetBrains SDK repository.
