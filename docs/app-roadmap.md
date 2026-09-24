# App implementation order

## Current addition: PocketCommunity

[A2UI / PocketCommunity](../a2ui/README.md) is the 25th topic, added at the user’s request. Live Gemini event → venue → preparation flow is verified on a physical phone. Next: evaluate broader prompts and deliver the roadmap/codelab. Status: Live-agent Preview, not a released learning journey. The original ordering below predates this addition.

The original plan covers 24 topics; A2UI brings the collection to 25. **PocketCards, PocketCook, and PocketChat are Building**; **PocketStories is a preview**, and the other 20 original topics are Planned. The naming family is **Pocket**: PocketCards, PocketCook, PocketChat, PocketStories, and PocketCommunity are selected; remaining names are undecided. PocketChat has a runnable core chat implementation; its website roadmap and codelab are next. PocketCook release and learning verification remain tracked separately. This is build order, not a promise that all learners must complete every earlier topic.

Work on one app's complete learning journey at a time. At 10–15 hours/week, estimate each app after its first vertical slice and device/API feasibility check; do not assign speculative completion dates to all 24.

| Order | Topic | App concept | Prerequisites / reusable learning | Core milestone |
| --- | --- | --- | --- | --- |
| 1 | [ai-fundamentals](../ai-fundamentals/README.md) | PocketCards — flashcard generator | None; Kotlin/Compose basics | One validated AI request, editable cards, save/delete, failure recovery |
| 2 | [gemini-live](../gemini-live/README.md) | PocketCook — hands-free cooking companion | Kotlin/Compose/coroutines; audio basics included | Direct Gemini Live without Firebase; realtime audio, interruption, reconnect and lifecycle |
| 3 | [gemini-chat](../gemini-chat/README.md) | PocketChat — conversation workspace | ai-fundamentals | Conversation ownership, streaming, cancellation, history |
| 4 | [firebase-ai-logic](../firebase-ai-logic/README.md) | Ingredient meal planner | gemini-chat | Protected mobile inference, structured recipes, saved plans |
| 5 | [multimodal-ai](../multimodal-ai/README.md) | Camera curiosity app | firebase-ai-logic | Image selection, media preparation, grounded follow-up UI |
| 6 | [ml-kit-vision](../ml-kit-vision/README.md) | Receipt and barcode organizer | ai-fundamentals | Camera input, OCR/barcodes, correction, local storage |
| 7 | [ml-kit-genai](../ml-kit-genai/README.md) | Writing workspace | ai-fundamentals | Capability/download states, rewrite/summary, before/after UI |
| 8 | [litert](../litert/README.md) | Model benchmark playground | ai-fundamentals | Compatible runtime/model loading and measured inference |
| 9 | [gemma](../gemma/README.md) | Offline story studio | litert | Downloadable model, story sessions, offline generation |
| 10 | [on-device-ai](../on-device-ai/README.md) | Private journal assistant | gemma; ml-kit-genai | Local routing, model delivery/removal, privacy and recovery |
| 11 | [rag-and-embeddings](../rag-and-embeddings/README.md) | Cited study library | gemma | Ingest, embed, retrieve, answer with citations and abstention |
| 12 | [voice-ai](../voice-ai/README.md) | Voice note organizer | gemini-chat | Recording, transcription, action extraction, playback |
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

1. **1–2: flagship learning journeys.** Complete PocketCards release checks, then PocketCook. PocketCook teaches its own audio prerequisites; camera is an advanced chapter. Follow [the PocketCook plan](../gemini-live/docs/app-brief.md).
2. **3–5: cloud conversations and vision.** Start with [the PocketChat plan](../gemini-chat/docs/app-brief.md): streaming chat, then protected inference and multimodal interaction.
3. **6–11: vision and local intelligence.** Validate physical-device access and model capabilities before provider-specific implementation.
4. **12: voice notes.** Recording, transcription and action extraction as a dedicated asynchronous voice workflow.
5. **13–18: actions and agents.** Tool validation, authorization, JVM boundaries and mobile integrations. AppFunctions availability is a feasibility gate.
6. **19–20: perception and production.** Dedicated movement and operational-reference apps; production practices also apply earlier.
7. **21–24: development workflows.** Dedicated apps demonstrating CLI and skills workflows, which we also use throughout development.

If hardware or API access blocks a topic, document the blocker and take the next topic with satisfied prerequisites. Do not replace the blocked feature with a fake and call it complete.

## First app: AI fundamentals

Build a flashcard generator with editable source text, generated question/answer cards, explicit validation, and local save/delete. The first implementation slice is a usable Compose screen backed by a repository and ViewModel. A fake supports tests; a verified inference integration is required for the completed AI feature. Provider and pinned toolchain are selected in the app brief.

Acceptance: a learner can generate, edit, save and reopen cards; cancel a request; recover from an error; and reject invalid output. The roadmap and codelab walk through the same implementation, ending with an independent extension such as organizing saved cards by subject.

Follow [the learning contract](learning-contract.md) and [engineering standards](engineering-standards.md). Start by completing [an app brief](app-brief-template.md); app code is intentionally deferred from this foundation change.
