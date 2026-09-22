# Troubleshooting

- **No Gradle wrapper at the root:** open `samples/recipe-lab`, then run its wrapper.
- **Unsupported Java:** use JDK 17 for Gradle and confirm `./gradlew --version`.
- **Missing SDK:** install Android SDK platform 36 and Build Tools 36.0.0; configure your local SDK path.
- **Minimum API conflict:** use API 26+; do not override the ADK artifact's manifest requirement.
- **Firebase not configured:** fixture mode still works. Follow the sample README to add your own configuration and App Check setup, then rebuild.
- **Live Firebase denied:** check App Check registration, model availability, AI Logic setup, project billing/quotas, and the reported error. Do not disable attestation to conceal a failed setup.
- **Espresso InputManager error on API 36:** the sample pins Espresso 3.7.0, avoiding the older transitive implementation. Use the checked-in test dependencies.
- **ADK loop stops:** the scripted model has a three-call limit and the runner a five-second timeout. Repeating proposals must terminate; never remove limits to make a stuck test pass.
- **Catalog validation fails:** check IDs, relative paths, academy URL shape, tested versions, and evidence fields. The validator does not replace a real build or provider check.

For reports, include recipe, commit/tag, versions, command, expected/actual results, and the first meaningful error. Redact credentials and personal data.
