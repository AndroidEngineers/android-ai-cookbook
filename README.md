# Android + AI Cookbook

**Build AI features in Android apps with Kotlin and Jetpack Compose.**

Want to add Gemini Live, use ML Kit, run AI on-device, or build an agent with ADK? Pick a topic below to find its guided learning path, explore the app planned for it.

## What do you want to build?

**24 feature and workflow guides.** Open a topic to find its examples, availability, and learning resources. **[PocketCards](ai-fundamentals/README.md) is the first app in development.** Its manual/sample flow runs without cloud setup; live AI requires Firebase configuration and is not yet verified. The other 23 apps are planned.

### Add AI to your Android app

| Topic | What you’ll learn |
| --- | --- |
| [Gemini Live in Android](gemini-live/README.md) | Build real-time conversations with audio, interruptions, and reconnects. |
| [ML Kit GenAI + Gemini Nano](ml-kit-genai/README.md) | Add on-device text and image features with capability checks. |
| [On-device AI for Android](on-device-ai/README.md) | Run models locally and understand downloads, memory, and device support. |
| [Gemini Chat for Android](gemini-chat/README.md) | Build chat, streaming responses, structured output, and tool calling. |
| [Firebase AI Logic](firebase-ai-logic/README.md) | Connect an Android app to Gemini with Firebase and App Check. |
| [Voice AI for Android](voice-ai/README.md) | Build speech-to-text, AI responses, text-to-speech, and turn-taking. |
| [Multimodal Android apps](multimodal-ai/README.md) | Combine camera, images, audio, and text in an AI feature. |

### Explore open models, local search, and computer vision

| Topic | What you’ll learn |
| --- | --- |
| [Gemma on Android](gemma/README.md) | Build an offline chat app with a downloadable open model. |
| [LiteRT and LiteRT-LM](litert/README.md) | Run custom models locally and understand the inference runtime. |
| [RAG and embeddings](rag-and-embeddings/README.md) | Search personal notes and answer questions with source citations. |
| [FunctionGemma and local tool calling](functiongemma/README.md) | Turn a natural-language request into an approved app action. |
| [ML Kit vision and text](ml-kit-vision/README.md) | Build OCR, barcode scanning, and other camera-based ML features. |
| [MediaPipe for Android](mediapipe/README.md) | Build live perception features such as gesture and pose detection. |

### Build agents and connect tools

| Topic | What you’ll learn |
| --- | --- |
| [ADK for Kotlin](adk-kotlin/README.md) | Build JVM agents with tools, sessions, and orchestration. |
| [ADK for Android](adk-android/README.md) | Put an agent in your Android app with lifecycle-aware sessions and local tools. |
| [Koog for Kotlin](koog/README.md) | Build AI agents with the Kotlin-based Koog framework. |
| [MCP for Android developers](mcp/README.md) | Connect an Android experience to tools through a controlled backend. |
| [Android AppFunctions](appfunctions/README.md) | Expose app actions with typed inputs, permissions, and confirmation. |

### Learn the foundations and ship your app

| Topic | What you’ll learn |
| --- | --- |
| [AI for Android — start here](ai-fundamentals/README.md) | PocketCards: turn notes into flashcards and learn the AI fundamentals. |
| [Production AI on Android](production-ai/README.md) | Handle failures, evaluation, privacy, costs, and rollout. |
| [Gemini in Android Studio](gemini-in-android-studio/README.md) | Use AI assistance and Agent Mode to implement and debug features. |
| [Android CLI](android-cli/README.md) | Build and verify Android projects from the terminal. |
| [Android Skills](android-skills/README.md) | Use Android development skills to guide and check project changes. |
| [Create agent skills for Android](agent-skills/README.md) | Write reusable workflows for your coding agent. |

## Find your way around

```text
android-ai-cookbook/
├── adk-android/
├── adk-kotlin/
├── agent-skills/
├── ai-fundamentals/
├── android-cli/
├── android-skills/
├── appfunctions/
├── firebase-ai-logic/
├── functiongemma/
├── gemini-chat/
├── gemini-in-android-studio/
├── gemini-live/
├── gemma/
├── koog/
├── litert/
├── mcp/
├── mediapipe/
├── ml-kit-genai/
├── ml-kit-vision/
├── multimodal-ai/
├── on-device-ai/
├── production-ai/
├── rag-and-embeddings/
├── voice-ai/
└── docs/                     # Feature index and contribution guidance
```

Browse [all feature guides and their status](docs/features.md). Each topic folder will hold its own dedicated project when implemented.

## New to Android + AI?

Start with [AI for Android fundamentals](ai-fundamentals/README.md), then choose the topic for the app you want to build.

## Build plan and engineering

Follow the [24-app implementation order](docs/app-roadmap.md), [engineering standards](docs/engineering-standards.md), and [roadmap/codelab learning contract](docs/learning-contract.md). Development uses [official Android skills and project-local guidance](docs/skills.md).

## Help grow the cookbook

Have a recipe idea or want to add a sample? Read the [contribution guide](CONTRIBUTING.md) and use the [recipe template](docs/recipe-template.md).

Maintained by [Android Engineers](https://github.com/AndroidEngineers) · [Android Engineers Academy](https://www.androidengineers.in/roadmap?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=readme) · [Apache 2.0](LICENSE)
