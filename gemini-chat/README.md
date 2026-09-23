# PocketChat · Gemini Chat for Android

**Explore ideas together.** Learn to build streaming conversations, follow-up context, local history, saved answers, and controlled AI actions.

**Status: Planned — design and build plan ready. No runnable app yet.**

![PocketChat design concept: Home, Conversation, and Saved](docs/design/pocketchat-concept.png)

*Design concept, not an implemented app screenshot. See the [design specification](docs/design.md) for exact scope.*

## What we are building

- Text conversations with streaming, Stop, Retry, and readable Markdown/code.
- Local conversation history and saved answers that reopen offline.
- Explicit lifecycle, failure, and context-limit behavior.
- Advanced chapters for validated takeaways and approved tool actions.

Standalone project planned in this directory, package `com.androidengineers.pocketchat`. Direct Gemini integration for a debug learning mode; no Firebase dependency. Learners supply their own credentials. Production credentials belong on an authenticated backend, a separate milestone.

## Follow the work

[App brief](docs/app-brief.md) · [Design](docs/design.md) · [Architecture](docs/architecture.md) · [Learning plan and build order](docs/learning-plan.md)

The PocketChat-specific roadmap update and hands-on codelab are planned; implementation checkpoints and setup commands will be published after verification.

[Browse all features](../docs/features.md) · [Cookbook home](../README.md)
