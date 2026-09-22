# PocketCards architecture

```text
Compose screens → PocketCardsViewModel → DeckRepository → AtomicFile (internal storage)
                                      → CardGenerator → Firebase AI Logic → Gemini
```

`data/Models.kt` defines decks, flashcards, and validation. `CardGenerator.kt` isolates cloud inference and JSON schema constraints. Validation checks size, required strings, and duplicate questions; it cannot prove factual correctness.

`FileDeckRepository` uses a coroutine Mutex and IO dispatcher. Writes use AtomicFile and only update UI state after storage succeeds. Existing corrupt data causes a visible error rather than being silently replaced. This small learning app uses one JSON file instead of a database; a larger library would need paging/query support and schema migration.

`PocketCardsViewModel` owns the create/edit workflow and library snapshot. It publishes immutable StateFlow values. Generation uses a cancellable coroutine plus a request identity guard. Persistence and generation dependencies use constructor injection with a small manual factory. No application or Activity context is held by the ViewModel.

Navigation 3 saves routes; one activity-scoped ViewModel owns the shared draft across Create and Editor. This scope is intentional for the short multi-screen creation flow. Study-session counters use saveable Compose state. On process recreation, saved decks reload from storage; unsaved drafts are intentionally not persisted and the editor explains the empty-draft case.

Firebase initialization is optional. The Google Services plugin applies only when `app/google-services.json` exists. Debug and release source sets select distinct App Check providers. The release is unsigned and not Play-published. Cloud permission, App Check enforcement, billing, and quotas must be configured in Firebase before external rollout.

The independent project and application files were generated from the official Android CLI empty-activity template; PocketCards behavior and visual design were implemented for this cookbook. Apache 2.0 repository licensing applies.
