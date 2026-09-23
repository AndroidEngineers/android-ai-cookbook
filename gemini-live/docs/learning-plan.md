# PocketCook learning delivery

Status: planned. The concrete next steps and existing website lesson mapping are in [the core release plan](release-plan.md). Existing website path: [Gemini Live in Android](https://www.androidengineers.in/roadmap/gemini-live-android). New course/codelab pages and source tags are not published yet.

Each lesson must explain the concept and tradeoffs, show compiling Kotlin from its checkpoint, provide a prediction/debugging exercise, and finish with an observable verification action. Introduce recipe UI before requiring cloud access. Starter and solution checkpoints must be executable; no full-project-copy maze.

| Lesson / codelab chapter | Working result | Exercise / check |
| --- | --- | --- |
| 1. Meet PocketCook and set up | Clone cookbook, open only gemini-live, run offline recipe UI | Explain local vs cloud responsibilities |
| 2. Compose and app state | Library, details, cooking and manual progress | Complete last step without out-of-bounds errors |
| 3. Direct Live protocol | Authenticated setup and typed connection events | Reject media before setup; recover from invalid credentials |
| 4. Capture microphone audio | Permission-aware PCM capture with bounded buffers | Deny permission, then grant; inspect format without logging audio |
| 5. Play streaming responses | Audible response with queue ownership | Test cancellation while audio is queued |
| 6. Conversation and interruptions | Natural turns, transcripts and interruption | Interrupt twice; old playback must not resume |
| 7. Lifecycle and recovery | Clean end, safe background behavior and reconnect | Rotate, background, restore network and restart |
| 8. Recipe context | Assistant answers using selected recipe/current step | Change a step manually and verify new context |
| 9. Validated tools | Step navigation and bounded in-app timer | Duplicate/invalid/cancelled tool call challenge |
| 10. Camera context | Explicit CameraX frame sharing | Deny/disable camera and verify voice remains usable |
| 11. Deployment authentication | Ephemeral token service and release configuration | Expired token and unauthorized issuance checks |
| 12. Evaluation and extension | Reproducible device report and final challenge | Add a supported recipe interaction with tests and measured outcomes |

## Checkpoint policy

Record immutable commits/tags only after the corresponding build and tests pass. Proposed milestones: starter, voice-core, tools, camera and complete; these are labels for planning, not existing refs. Document JDK/SDK setup, Gradle commands, accounts, API access, costs, device requirements, exact files edited, expected UI, likely errors and remedies.

Website roadmap follows the chapter prerequisites; release order does not require finishing separate voice-ai or multimodal-ai apps. Course explains why the implementation works. Codelab makes the same source reproducible. README provides a screenshot-led entry point and links both ways.

## Publication gate

Capture working app screenshots in light/dark and a short real-device voice/interruption demo. Add full-portrait images to the codelab with descriptive captions. Update the existing roadmap, add substantive lessons and publish a dedicated codelab with exact source refs. Verify deployment and links; a pushed commit is not proof the website is live.

Run the complete clean-checkout walkthrough and identify whether it was author-run or independent. Record remaining limitations. Mark core app and advanced chapters separately; do not mark the full learning journey Released until all advertised chapters have been verified.
