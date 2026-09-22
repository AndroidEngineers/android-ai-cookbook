# PocketCards

**Turn your notes into knowledge.** The first Android AI Fundamentals app in the Pocket collection.

## First implementation

Enter study notes → request flashcards → review/edit questions and answers → save a deck → reveal and rate cards in a study session. The visual direction is lavender, warm ivory, editorial headings, and layered cards, with dark-theme equivalents.

Screens: Library, Create, Review/Edit, Study (including completion state). The main interaction is revealing an answer and deciding whether it feels familiar. A supplied AI-vocabulary deck is hand-authored and labeled as a sample; it is not generated from the user's notes.

## Scope

- Cloud generation through Firebase AI Logic with structured JSON, local validation, timeout, and cancellation.
- Manual card creation and editing; 1–10 cards per deck.
- Atomic local deck storage, reopen/edit/delete, and a study session summary.
- Accessible button-based study interaction, system light/dark themes, scrollable content, and constrained content width on larger windows.

No accounts, subscriptions, spaced-repetition scheduling, imports, or cloud synchronization in this first version. Unsaved notes, drafts, and in-progress network requests are not restored after process death; saved decks persist. Activity recreation retains the ViewModel and navigation/session state.

## Runtime and data

Kotlin/Compose Android application, API 26+, target/compile SDK 36 from the Android CLI's generated template. Firebase AI Logic generation uses the configured learner-owned project and the pinned model name in the app build file. No privileged provider API key is embedded. Debug App Check is for development; release uses Play Integrity. Live provider access is not verified without a configured project.

Notes are sent to the cloud only when the user requests generation. Saved decks stay in internal app storage; backup and device-transfer rules exclude app data. Deleting a deck removes it locally; uninstall removes all stored decks. Do not enter private source material in this learning sample.

## Acceptance criteria

A learner can run the app without Firebase, study the sample, and create/save/edit/delete a manual deck. With Firebase configured, a generation request produces editable validated cards or a recoverable error. Cancellation cannot publish stale output. Disk failure does not discard the editor contents. Each study card can reveal its answer and advance exactly once per action.

## Learning outcomes

Understand an inference boundary, distinguish output structure from factual accuracy, trace UI → ViewModel → repository, evaluate generated cards against source notes, and test failure paths without cloud calls.
