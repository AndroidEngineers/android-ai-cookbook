# Getting started

Choose a topic from the [feature index](features.md), which distinguishes available examples from planned samples.

## Run a recipe

Start with [Recipe Lab](../samples/recipe-lab/README.md). It contains six tabs and runs offline using synthetic fixtures after build dependencies have been downloaded. You need JDK 17, SDK 36, and an API 26+ emulator or device. There is no root Gradle project: open `samples/recipe-lab` in Android Studio.

1. Run Streaming to see partial output and an injected failure.
2. Try Firebase's fixture and configuration-missing path; configure your own project only if you want live inference.
3. Replace or cancel a streaming request.
4. Validate editable synthetic JSON.
5. Inject transient failures, offline behavior, and timeouts.
6. Run actual ADK tool dispatch with a deterministic model and an exact approval boundary.

The [catalog](recipes.json) links each recipe to an academy lesson. The [verification report](verification/v0.1.0-fixtures.md) distinguishes builds, unit tests, emulator tests, and unverified cloud/device paths.

## Contribute

Use Python 3.11+ to run `python3 scripts/validate_catalog.py`. Follow [CONTRIBUTING.md](../CONTRIBUTING.md), include regression evidence, and keep draft status until a recipe meets its publication criteria.
