# PocketChat app brief

Status: **Building — the initial Android implementation exists locally. See verification.md for tested behavior and remaining work.**

PocketChat · Gemini Chat for Android · `com.androidengineers.pocketchat`

## Product

“Explore ideas together.” An approachable conversation workspace for people who want to ask follow-up questions and keep useful answers. Android developers learn how to own the entire conversation lifecycle, beyond a single generation request.

Core interaction: ask a question → see an answer stream → ask a follow-up → save a useful answer → reopen the conversation later.

## Scope and order

Core release features:
1. Text chat with incremental responses, Markdown/code blocks, Stop, and explicit Retry.
2. Multi-turn context with a bounded context policy and one active generation at a time.
3. Local conversation history: reopen, rename, search, delete, and recover interrupted work.
4. Saved answers with copy, search, source navigation, and removal.
5. Responsive, accessible Home, Conversation, Saved, and Settings screens.

Advanced chapters in the same app, after core chat is verified: validated takeaway cards; a bounded tool loop for searching saved notes and proposing a note to save. Confirm writes in application UI before committing them. Disclose when local note content is sent to the cloud; never search and upload the entire library silently.

Later/separate scope: voice, images, image generation, web grounding, RAG, attachments, cloud sync, subscriptions, autonomous agents, and editing earlier turns/branching. No affordances for these in the initial UI. These subjects have dedicated cookbook apps where appropriate.

## Learners and outcomes

Prerequisites: Kotlin, Compose basics, coroutines, and basic HTTP/JSON; PocketCards is useful but optional. Learners can explain and implement streaming cancellation, context construction, durable messages, recovery, validated model output, and controlled tool effects. The app must remain understandable as one independently buildable project.

## Runtime and credentials

Android Compose app with cloud Gemini inference, Room for local conversations, and no Firebase dependency. No microphone or camera permission.

The direct-provider path is a **debug learning mode**: a learner enters their own key at runtime, retained only in memory, never bundled, logged, backed up, or committed. Clear it when the app goes to the background; cancel active cloud work at that boundary. Device-local persistence does not mean prompts are private from the model provider. Explain this before the first request.

Production is a separate, explicit backend milestone: authenticated requests, authorization, server-held provider credentials, per-user quotas, cancellation propagation, and bounded output. Keep the direct key adapter debug-only; release cloud chat remains unavailable until that backend is implemented and verified. Offline history remains usable. A backend URL alone does not constitute production readiness.

## Dependencies and feasibility gate

Use Compose/Material 3, ViewModel/StateFlow, coroutines, Room, a minimal streaming HTTP transport, and constructor injection. Evaluate Navigation 3/adaptive compatibility before pinning. Start with one app module; use cases only for complex/reused policies.

Before app scaffolding, verify current official Android/Gemini documentation and publish a compatible pinned JDK, Gradle, AGP, Kotlin, Compose, Room, navigation, serialization, HTTP, SDK, and model/API selection. No dependency versions or model IDs are asserted by this plan. Check whether the selected API owns server-side conversation state and document that boundary; local app history remains the UI source of truth.

Required verification resources: Android SDK/JDK, emulator, physical phone with network, learner-owned Gemini access/quota, and wide-window emulator. No shared keys required for unit tests or CI. Model integration must pass a real request, streaming cancellation, follow-up context, and failure recovery before expanding scope.

## Acceptance

- A clean checkout builds and follows documented setup with no author-owned configuration.
- An answer starts rendering before generation completes; Stop terminates client streaming and preserves the partial answer as stopped, not successful. Explain that server billing may continue after disconnection.
- Double-tapping Send does not produce duplicate turns. A canceled or old request cannot append to a newer answer.
- Rotation preserves the active conversation and does not start another request. Backgrounding cancels generation and clears the debug key. Process restart exposes interrupted messages and permits explicit retry after setup.
- Completed messages survive restart. No failed, stopped, or interrupted answer silently becomes context for later turns.
- Retry reuses the user turn, creates a distinct attempt, and never automatically repeats a potentially billed request.
- History and saved answers work offline. Delete removes local content; explain it does not delete provider-side records. Saved copies survive source deletion with an unavailable-source label, until explicitly removed or all local data is cleared.
- Streaming does not yank the viewport away from someone reading earlier messages. Stop, Copy, and Retry are accessible at large font sizes and with TalkBack.
- Advanced writes validate inputs, require confirmation, and commit at most once per tool-call identity.

## Delivery

Follow [design](design.md), [architecture](architecture.md), and [learning plan](learning-plan.md). Build order is design → real streaming slice → durable chat → advanced chapters → clean-checkout learning walkthrough. Estimate remaining effort after the streaming slice, at 10–15 hours/week. Do not declare the learning journey released until source, screenshots, roadmap, codelab, and live links are verified.

Current evidence is recorded in [verification.md](verification.md). The initial Android slice is implemented; live-model acceptance and the published PocketChat codelab remain outstanding.
