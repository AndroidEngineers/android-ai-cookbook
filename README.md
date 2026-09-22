# Android + AI Cookbook

**Build AI features in Android apps with Kotlin and Jetpack Compose.**

Want to add Gemini Live, use ML Kit, run AI on-device, or build an agent with ADK? Pick a topic below to find its guided learning path, then try the code examples available here.

## What do you want to build?

**18 learning paths.** Each topic links to Android Engineers Academy for lessons and exercises. Cookbook code is growing; the six examples you can run today are listed [below](#try-the-code).

### Add AI to your Android app

| Topic | What you’ll learn |
| --- | --- |
| [Gemini Live in Android](https://www.androidengineers.in/roadmap/gemini-live-android?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Build real-time conversations with audio, interruptions, and reconnects. |
| [ML Kit GenAI + Gemini Nano](https://www.androidengineers.in/roadmap/gemini-nano-ml-kit?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Add on-device text and image features with capability checks. |
| [On-device AI for Android](https://www.androidengineers.in/roadmap/on-device-ai-android?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Run models locally and understand downloads, memory, and device support. |
| [Gemini API for Android](https://www.androidengineers.in/roadmap/gemini-api-android?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Build chat, streaming responses, structured output, and tool calling. |
| [Firebase AI Logic](https://www.androidengineers.in/roadmap/firebase-ai-logic?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Connect an Android app to Gemini with Firebase and App Check. |
| [Voice AI for Android](https://www.androidengineers.in/roadmap/voice-ai-android?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Build speech-to-text, AI responses, text-to-speech, and turn-taking. |
| [Multimodal Android apps](https://www.androidengineers.in/roadmap/multimodal-android-ai?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Combine camera, images, audio, and text in an AI feature. |

### Build agents and connect tools

| Topic | What you’ll learn |
| --- | --- |
| [ADK for Kotlin](https://www.androidengineers.in/roadmap/adk-kotlin?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Build JVM agents with tools, sessions, and orchestration. |
| [ADK for Android](https://www.androidengineers.in/roadmap/adk-android?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Put an agent in your Android app with lifecycle-aware sessions and local tools. |
| [Koog for Kotlin](https://www.androidengineers.in/roadmap/koog?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Build AI agents with the Kotlin-based Koog framework. |
| [MCP for Android developers](https://www.androidengineers.in/roadmap/mcp-android?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Connect an Android experience to tools through a controlled backend. |
| [Android AppFunctions](https://www.androidengineers.in/roadmap/android-appfunctions?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Expose app actions with typed inputs, permissions, and confirmation. |

### Learn the foundations and ship your app

| Topic | What you’ll learn |
| --- | --- |
| [AI for Android — start here](https://www.androidengineers.in/roadmap/ai-android-fundamentals?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Learn models, prompts, and when to choose cloud or on-device AI. |
| [Production AI on Android](https://www.androidengineers.in/roadmap/production-ai-android?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Handle failures, evaluation, privacy, costs, and rollout. |
| [Gemini in Android Studio](https://www.androidengineers.in/roadmap/gemini-android-studio?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Use AI assistance and Agent Mode to implement and debug features. |
| [Android CLI](https://www.androidengineers.in/roadmap/android-cli?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Build and verify Android projects from the terminal. |
| [Android Skills](https://www.androidengineers.in/roadmap/android-skills?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Use Android development skills to guide and check project changes. |
| [Create agent skills for Android](https://www.androidengineers.in/roadmap/agent-skills-android?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index) | Write reusable workflows for your coding agent. |

## Try the code

**One Android app, six examples. No cloud account needed for the default demo.**

[Run the sample in Android Studio](samples/recipe-lab/README.md) · [Download the demo APK](https://github.com/AndroidEngineers/android-ai-cookbook/releases/tag/v0.1.0-fixtures)

| Example | What you can try |
| --- | --- |
| [Streaming in Compose](recipes/01-cloud-basics/fixture-streaming-ui/README.md) | Display a response as it arrives. |
| [Firebase AI Logic setup](recipes/01-cloud-basics/firebase-first-feature/README.md) | Explore the integration and configure your Firebase project. |
| [Cancel a response](recipes/01-cloud-basics/lifecycle-cancel-stream/README.md) | Stop a request and prevent old results from updating the screen. |
| [Structured output](recipes/02-structured-output/validated-extraction/README.md) | Turn model output into validated app data. |
| [Retries and offline behavior](recipes/01-cloud-basics/bounded-recovery/README.md) | Try timeouts, errors, and recovery. |
| [ADK tool approval](recipes/04-adk-agents/adk-safe-tool/README.md) | Approve or reject an agent’s proposed action. |

These examples are tested with simulated model responses. Live Firebase inference and real-model ADK reasoning are not yet verified. The other topics above currently link to academy learning paths; their cookbook samples are not available yet. See [test results and limitations](docs/verification/v0.1.0-fixtures.md).

## New to Android + AI?

Start with [AI for Android fundamentals](https://www.androidengineers.in/roadmap/ai-android-fundamentals?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=topic_index), try [streaming in Compose](recipes/01-cloud-basics/fixture-streaming-ui/README.md), then choose the topic for the app you want to build.

## Help grow the cookbook

Have a recipe idea or want to add a sample? Read the [contribution guide](CONTRIBUTING.md) and use the [recipe template](docs/recipe-template.md).

Maintained by [Android Engineers](https://github.com/AndroidEngineers) · [Android Engineers Academy](https://www.androidengineers.in/roadmap?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=readme) · [Apache 2.0](LICENSE)
