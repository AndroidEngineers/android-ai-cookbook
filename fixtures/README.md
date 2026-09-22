# Deterministic fixture inventory

Fixtures live beside the code so tests and the interactive app exercise the same implementation:

- `samples/recipe-lab/app/src/main/java/in/androidengineers/cookbook/Streaming.kt`: delayed chunks and injected stream failure.
- `MainActivity.kt` in the same directory: transient/offline/timeout recovery scenarios and editable synthetic JSON.
- `SafeAgent.kt`: scripted ADK model proposal, exact approval, mismatch, and repeating-model mode.
- `app/src/test/java/in/androidengineers/cookbook/RecipeTests.kt`: malformed output and callback/retry/approval edge cases.

These are synthetic examples. Fixture wording is not a model-quality measurement. No provider credentials or real user records are included.
