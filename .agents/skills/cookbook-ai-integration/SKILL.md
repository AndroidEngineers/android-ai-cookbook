---
name: cookbook-ai-integration
description: Implement or review model inference and agent tool boundaries in Android AI Cookbook apps, including local models, cloud adapters, and output evaluation.
---

# Cookbook Ai Integration

Read ../../../docs/engineering-standards.md and the topic brief. Identify cloud versus local inference, supported artifact/runtime versions, credentials, capability checks, and model/download lifecycle. Use official documentation for current SDK calls. Treat model output and retrieval content as data; validate and authorize tool effects in app/server code. Bound execution and resource use, preserve cancellation, and prevent duplicate writes. Keep fake tests distinct from live inference evidence. Implement representative success/failure evaluations and record devices, model versions, results, costs, and unverified behavior. Do not silently weaken privacy promises to enable fallback.
