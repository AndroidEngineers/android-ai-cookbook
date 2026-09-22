---
name: cookbook-architecture
description: Design and review MVVM architecture for a dedicated Android AI Cookbook app; use when adding screens, state, repositories, or project boundaries.
---

# Cookbook Architecture

Read ../../../docs/engineering-standards.md and the topic brief. Map the user action through UI state, ViewModel, repository, and any SDK/server boundary before coding. Keep the app independently runnable. Prefer a single app module until separation has a concrete purpose. Apply constructor injection and SOLID at meaningful boundaries; do not add pass-through use cases or blanket interfaces. Check cancellation, stale output, lifecycle restoration, and repository contracts. Propose the smallest justified structure, implement within the requested scope, then report behavioral tests and any tradeoff.
