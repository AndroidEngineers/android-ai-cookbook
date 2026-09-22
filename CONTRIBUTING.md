# Contributing

Choose one learner outcome from the catalog or open a recipe request. Keep the first pull request small enough to review and reproduce.

## Authoring workflow

1. Copy [the recipe template](docs/recipe-template.md) into the matching track.
2. Add a unique catalog entry with status `draft`. Assign an owner and link a relevant academy lesson.
3. Put runnable source in `samples/` and synthetic inputs in `fixtures/`. Share a Gradle build only when toolchains are compatible; isolate preview SDKs under `experimental/`.
4. Supply exact setup and run commands, supported environments, expected output, and a clearly labelled fixture mode where useful.
5. Exercise at least one failure and recovery path. Explain the decision being taught and give the learner an independent task.
6. Record the commands/results in an evidence document. Include versions, verification date, tested devices where relevant, and the commit tested. A fixture result must not be presented as a live-provider result.
7. Run `python3 scripts/validate_catalog.py` and the relevant sample build/tests. Submit source, docs, fixtures, and catalog changes together.

A maintainer coordinates an immutable release tag before a recipe is promoted to a tested status. The validator requires publication metadata but cannot prove the truth of test results or the existence of an external release; reviewers must check those claims.

## Review checklist

- One clear outcome and a working academy lesson link.
- Source builds and the documented commands reproduce the result.
- Loading, errors, cancellation, and unsupported configurations are described where relevant.
- Provider credentials and privileged actions have an appropriate server/application boundary.
- Costs, device restrictions, and external services are explicit.
- Fixtures are synthetic and distributable; source licenses and attribution are preserved.
- Claims match the recorded evidence. Do not label preview APIs as generally available without checking current official documentation.

Do not publish private course material or third-party content without permission. Be respectful and specific when discussing contributions. External pull requests should use deterministic fixtures without access to production secrets.
