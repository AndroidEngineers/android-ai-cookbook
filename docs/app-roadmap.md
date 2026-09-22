# App implementation order

One dedicated app for each of the 24 topics. **PocketCards is Building**; the other 23 apps are Planned. Names below describe concepts; no branding family has been approved. This is build order, not a promise that all learners must complete every earlier topic.

Work on one app's complete learning journey at a time. At 10–15 hours/week, estimate each app after its first vertical slice and device/API feasibility check; do not assign speculative completion dates to all 24.

| Order | Topic | App concept | Prerequisites / reusable learning | Core milestone |
| --- | --- | --- | --- | --- |
| 1 | [ai-fundamentals](../ai-fundamentals/README.md) | PocketCards — flashcard generator | None; Kotlin/Compose basics | One validated AI request, editable cards, save/delete, failure recovery |
| 2 | [gemini-chat](../gemini-chat/README.md) | Streaming messenger | ai-fundamentals | Conversation ownership, streaming, cancellation, history |
| 3 | [firebase-ai-logic](../firebase-ai-logic/README.md) | Ingredient meal planner | gemini-chat | Protected mobile inference, structured recipes, saved plans |
| 4 | [multimodal-ai](../multimodal-ai/README.md) | Camera curiosity app | firebase-ai-logic | Image selection, media preparation, grounded follow-up UI |
| 5 | [ml-kit-vision](../ml-kit-vision/README.md) | Receipt and barcode organizer | ai-fundamentals | Camera input, OCR/barcodes, correction, local storage |
| 6 | [ml-kit-genai](../ml-kit-genai/README.md) | Writing workspace | ai-fundamentals | Capability/download states, rewrite/summary, before/after UI |
| 7 | [litert](../litert/README.md) | Model benchmark playground | ai-fundamentals | Compatible runtime/model loading and measured inference |
| 8 | [gemma](../gemma/README.md) | Offline story studio | litert | Downloadable model, story sessions, offline generation |
| 9 | [on-device-ai](../on-device-ai/README.md) | Private journal assistant | gemma; ml-kit-genai | Local routing, model delivery/removal, privacy and recovery |
| 10 | [rag-and-embeddings](../rag-and-embeddings/README.md) | Cited study library | gemma | Ingest, embed, retrieve, answer with citations and abstention |
| 11 | [voice-ai](../voice-ai/README.md) | Voice note organizer | gemini-chat | Recording, transcription, action extraction, playback |
| 12 | [gemini-live](../gemini-live/README.md) | Hands-free cooking companion | voice-ai; multimodal-ai | Realtime session, interruption, reconnect, audio lifecycle |
| 13 | [functiongemma](../functiongemma/README.md) | Natural-language task manager | gemma | Local tool proposal, schema validation, approval, one-time effects |
| 14 | [adk-kotlin](../adk-kotlin/README.md) | Agent trip planner with Android client | gemini-chat | Independent JVM service, sessions, tools, client API contract |
| 15 | [adk-android](../adk-android/README.md) | In-app daily planner | functiongemma; adk-kotlin | Mobile agent ownership, local tools, bounded execution |
| 16 | [koog](../koog/README.md) | Goal coaching app | adk-kotlin | Kotlin workflow, persistence, feedback-driven plan revision |
| 17 | [mcp](../mcp/README.md) | Tool-connected dashboard | adk-kotlin | Controlled backend integration, tool discovery and authorization |
| 18 | [appfunctions](../appfunctions/README.md) | Assistant-accessible notes app | adk-android | Typed actions, caller permissions, supported-device verification |
| 19 | [mediapipe](../mediapipe/README.md) | Gesture movement game | ml-kit-vision | Live frames, landmarks, gesture interaction, missing detections |
| 20 | [production-ai](../production-ai/README.md) | Support assistant | rag-and-embeddings; adk-android | Evaluation, observability, budgets, rollout and recovery |
| 21 | [gemini-in-android-studio](../gemini-in-android-studio/README.md) | Habit tracker | ai-fundamentals | Dedicated app with reproducible AI-assisted implementation workflow |
| 22 | [android-cli](../android-cli/README.md) | Focus timer | ai-fundamentals | Dedicated app built, installed and verified from terminal |
| 23 | [android-skills](../android-skills/README.md) | Adaptive reading app | ai-fundamentals | Dedicated app demonstrating skill selection and verified changes |
| 24 | [agent-skills](../agent-skills/README.md) | Bookmark organizer | android-skills | Dedicated app plus reusable development skills and evaluations |

## Delivery waves

1. **1–4: foundation and cloud.** Establish UI/state, data boundaries, model integration, and the first complete codelab.
2. **5–10: vision and local intelligence.** Validate physical-device access and model capabilities before committing to provider-specific features.
3. **11–12: voice.** Build audio lifecycle fundamentals before live duplex interaction.
4. **13–18: actions and agents.** Establish tool validation/approval, JVM runtime boundaries, then mobile and cross-app integration. AppFunctions availability is a feasibility gate, not an assumed universal capability.
5. **19–20: perception and production.** A dedicated movement app and a dedicated operational reference app. Production practices still apply to earlier apps.
6. **21–24: development workflows.** Each gets its own finished app and documented workflow. We use CLI/skills throughout development even though their dedicated teaching apps come later.

If hardware or API access blocks a topic, document the blocker and take the next topic with satisfied prerequisites. Do not replace the blocked feature with a fake and call it complete.

## First app: AI fundamentals

Build a flashcard generator with editable source text, generated question/answer cards, explicit validation, and local save/delete. The first implementation slice is a usable Compose screen backed by a repository and ViewModel. A fake supports tests; a verified inference integration is required for the completed AI feature. Provider and pinned toolchain are selected in the app brief.

Acceptance: a learner can generate, edit, save and reopen cards; cancel a request; recover from an error; and reject invalid output. The roadmap and codelab walk through the same implementation, ending with an independent extension such as organizing saved cards by subject.

Follow [the learning contract](learning-contract.md) and [engineering standards](engineering-standards.md). Start by completing [an app brief](app-brief-template.md); app code is intentionally deferred from this foundation change.
