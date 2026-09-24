# PocketCommunity — A2UI

Status: implementation in progress, not a released learning journey.

## Product
Developer event discovery companion to DevEarth. One continuous conversation hosts native event, venue and preparation surfaces. Light ivory/lavender design follows the approved concept. No event comparison, booking or payment flow.

## First vertical slice
- Real AndroidX A2UI parser, processor and Material catalog; progressive surface messages and bound checkboxes.
- DevEarth public discovery API, preserving missing venue/price values and sponsored status.
- Required live Gemini agent backend; setup and retry replace scripted output. Provider key stays on server.
- User action events return to the conversation; Inspect exposes inbound protocol and outbound events.
- HTTPS official links handled by the application, not arbitrary model-provided intents.

## Learning
Kotlin/Compose and coroutines prerequisites. Learn catalog contracts, surfaces, component/data updates, action dispatch, state ownership, grounded agent output, transport validation and failure recovery. App -> roadmap -> codelab checkpoints follow verified implementation; no lessons published yet.

## Architecture and verification
Single app module, constructor-injected repository, ViewModel and lifecycle-aware StateFlow. Backend is independently runnable under server/. No cross-topic dependencies. No device permissions beyond internet. Verify build, unit tests, lint and emulator discover/venue/checklist flow; separately evaluate live model output. Deterministic messages exist only in instrumentation test sources. Current alpha dependency requirements are pinned explicitly.

## Later
Persistent conversations, richer artwork/map rendering, production authentication/rate limits and full learning material. No fabricated agendas, attendee lists or prices.
