# PocketStories verification — first implementation slice

Date: 2026-09-23. Status: Building / Preview; not a completed learning release.

## Toolchain and candidate model

- JDK 17; Gradle 9.1.0 with wrapper checksum; AGP 9.0.1; Compose compiler 2.3.20; Compose BOM 2026.03.01; SDK 36, min SDK 31.
- CameraX 1.6.2; Room 2.8.5; LiteRT-LM 0.16.1. This runtime matches the existing Kotlin toolchain; the newer 0.17.1 POM requires Kotlin 2.4.0. This is a compatibility choice, not a latest-version claim.
- Gemma 4 E2B GPU candidate, exact artifact revision/hash/size recorded in README and `GemmaArtifact`. Not yet loaded or measured on real hardware.

## Completed checks

- Debug APK builds and installs on emulator-5556 and the connected DN2101 Android 13 arm64 phone.
- Ten unit tests pass: six context/draft policy tests and four ViewModel tests for duplicate generation, late callbacks, accepted drafts, background stop and draft checkpointing. Inference is a controlled fake in these tests.
- Three emulator instrumentation tests pass: honest model setup; explicitly labeled sample; CameraX capture → model setup → reopen saved scene. The camera test initially exposed capture-before-ready behavior; the shutter now waits for a streaming preview. The rerun passed all three.
- Local native adapter compiles with explicit cancellation and bounded output. Compilation is not real inference evidence.
- Android lint: zero errors, 15 warnings, covering newer dependency/target versions and the conservative free-space check. No warning baseline or blanket suppression added.
- Final release build with shrinking passed after the camera-readiness fix. Final unit/lint checks also passed.
- Actual emulator home screen visually inspected. Fonts and illustration are bundled assets. No generated-model output is shown in the sample.

Commands: `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest :app:assembleRelease`; instrumentation via AndroidJUnitRunner on emulator-5556.

## Physical device and remaining evidence

The connected DN2101 reports Android 13 and approximately 8 GB RAM, but only 3.1 GB free storage at inspection. A browser download plus private import of this 2.01 GB artifact needs more headroom; the user was asked to free about 6 GB total. No user files were removed and no multi-gigabyte model download was started.

UI installation/launch on this phone is verified. Camera capture in the instrumentation test used the emulator's scene; physical camera behavior is not yet independently checked. No live model output, native cancellation completion, GPU compatibility, memory/thermal benchmark, or airplane-mode inference is claimed.

Next: download verified weights with sufficient storage, run a benign real camera scene, check interruption/reload and airplane-mode generation, measure cold/warm latency and peak memory, and record failures. If the GPU cannot initialize, select and document a compatible model/runtime path rather than silently sending data to the cloud.

Large-text, expanded-window, fixed light-theme and TalkBack reviews remain before release. Discovery inventory, branching, PDF export, narration and RAG remain unimplemented future milestones. The academy roadmap and codelab are still planned.

## In-app download update

- Debug build, lint and all 17 unit tests pass. Seven new controlled HTTP tests cover verified installation, checksum rejection, range resume, servers ignoring Range, interrupted transfer/retry, invalid Content-Range and cancellation while blocked on a response.
- A 1,024-byte range request to the actual pinned model URL returned HTTP 206. This checks endpoint access, not a complete model download.
- Setup now offers Download Gemma directly. Partial downloads survive cancellation/process death and resume on the next explicit tap. Only verified weights are promoted into the installed path. Manual import remains under Advanced setup.
- INTERNET permission is used for model acquisition. The downloader stores a single private copy, requiring approximately 2.6 GB free initially rather than browser-plus-import duplication. Keep the app open; no scheduled background download is claimed.
- Full artifact download and real-device inference remain unverified. The earlier release-build evidence predates this downloader update.

## Vision artifact correction

The first real-device attempt exposed `TF_LITE_VISION_ENCODER not found`. The earlier 2,008,432,640-byte `-gpu` artifact did not supply the required encoder. The app now pins `gemma-4-E2B-it.litertlm` at the same revision: 2,588,147,712 bytes, SHA-256 `181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c`. Upstream LFS metadata was checked, and a ranged read of the artifact header explicitly contains `tf_lite_vision_encoder`, `tf_lite_vision_adapter` and `tf_lite_end_of_vision`.

The incorrect app-owned model was deleted on the physical device with the user's authorization, preserving scenes and Room data. Approximately 2.8 GiB remains free; setup asks for 3.2 GB, including working headroom. Updated setup removes the known obsolete model on explicit download/import. Build, all 17 unit tests and lint pass. Full corrected-model download, GPU initialization and photo-to-story inference still require real-device verification; header inspection alone is not inference evidence.

## Public preview — 2026-09-24

The corrected model reached vision compilation on PocketStories_API_36_1. Native logs show WebGPU failing a 152,409,600-byte allocation against a 134,217,728-byte maximum GPU buffer. This is a confirmed emulator backend limit, not evidence of successful generation. No CPU vision fallback has been added. Three UI tests passed on that dedicated emulator before the inference attempt.

Public screenshots were captured from the actual app on emulator-5556: bookshelf, creation and corrected model setup. They show bundled artwork and UI, not generated model output. Root and app READMEs mark the project as Preview and describe the remaining runtime checks. No model weights, local SDK settings or build outputs belong in this commit.

Publication check: `:app:assembleRelease :app:testDebugUnitTest :app:lintDebug` passed on 2026-09-24 with the corrected model/download code. Staged-file checks excluded weights, APKs, generated build outputs and local configuration; README local links were checked.
