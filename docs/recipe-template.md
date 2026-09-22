# Recipe authoring template

Authoring template for `android-ai-cookbook`. Replace bracketed fields before publishing. Do not publish the template as a completed recipe.

## [Observable outcome, e.g. Cancel a streamed answer when leaving the screen]

[One paragraph: who needs this, what they will build, and the engineering decision being taught.]

**Status:** [draft / tested-fixture / tested-live / experimental / needs-update / retired]

**Last verified:** [date, release tag, relevant commit]

**Runs:** [emulator / specific physical device / local backend / cloud account]

**Cost and credentials:** [none for fixture mode; exact setup requirements for live mode]

### See the result

[Short recording or screenshot with alt text. Identify simulated versus live behavior. Describe what is observable; do not invent speed or quality claims.]

### Before you start

| Requirement | Tested configuration |
| --- | --- |
| Android Studio / JDK / Gradle / AGP | [versions] |
| Kotlin / Compose | [versions] |
| Android API / device | [versions, model support, physical-device needs] |
| Model SDK and model identifier | [exact tested version or explicit fixture] |
| Backend / cloud services | [required configuration and supported credential boundary] |

Prerequisite academy lessons: [links]. Explain device unavailability and missing-model behavior before the learner reaches a dead end.

### Run it

[Provide clone, project selection, configuration, build, and run steps against an actual release. Supply the fake mode first where it teaches the same application boundary.]

**Expected result:** [exact visible behavior or fixture output].

**If it fails:** [three likely setup problems, diagnostic checks, and remedies].

### Understand the request path

[Small architecture diagram and explanation of which component owns UI state, model calls, identity, tools, persistence, cancellation, and errors. Explain only components used by this recipe.]

### Walk through the important code

[Link to real source files and explain the relevant implementation. Keep the source authoritative; avoid a second drifting copy of the entire application in the README.]

### Break it deliberately

| Failure | How to reproduce | Expected application behavior | Test / evidence |
| --- | --- | --- | --- |
| [e.g. late chunk after cancellation] | [fixture or action] | [old output rejected] | [test name] |
| [e.g. invalid model output] | [fixture] | [controlled error or review state] | [test name] |

### Try the next step

[One independent challenge. Include expected behavior and an assessment guide or a separately linked worked solution. Do not describe an unimplemented TODO as a working feature.]

### Learn the engineering behind this recipe

[One specific academy lesson CTA with recipe-specific UTM parameters. Explain what the learner will understand or build next.]

### Verification and limitations

[Distinguish compile checks, deterministic tests, emulator runs, physical-device runs, live-provider checks, and performance measurements. Describe exactly what has and has not been executed.]

### References and attribution

[Primary documentation, source inspirations, licenses and attribution where applicable. Date version-sensitive checks. Do not reproduce private course material or another repository's tutorial without the right to reuse it.]

---
