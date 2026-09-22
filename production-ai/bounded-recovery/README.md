# Retry, timeout, and offline behavior

**Status: tested-fixture.** Runnable Android sample with deterministic inputs. Firebase live inference and real-model ADK reasoning are not verified.

## See the result

<img src="../../docs/images/recovery.png" alt="5 Recovery: captured from the Android emulator using synthetic inputs" width="320" />

The transient case succeeds on attempt 3. Offline and timeout scenarios stop after three attempts instead of spinning indefinitely. Cancel stops the active job.

## Before you start

Use Android Studio with AGP 9.0.1 support, JDK 17, Android SDK 36, and an API 26+ emulator/device. The wrapper pins Gradle 9.1.0; Compose compiler is 2.3.20 and Compose BOM is 2026.03.01. See [the sample setup](../../samples/recipe-lab/README.md) for the full dependency matrix and cloud configuration.

The published ADK 0.1.0 Android artifact requires minimum API 26, as verified by manifest merging. This sample follows the artifact rather than overriding its manifest requirement.

Fixture mode has no cloud cost and needs no account. It uses synthetic data only.

## Run it

```sh
git clone https://github.com/AndroidEngineers/android-ai-cookbook.git
cd android-ai-cookbook
git checkout v0.1.0-fixtures
cd samples/recipe-lab
./gradlew :app:assembleDebug :app:testDebugUnitTest
./gradlew :app:installDebug
```

Open **Android + AI Cookbook** on the emulator, select **5 Recovery**, and choose **Fail twice, recover; then Always offline and Always timeout**. In Android Studio, open `samples/recipe-lab` and run the `app` configuration. Windows uses `gradlew.bat`.

## Understand the request path

boundedRead wraps idempotent one-shot reads only. It retries IOException and its own per-attempt timeout, then backs off within a capped attempt count. It rethrows cancellation and refuses to treat an outer timeout as a recoverable provider failure. Validation errors are not retried. It is deliberately not applied to partially consumed streams or tool writes, where replay can duplicate effects.

Read [Streaming.kt](../../samples/recipe-lab/app/src/main/java/in/androidengineers/cookbook/Streaming.kt) and [the Compose screen](../../samples/recipe-lab/app/src/main/java/in/androidengineers/cookbook/MainActivity.kt). The source is authoritative; the lesson does not maintain a second copy of the implementation.

## Break it deliberately

The three fixture buttons inject success-after-failure, permanent offline, and a response slower than the per-attempt timeout.

**Regression checks:** `retryRecoversOnThirdAttempt, permanentOfflineStopsAtBudget, timeoutsAreBounded, cancellationDoesNotRetry, outerTimeoutDoesNotBecomeRetry, validationFailureDoesNotRetry`. See [unit tests](../../samples/recipe-lab/app/src/test/java/in/androidengineers/cookbook/RecipeTests.kt) and [Android tests](../../samples/recipe-lab/app/src/androidTest/java/in/androidengineers/cookbook/RecipeUiTest.kt).

## Try the next step

Add deterministic jitter through an injected delay policy while retaining the maximum attempt count.

**Assessment guide:** Use a seeded or scripted delay sequence in tests. Verify permanent failure still stops, outer cancellation does not retry, and a non-retryable exception exits after one attempt. Document the maximum total wait.

## Troubleshooting

- Gradle fails before compilation: select JDK 17 and install SDK 36. Run `./gradlew --version` to confirm the actual JVM.
- No device appears: start an API 26+ emulator, then check `adb devices` before installing.
- The result differs: confirm you selected the named recipe and fixture. A live provider is not expected to reproduce fixture wording.

## Continue learning

[Study the related Android Engineers Academy lesson](https://www.androidengineers.in/roadmap/gemini-api-android/lesson/practice-evaluating-and-operating-gemini-features?utm_source=github&utm_medium=repository&utm_campaign=android_ai_cookbook&utm_content=bounded-recovery) to connect this behavior to the broader engineering curriculum.

## Verification and limitations

See [the release evidence](../../docs/verification/v0.1.0-fixtures.md) for executed commands and environments. Deterministic tests do not establish production security, model quality, physical-device compatibility, or live cloud access. Recipe screenshots show fixture behavior.

## References

[Android coroutines testing](https://developer.android.com/kotlin/coroutines/test), [Firebase AI Logic setup](https://firebase.google.com/docs/ai-logic/get-started?platform=android), and [ADK on Android](https://developer.android.com/ai/adk). SDK interfaces were checked against the published artifacts during compilation. Recipe implementation and teaching material are original to this cookbook; the sample wrapper/setup originated from the Android CLI empty-activity template.
