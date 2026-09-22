# Recipe Lab

One Android app for the cookbook's first six recipes. Fixture mode works offline after dependencies have been downloaded. Firebase live mode is opt-in.

## Build and run

Install Android SDK platform 36, Build Tools 36.0.0, and JDK 17. Open this directory in Android Studio with AGP 9.0 support. Configure `local.properties` with your SDK path if the IDE has not done so. Use an API 26+ emulator/device.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:installDebug
# Start the installed Android + AI Cookbook app from the launcher.
./gradlew :app:connectedDebugAndroidTest
```

Windows users should replace `./gradlew` with `gradlew.bat`. The six numbered tabs are independent exercises. Fixture responses are labelled and make no model calls. Leaving a tab or backgrounding the streaming screen cancels its work. Changes are in memory and do not persist across process restarts.

## Pinned dependencies

| Component | Version |
| --- | --- |
| Gradle / Android Gradle Plugin | 9.1.0 / 9.0.1 |
| JDK | 17 |
| Compose compiler / resolved Kotlin stdlib | 2.3.20 / 2.3.20 |
| Compose BOM | 2026.03.01 |
| Android compile/target/minimum API | 36 / 36 / 26 |
| Activity / Lifecycle | 1.13.0 / 2.10.0 |
| Coroutines / serialization JSON | 1.10.2 / 1.9.0 |
| Firebase BOM | 34.19.0 |
| ADK Android | 0.1.0 |

The ADK artifact's manifest requires API 26. Its published API differs from upstream main: the tool arguments are `Map<String, Any>`, and the sample enforces its own call budget rather than relying on an unavailable `RunConfig.maxLlmCalls` property. No KSP processor is needed because the tool declares its schema explicitly through BaseTool.

## Optional live Firebase mode

1. Create/select your Firebase project and register Android package `in.androidengineers.cookbook`. Enable Firebase AI Logic using its console setup flow and select the Gemini Developer API backend used by this adapter.
2. Download **your project's** `google-services.json` to `app/google-services.json`. It is ignored by Git. It contains Firebase client configuration, not a privileged Gemini/service-account credential. Do not embed a provider API key or service-account secret in the app.
3. Follow [Firebase's App Check setup](https://firebase.google.com/docs/ai-logic/app-check). For local debug builds, register the SDK-generated debug token in your own Firebase console. Treat that token as private; do not paste it into issues or commit it.
4. Rebuild and reinstall the app. The Google Services plugin is applied only when the configuration file exists. CookbookApplication initializes the debug App Check provider before calls. Release builds use Play Integrity; register and configure that provider before distributing a release.
5. Choose an available model in your Firebase project. Enter its exact identifier in **2 Firebase**, then press **Send live request (may cost)**. The fixed prompt asks for a short ViewModel explanation; no personal data is sent by the sample.
6. Review your project's pricing, billing, quotas, and usage before calling the provider. Set appropriate controls in the console. App Check is an abuse signal, not user authentication or a complete authorization system.

[Official getting-started guide](https://firebase.google.com/docs/ai-logic/get-started?platform=android). Live Firebase calls have not been run for this release; the adapter is compiled and the fixture/configuration-missing path is tested. Configure your own account before attempting live mode. API/provider availability can change.

## Tests and evidence

Unit tests cover state ownership, strict JSON validation, retry/cancellation boundaries, and actual ADK runner execution with a scripted model. Instrumentation checks the six recipe screens, navigation cancellation, and background cancellation. See [release evidence](../../docs/verification/v0.1.0-fixtures.md).

The approval recipe simulates a single-user, in-memory action. It is not a durable authorization service. Do not substitute real writes without server-side identity, authorization, replay protection, and concurrency handling.
