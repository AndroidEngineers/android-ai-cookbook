# Project skills

Official skills from [android/skills](https://github.com/android/skills) are installed under `.agents/skills/` at commit `b1f707d90904129b5972b3cc6436b568583effe5`. Read only the skills relevant to the current task. Their presence does not establish compatibility with a future app's selected SDKs.

| Skill | Use when |
| --- | --- |
| android-cli | Creating projects and managing SDKs/devices |
| edge-to-edge | Handling system bars and keyboard insets |
| navigation-3 | Adding navigation and adaptive destination structure |
| adaptive | Designing for different window sizes and input devices |
| testing-setup | Choosing and setting up app test coverage |
| appfunctions | Exposing app actions through Android AppFunctions |
| ml-kit-genai-prompt-api | Integrating on-device ML Kit prompting |
| camerax | Implementing camera capture and lifecycle behavior |
| r8-analyzer | Reviewing release shrinking and keep rules |
| android-intent-security | Reviewing exported components and incoming intents |
| android-profiler | Measuring and investigating runtime performance |

Cookbook-authored skills: `cookbook-architecture`, `cookbook-ai-integration`, and `cookbook-learning-delivery`. These complement official workflow guidance and are not Google-authored or certified.

## Updating

See [the source manifest](skills-sources.json). Review upstream changes, install into a temporary directory at an explicit commit, compare the selected folders, and validate before replacing project copies. Preserve licenses and update the source commit and hashes together. Do not auto-update skills during an app implementation.

Project-local skills are available for discovery on the next turn in a task opened in this repository. `AGENTS.md` explicitly routes to them as well. The sibling website repository does not inherit these instructions.
