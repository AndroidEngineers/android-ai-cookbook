# PocketStories learning journey

Status: Planned curriculum; app first slice is Building. Academy URLs and immutable source commits will be recorded after implementation; no published course is claimed.

Each row becomes a conceptual roadmap lesson, a companion exercise, and a codelab milestone. Checkpoint labels below are planned milestones, not existing Git tags.

| Lesson | Android + Gemma understanding | Codelab action / planned checkpoint | Observable evidence |
| --- | --- | --- | --- |
| 1. Where the intelligence runs | Weights, tokenizer, runtime, app policy; Gemma vs cloud Gemini vs system-managed Nano; inference vs training | Trace the local data path / P1 | Explain which operations need a network and which do not |
| 2. Model and device fit | Instruction tuning, quantization, compatible formats, ABI/backends; storage vs peak memory | Inspect the pinned model manifest and device / P1 | Record a real tested compatibility tuple |
| 3. Acquire and own a model | Terms/access, HTTP Range resume, cancellation, document picker, app-private files and integrity | Implement verified download and optional import / P2 | Interrupted or corrupt imports never become ready |
| 4. Runtime lifecycle | Engine vs conversation, background initialization, exclusive ownership and cleanup | Initialize and unload Gemma / P3 | UI stays responsive; repeated loads do not accumulate engines |
| 5. Stream the first scene | Kotlin Flow, SDK chunk semantics, main-safe UI updates and completion | Connect the real generator to Compose / P3 | Actual Gemma scene streams on a phone offline |
| 6. Stop and recover | Coroutine cancellation vs native cancellation, stale attempts, background policy | Add cancellation and attempt isolation / P4 | Stop/retry/rotation cannot mix two responses |
| 7. Control creative output | System instructions, structured input, temperature/top-p and output limits | Compare settings on fixed story prompts / P4 | Explain observed quality differences without claiming determinism |
| 8. Maintain story continuity | Context window, prefill/decode, KV cache, output reserve and pruning | Implement context selection with failing then passing tests / P5 | Recent accepted scenes retained; rejected drafts excluded |
| 9. Persist the story, not the engine | Room, draft acceptance, interrupted states, restart reconstruction | Save and reopen stories / P5 | Process restart keeps accepted chapters without resuming stale native work |
| 10. Measure and diagnose | Cold/warm startup, first text, memory, backend tradeoffs and thermal behavior | Run a repeatable physical-device protocol / P6 | Publish device/model/settings alongside measurements |
| 11. Evaluate and ship honestly | Continuity rubric, failure UX, privacy/backup, release native libraries | Complete quality matrix and release checks / P6 | Separate deterministic tests, real-model evidence and unverified cases |
| 12. Independent capstone | Apply context and persistence policies without copying a finished solution | Add editable character facts / P7 | Facts persist, affect input and remain isolated per story; regression tests pass |

## Camera-adventure additions

The user selected camera-led storytelling. Add lessons for CameraX permission/lifecycle ownership, photo picker URI handling, image orientation/resizing, text versus image-capable models, foreground gesture events, and explicit user acceptance of fictional discoveries. The first exercise connects a single snapshot to a generated scene; automatic frame sampling, inventory, PDF and RAG remain advanced milestones.

## Teaching format

Every lesson contains: a mental model, one focused Kotlin excerpt from the verified source, why the Android decision matters, an exercise, expected behavior, a failure case, and understanding questions. Teach tokens, quantization and caching enough to explain app behavior; do not turn this into an unrelated model-training course.

The codelab includes clone/open/build steps, exact tested SDK/model requirements, in-app model acquisition and optional import, named files to edit, explanations beside snippets, a real red/green context exercise, screenshots of real states and troubleshooting. A runnable completed baseline is explicitly labeled run–understand–extend; do not pretend it is an empty starter. Provide a compiling starter only if separately maintained and verified.

Diagrams: model acquisition lifecycle; Compose-to-Gemma inference flow; cancellation/resource ownership; persisted story versus active context. Publish rendered diagrams plus editable Mermaid source.

Assessment: learners explain where inference happens, why file size is not RAM usage, what cancellation actually stops, why chapters can outlive context, how to recover after process death, and what their measured performance does and does not establish.

Completion certificates represent self-reported completion. The learning journey is released only after an aligned app/roadmap/codelab, working links and a documented clean-checkout walkthrough. Record author-run versus independent learner verification.
