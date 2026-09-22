# Android + AI Cookbook

**Build AI features in Android apps with Kotlin and Jetpack Compose.**

Want to add Gemini Live, use ML Kit, run AI on-device, or build an agent with ADK? Pick a topic below to find its guided learning path, then try the code examples available here.

## What do you want to build?

**18 feature and workflow guides.** Open a topic to find its examples, availability, and academy learning path. The six examples you can run today are listed [below](#try-the-code).

### Add AI to your Android app

| Topic | What you’ll learn |
| --- | --- |
| [Gemini Live in Android](recipes/gemini-live/README.md) | Build real-time conversations with audio, interruptions, and reconnects. |
| [ML Kit GenAI + Gemini Nano](recipes/ml-kit-genai/README.md) | Add on-device text and image features with capability checks. |
| [On-device AI for Android](recipes/on-device-ai/README.md) | Run models locally and understand downloads, memory, and device support. |
| [Gemini Chat for Android](recipes/gemini-chat/README.md) | Build chat, streaming responses, structured output, and tool calling. |
| [Firebase AI Logic](recipes/firebase-ai-logic/README.md) | Connect an Android app to Gemini with Firebase and App Check. |
| [Voice AI for Android](recipes/voice-ai/README.md) | Build speech-to-text, AI responses, text-to-speech, and turn-taking. |
| [Multimodal Android apps](recipes/multimodal-ai/README.md) | Combine camera, images, audio, and text in an AI feature. |

### Build agents and connect tools

| Topic | What you’ll learn |
| --- | --- |
| [ADK for Kotlin](recipes/adk-kotlin/README.md) | Build JVM agents with tools, sessions, and orchestration. |
| [ADK for Android](recipes/adk-android/README.md) | Put an agent in your Android app with lifecycle-aware sessions and local tools. |
| [Koog for Kotlin](recipes/koog/README.md) | Build AI agents with the Kotlin-based Koog framework. |
| [MCP for Android developers](recipes/mcp/README.md) | Connect an Android experience to tools through a controlled backend. |
| [Android AppFunctions](recipes/appfunctions/README.md) | Expose app actions with typed inputs, permissions, and confirmation. |

### Learn the foundations and ship your app

| Topic | What you’ll learn |
| --- | --- |
| [AI for Android — start here](recipes/ai-fundamentals/README.md) | Learn models, prompts, and when to choose cloud or on-device AI. |
| [Production AI on Android](recipes/production-ai/README.md) | Handle failures, evaluation, privacy, costs, and rollout. |
| [Gemini in Android Studio](recipes/gemini-in-android-studio/README.md) | Use AI assistance and Agent Mode to implement and debug features. |
| [Android CLI](recipes/android-cli/README.md) | Build and verify Android projects from the terminal. |
| [Android Skills](recipes/android-skills/README.md) | Use Android development skills to guide and check project changes. |
| [Create agent skills for Android](recipes/agent-skills/README.md) | Write reusable workflows for your coding agent. |

## Try the code

**One Android app, six examples. No cloud account needed for the default demo.**

[Run the sample in Android Studio](samples/recipe-lab/README.md) · [Download the demo APK](https://github.com/AndroidEngineers/android-ai-cookbook/releases/tag/v0.1.0-fixtures)

| Example | What you can try |
| --- | --- |
| [Streaming in Compose](recipes/gemini-chat/fixture-streaming-ui/README.md) | Display a response as it arrives. |
| [Firebase AI Logic setup](recipes/firebase-ai-logic/firebase-first-feature/README.md) | Explore the integration and configure your Firebase project. |
| [Cancel a response](recipes/gemini-chat/lifecycle-cancel-stream/README.md) | Stop a request and prevent old results from updating the screen. |
| [Structured output](recipes/gemini-chat/validated-extraction/README.md) | Turn model output into validated app data. |
| [Retries and offline behavior](recipes/production-ai/bounded-recovery/README.md) | Try timeouts, errors, and recovery. |
| [ADK tool approval](recipes/adk-android/adk-safe-tool/README.md) | Approve or reject an agent’s proposed action. |

These examples are tested with simulated model responses. Live Firebase inference and real-model ADK reasoning are not yet verified. Other feature folders are marked **Planned** and link to their academy learning paths. See [test results and limitations](docs/verification/v0.1.0-fixtures.md).

## Find your way around

```text
android-ai-cookbook/
├── recipes/                  # Browse by feature or workflow
│   ├── gemini-chat/
│   ├── gemini-live/
│   ├── ml-kit-genai/
│   ├── on-device-ai/
│   ├── firebase-ai-logic/
│   ├── adk-kotlin/
│   ├── adk-android/
│   ├── koog/
│   ├── mcp/
│   ├── appfunctions/
│   ├── voice-ai/
│   ├── multimodal-ai/
│   ├── ai-fundamentals/
│   ├── production-ai/
│   ├── gemini-in-android-studio/
│   ├── android-cli/
│   ├── android-skills/
│   └── agent-skills/
├── samples/recipe-lab/        # Android Studio project: six runnable examples
├── docs/                     # Setup, screenshots, and verification
└── catalog/recipes.json      # Implemented recipe inventory
```

Browse [all feature guides and their status](recipes/README.md). Open `samples/recipe-lab` in Android Studio to run the current examples.

## New to Android + AI?

Start with [AI for Android fundamentals](recipes/ai-fundamentals/README.md), try [streaming in Compose](recipes/gemini-chat/fixture-streaming-ui/README.md), then choose the topic for the app you want to build.

## Help grow the cookbook

Have a recipe idea or want to add a sample? Read the [contribution guide](CONTRIBUTING.md) and use the [recipe template](docs/recipe-template.md).

Maintained by [Android Engineers](https://github.com/AndroidEngineers) · [Android Engineers Academy](https://www.androidengineers.in/roadmap?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=readme) · [Apache 2.0](LICENSE)
