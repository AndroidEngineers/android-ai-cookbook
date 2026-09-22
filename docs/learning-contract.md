# One app, one learning journey

Every topic owns a dedicated app, an academy roadmap, and a codelab for that same app. App names remain working decisions until selected.

## Required artifacts per topic

- `README.md`: app purpose, screenshot when implemented, availability, setup, learning links, and tested requirements.
- `docs/app-brief.md`: scope, learner prerequisites, core interaction, runtime dependencies, and acceptance criteria.
- `docs/architecture.md`: state/data flow, runtime and credential boundaries, and decisions that need explanation.
- `docs/learning-plan.md`: ordered lessons mapped to codelab steps and source checkpoints. Use existing academy URLs only when verified; label unpublished links as planned text.
- `docs/codelab.md`: executable steps, files to change, explanations, expected output, troubleshooting, and independent final challenge.
- `docs/verification.md`: exact commands, device/model versions, results, limitations, and clean-checkout evidence.

Add these files as each app is developed; avoid filling every topic with empty templates now.

## Milestones shared by roadmap and codelab

1. Meet the finished app and its user problem.
2. Prepare the environment and run the starter.
3. Build the UI and state foundations.
4. Integrate the topic's core capability.
5. Complete storage, navigation, and interactions required by the scope.
6. Handle errors, cancellation, unavailable features, and permissions.
7. Verify behavior and evaluate the AI feature.
8. Extend the app independently using measurable success criteria.

Each step includes a goal, prerequisites, source files/checkpoint, expected visible result, a verification action, and a related lesson. Keep checkpoints in immutable release tags or clearly recorded commits instead of many full project copies. A starter must compile and accurately state which behavior is intentionally incomplete.

## Completion states

Planned → Building → App verified → Learning journey verified → Released.

Do not mark the whole journey ready merely because the app builds. Release requires the completed app, reproducible codelab, aligned academy lessons, working links, and a clean-setup walkthrough. Record whether a walkthrough was author-run or performed by an independent learner. Publish website changes only within the user's authorized scope.
