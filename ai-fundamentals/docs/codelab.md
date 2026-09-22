# PocketCards: run and explore

This walkthrough covers the current implementation. It is not yet the full build-from-starter website codelab.

## 1. Run the app

Follow [the README](../README.md). Open the `ai-fundamentals` project, build debug, and run it on an API 26+ device. Expect the PocketCards library, its layered-card illustration, and an empty saved-deck collection.

## 2. Study the supplied deck

Choose **Try a sample deck**. Think about the question, reveal the answer, then choose **Again** or **Got it**. Complete five cards and check the completion screen. Open Due: Got it cards return tomorrow; Again cards remain due today. Star a card and find it in Favorites. Daily progress counts distinct cards reviewed today in this deck. Read `StudyScreen.kt` and `PocketCardsViewModel.updateReview`.

## 3. Create your own cards

Choose **Create deck → Write my own cards**. Enter a title, question, and answer. Add a second card and save. Reopen the deck from the library. Try to save an empty answer and explain how validation protects storage. Read `CardValidation` and `PocketCardsViewModel.save`.

## 4. Trace persistence

Force-stop and reopen the app after saving. The deck must still exist. Edit it and confirm that saving replaces the existing deck rather than creating a duplicate. Inspect `FileDeckRepository` and its atomic write boundary. Delete only a deck you created and confirm its removal.

## 5. Configure cloud generation

Use the Firebase setup in the README. Read the notice before sending notes. Generate cards from non-sensitive study material and review every answer before saving. API setup, model access, and live behavior require separate verification in your own project; a passing mocked test is not that evidence.

## 6. Exercise failure and cancellation

Try short input, disconnect before generation, and cancel a slow request. Notes should remain available, errors should be recoverable, and a cancelled response must not replace a newer task. Review the ViewModel tests and the 45-second inference timeout.

## 7. Evaluate quality

Prepare ten short source passages with expected facts. For each generated deck, record whether answers are supported, questions are distinct, and useful facts are covered. Include ambiguous text and text containing instructions. A valid schema is not proof of a correct flashcard. Record the actual model and device/project setup; do not publish invented scores.

## 8. Independent extension

Add a subject label to decks and a library filter. Acceptance: labels survive relaunch, editing preserves labels, and filtering never deletes hidden decks. Update the data model, storage compatibility handling, UI state, and behavior tests.
