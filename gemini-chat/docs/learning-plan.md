# PocketChat learning delivery plan

Status: planned. No source checkpoint IDs or new academy/codelab URLs are published by this document. The existing Gemini Chat roadmap will be audited and updated when the app has verified source. Keep website course content separate from the codelab: lessons explain concepts; the codelab applies them to this same app.

## Outcome → lesson → exercise → checkpoint

Checkpoint names below are planning labels, not existing tags. Replace them with immutable commit/tag references only after the relevant implementation is verified.

| # | Roadmap lesson | Codelab action and observable check | Planned checkpoint |
| --- | --- | --- | --- |
| 1 | Conversation ownership and architecture | Clone cookbook, open `gemini-chat`, run a compiling starter; trace a user action | setup |
| 2 | Compose state and chat UX | Build Home/Conversation/Saved with labeled deterministic preview data; verify IME and empty state | ui |
| 3 | Gemini requests and credentials | Configure learner-owned debug credentials; receive one real reply; verify missing-key recovery | first-reply |
| 4 | Streaming and cancellation | Parse incremental events, append safely, stop the transport; inject a late chunk and verify it is ignored | streaming |
| 5 | Multi-turn context | Follow up using a fact from the first turn; inspect sanitized request structure in tests | context |
| 6 | Room and durable conversations | Save/reopen, rename, search, delete; restart app and verify messages remain | persistence |
| 7 | Lifecycle and recovery | Rotate during streaming, background, kill/restart, retry without duplicating user turns | recovery |
| 8 | Context limits and request budgets | Exceed a small test context budget; preserve recent pairs and surface omitted history | budgets |
| 9 | Validated structured takeaways | Parse schema output, reject invalid values, save only validated content | takeaways |
| 10 | Function calls and authorization | Search approved local notes, approve one write, replay duplicate call without duplicating effects | tools |
| 11 | Evaluation and production boundary | Run behavior tests and quality cases; explain why debug keys do not ship; document backend requirements | verified-app |
| 12 | Independent extension | Add conversation pinning with persistence, accessibility, and tests without copying a finished patch | assessment |

Backend production implementation is a separate advanced follow-up, not a prerequisite disguised as an already working mobile sample. Its chapter must either be fully runnable and verified or explicitly remain a design exercise.

## Lesson and codelab authoring requirements

Each lesson includes prerequisites, a concept explanation, a focused Kotlin snippet from the pinned source, a diagram where useful, a common failure, a check-your-understanding question, and a practice outcome. Include a streaming state diagram and a credential/data-flow diagram with Mermaid source plus accessible rendered assets.

Each codelab step names exact paths, explains why the change exists, provides a complete relevant snippet or precise patch context, shows expected visible behavior, gives a verification action, and provides recovery guidance. Avoid full-file dumping and unexplained copy/paste. Clone instructions use the public cookbook URL and a real pinned source revision. The starter must compile; demo data remains labeled until live inference is added. Advanced chapters must not introduce code not yet present in the app.

## Milestones and gates

1. **Design prepared:** app brief, architecture, this lesson map, and three-screen concept. Current milestone.
2. **Streaming slice:** verified compatible toolchain, real provider response, Stop, follow-up, and one failure on a device. Record API/model and rough first-token/end-to-end latency; estimate remaining effort now.
3. **Core chat verified:** storage, saved answers, lifecycle, cancellation, accessibility, adaptive layout, and clean build/lint/tests.
4. **Advanced learning verified:** takeaways, bounded tools, confirmation and replay tests, representative live output evaluation.
5. **Journey verified:** build from clean checkout, follow each codelab step, align article snippets/source, capture real screenshots/recording, verify all links and completion/certificate flows.
6. **Released:** publish website lessons and codelab, update cookbook links/screenshots, verify live pages, and prepare technical launch article. Distinguish authored walkthrough from independent learner review.

Capacity: 10–15 hours/week with AI assistance. Work one milestone at a time; do not commit to an arbitrary launch date before the API/device slice.

## Evaluation cases

Use a recorded prompt set: explain Kotlin coroutine cancellation; compare two approaches; follow up referring to a prior constraint; produce a fenced code example; handle an overly long request; produce takeaways with bounds; reject malformed tool args; deny save approval; replay a tool response. Check relevant context, usefulness, honest uncertainty, valid formatting, controlled effects, and recovery. Record model and date; do not assert deterministic natural-language answers or universal timing.

## Remaining work

No app code, source tags, finished lessons, finished codelab, device evidence, or production backend yet. These are explicit delivery gates, not completed features.
