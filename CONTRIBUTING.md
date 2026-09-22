# Contributing

Choose one topic from the [feature index](docs/features.md). Each top-level topic folder will hold its own dedicated app or runtime-appropriate project.

## Where code belongs

Put the project directly in its topic folder, such as `gemini-chat/` or `adk-kotlin/`. Keep its README as the entry point. Each project owns its build files, dependencies, tests, and any synthetic inputs; document the directory to open or command to run. Kotlin JVM topics do not need to become Android apps.

## Adding an app

1. Define one useful learner outcome and use the [recipe template](docs/recipe-template.md) as a guide.
2. Include prerequisites, exact setup commands, device requirements, and any accounts or costs.
3. Add a real screenshot or recording and explain the important source files.
4. Test the main flow and at least one failure and recovery path. Record commands, versions, environment, and results alongside the project.
5. Distinguish simulated responses from verified live-model behavior. Keep the topic marked Planned until runnable code is included.
6. Link related academy lessons or official references. Update the root README and feature index when availability changes.
7. Add CI for the dedicated project when it is introduced.

Preserve attribution and licenses. Do not include credentials, private data, or third-party material without permission. Cloud credentials and privileged actions need an appropriate application or backend boundary.

Before implementing an app, read [AGENTS.md](AGENTS.md), [the app order](docs/app-roadmap.md), and [the engineering standards](docs/engineering-standards.md). Use the project-local skills relevant to the change.
