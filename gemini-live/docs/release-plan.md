# PocketCook core release and learning plan

Status: planning and hardening, 2026-09-23. Voice works according to the author's physical-device retry. No release tag or website publication is claimed.

## 1. Stabilize and freeze the core

Keep the core scope: offline recipes, Compose state, direct Gemini Live voice, manual steps, transcripts, mute/end, lifecycle cleanup, local progress. Preserve runtime-only debug credentials and offline-only release behavior until the token service exists.

Implemented hardening: fixed-duration capture chunks, mute epochs, and stale-queue cleanup; 19 unit tests and five emulator tests pass. Remaining priority: verify the reported capture-buffer overflow no longer occurs during a sustained ten-minute session with active UI/transcripts. The eight-item microphone channel now receives complete 100 ms chunks; nonblocking fragments are accumulated before enqueueing. Measure chunk sizes, queue occupancy and consumer delays without recording audio, text or keys. Choose an explicit buffering/backpressure policy from evidence; do not simply enlarge the queue or silently drop speech. Add a regression test for the resulting policy and session restarts.

Review audio ownership, interruption epochs, stale callbacks, cancellation, mute/unmute races and resource cleanup. Split or format dense session/audio code where it improves teaching and review. Re-run unit tests, lint, debug/release builds and focused device tests after changes.

Acceptance: sustained live conversation; repeated interruptions; mute/unmute; end/restart; last recipe step; permission denial; background; rotation; network disconnect/retry. Log actual device/model versions and measured first-audio/interruption timings. Run the README from a clean checkout. Scan all publishable files and staged changes for secrets/local configuration. Commit the reviewed core and record its immutable reference only after checks pass. Remote CI must pass before advertising it as verified.

## 2. Align the existing website roadmap

Update the existing gemini-live-android path; preserve current article URLs. Source of truth: src/data/android-ai-curriculum.json and src/content/articles/gemini-live-android/ in the website repository. The present six modules are short conceptual introductions: expand them into source-backed teaching chapters, with PocketCook as the continuous example.

| Existing lesson | PocketCook lesson and code | Learner evidence |
| --- | --- | --- |
| what-a-live-api-session-is | Clone/setup; Compose → CookViewModel → LiveConnection; handshake and state ownership; LiveProtocol.kt | Run offline first, draw setup/connected/error transitions, explain why Firebase is unnecessary |
| microphone-capture-and-audio-formats | AudioRecord in AndroidPcmAudio.kt; permission; PCM16 mono at 16 kHz; queue duration and backpressure | Derive 32,000 bytes/second and 3,200 bytes/100 ms; deny permission; reproduce a bounded overload |
| playback-and-turn-completion | AudioTrack at 24 kHz; partial writes; startup threshold; independent producer and playback progress | Explain the actual no-voice bug; verify small chunks keep playing and network turn-complete is not speaker-complete |
| interruptions-and-voice-activity | LiveProtocol events; interrupt epoch; flush; transcripts; mute versus end | Interrupt twice, verify stale queued audio cannot return, explain server turn detection |
| live-tools-and-session-recovery | Core: lifecycle, stale connection generations, recipe context, manual restart and progress | Background/network tests; state clearly that resumption and tool execution are not implemented |
| camera-context-and-realtime-evaluation | Core: a real-device evaluation rubric; camera remains an explicitly planned extension | Record task outcomes and latency without invented values; distinguish observations from screenshots |

Expand the six matching practice articles and capstone too. Add a course study guide and final assessment with expected reasoning and common mistakes. Each lesson needs focused compiling snippets with file paths and pinned source, a diagram where useful, tradeoffs, failure analysis and an independent exercise. Recheck official Gemini/Android documentation during authoring; exact model availability must match the tested release.

## 3. Build the core codelab

Working title: Build PocketCook: a realtime Gemini voice companion for Android. Proposed slug pocketcook-gemini-live is unpublished. Follow the existing PocketCards codelab data/rendering pattern; add a dedicated data module and register it in the catalogue only when ready.

1. See the finished app: three full-height screenshots with constrained width and uncropped aspect ratio; state core scope and prerequisites.
2. Clone the cookbook, check out the exact verified source ref, open gemini-live only, select JDK/SDK and run offline. Include Android Studio and terminal routes.
3. Map Compose, immutable state, ViewModel and local recipe progress. Predict last-step behavior before editing.
4. Configure a project-owned key through the debug dialog. Explain cloud processing, model access, quota, memory-only key storage and why release voice is unavailable.
5. Implement setup and typed protocol events. Do not start capture before setup acknowledgement.
6. Implement permission-aware microphone input; derive format/chunk calculations and test backpressure.
7. Implement streaming speaker output; fix a deliberately isolated startup-threshold exercise, handle partial writes and track actual playback.
8. Implement interruption, transcript display, mute and recipe context. Clearly distinguish spoken guidance from automatic navigation.
9. Own lifecycle and failure recovery; test old callbacks, background and network loss without automatic microphone restart.
10. Run tests and the real-device checklist; independently add a fourth local recipe with bounded progress, correct context and an automated test.

Each step has the exact source files, a small explained snippet, expected screen/output, a prediction question, a failure scenario and an observable check. Prepare an executable offline starter and verified core solution as immutable checkpoints; these do not exist yet. Avoid placeholder refs in published clone commands. Run every step from the starter before publication.

## 4. Advanced sequence, after core

Implement and verify validated navigation/timer tools → explicit CameraX sharing → authenticated ephemeral-token service and release voice. Then extend the same roadmap and add advanced codelab chapters. Do not teach planned features as working PocketCook capabilities or block the useful core lesson on their completion.

## 5. Publish together

Sync README, roadmap, course and codelab to the verified core source. Add reciprocal links using actual published URLs. Run website lint/build and verify rendered code, diagrams, mobile screenshot sizing and navigation. Deploy and inspect live pages before claiming publication. Mark core and advanced statuses separately. Keep one milestone active within the author's 10–15 hours/week; schedule estimates follow the reliability gate.
