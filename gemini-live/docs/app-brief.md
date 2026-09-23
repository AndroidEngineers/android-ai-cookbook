# PocketCook: app brief and delivery plan

Status: Building. The first native implementation is under verification. Live model/device validation remains pending; see verification.md for evidence.

## Product

PocketCook is a native Android cooking companion: choose a curated recipe, put the phone beside the chopping board, and ask for help while following the steps. It is the next app after PocketCards and uses direct Gemini Live integration without Firebase.

Package: `com.androidengineers.pocketcook`. Independent project in `gemini-live/`, with its own Gradle wrapper and app module. Pocket is the collection's naming convention.

Primary interaction: open a recipe, start cooking, ask “What do I do next?”, interrupt with “Can you repeat that?”, and continue without losing the current step.

Audience: Kotlin/Compose developers who understand ViewModels and basic coroutines. Earlier voice/camera apps are not required; this journey teaches its audio prerequisites explicitly.

## Scope

Core release:
1. Browse three curated recipes and read ingredients and instructions offline.
2. Start and end a live voice conversation scoped to the selected recipe.
3. Interrupt responses, mute the microphone, and view supported transcripts.
4. Navigate steps manually, retain progress, and recover from a disconnected session.
5. Use accessible light/dark screens with explicit connection and microphone states.

Advanced chapters follow the verified core: validated step-navigation and in-app timer tools; optional CameraX visual context; a deployment token service. They belong to the full learning journey but must not delay proving voice feasibility.

Exclude initial scope: generated recipes, accounts/social feeds, shopping purchases, unrestricted device control, background conversation, nutrition/medical advice, and automatic claims of food safety from camera images. No foreground service is needed for a foreground-only first version.

## Runtime and credentials

Proposed stack: Compose Material 3, screen ViewModels, StateFlow, coroutines, OkHttp WebSocket, Kotlin serialization, AudioRecord and AudioTrack. CameraX belongs to the advanced chapter. Pin compatible toolchain and dependency versions during the first implementation milestone.

Inference runs in Gemini's cloud. Microphone audio leaves the device only during an explicitly started session; camera frames only after separate user enablement. The app needs network access for AI; local recipes remain usable offline.

Development mode: a debug-only, user-entered API key held in memory for that run; never generated into BuildConfig, committed, persisted, or included in release builds. This refines the earlier bring-your-own-key proposal to meet repository credential rules. Local secrets files alone do not protect an APK.

Deployment mode: an independently runnable service in `server/` issues constrained ephemeral tokens; the app connects directly to Gemini for audio. Service authentication, rate limiting, expiry and model restrictions must be implemented before describing it as deployable. Firebase is not required. Release builds must not expose the debug key entry path.

## Milestones and exit criteria

| Milestone | Work | Exit evidence |
| --- | --- | --- |
| 0: design contract | Three main screens, key states, tokens, navigation and portrait reference frames | Reviewable screen designs and component inventory; no generic replacement UI during implementation |
| 1: device feasibility | Independent app; direct handshake; mic capture; audio playback; interrupt; close session | Recorded physical-device conversation, exact model/API/toolchain versions, no embedded credentials |
| 2: app foundation | Recipe library/details; cooking UI; manual step navigation; progress storage | Offline navigation works; UI checked against design; fakes visibly limited to previews/tests |
| 3: voice product | Integrate session controller; recipe context; transcripts; lifecycle; reconnect | Core end-to-end scenario plus denied permission, interruption, network loss, background and repeat-start checks |
| 4: tools | Step requests and a bounded in-app timer with deterministic local state | Invalid/duplicate/cancelled tool calls tested; UI reflects actual execution, not model claims |
| 5: camera | Explicit camera toggle, preview, bounded frame preparation and sending | Permission denial and lifecycle verified; camera off means no frames sent |
| 6: deployment and hardening | Token service, release configuration, accessibility, resource budgets and evaluation | Clean-checkout setup and release build verified; device measurements reported without invented targets |
| 7: learning release | Course, roadmap, starter/solution checkpoints, codelab, screenshots and demo | Each chapter executed against its pinned source; website deployed and links checked before Released status |

At 10–15 hours/week, keep one implementation milestone active. Estimate calendar duration after milestone 1; audio routing and echo behavior are real device-dependent risks. The smaller core can be published as a clearly scoped release before advanced chapters are complete.

## Acceptance scenarios

- Select a recipe, grant microphone permission, receive a relevant audible answer, interrupt it and receive a new answer without old queued audio returning.
- Double-tapping Start produces one connection and one microphone owner.
- Denied permission leaves the recipe usable; permanent denial offers a route to settings.
- Mute stops capture/sending; End stops capture/playback and closes the socket. Back/background ends the live session; return offers explicit restart with retained recipe progress.
- Rotation does not create duplicate resources. Process death restores recipe progress but never automatically restores recording.
- Airplane mode preserves current step, shows a recoverable state and never claims the assistant is listening while disconnected.
- Old callbacks cannot mutate a new session. Audio buffers and reconnect attempts are bounded.
- Light/dark, large text, TalkBack, compact/expanded layouts, speaker and headset paths are checked. Bluetooth support claims require device evidence.
- Ask an ambiguous cooking question and a visual food-safety question; the assistant asks for clarification and avoids unsupported certainty.

## Evidence and references

Implementation will add `docs/verification.md` with commands, devices, API/model versions, measured connection/response/interruption latency, task outcomes and known limitations. No performance values are claimed yet.

- [Design specification](design.md)
- [Architecture proposal](architecture.md)
- [Learning plan](learning-plan.md)
- [Direct WebSocket guide](https://ai.google.dev/gemini-api/docs/live-api/get-started-websocket)
- [Ephemeral tokens](https://ai.google.dev/gemini-api/docs/live-api/ephemeral-tokens)
- [Google Live cookbook](https://github.com/google-gemini/cookbook/blob/main/quickstarts/Get_started_LiveAPI.ipynb)

API references checked 2026-09-23. Recheck exact model access and capabilities during feasibility; do not lock a model from a marketing screenshot.
