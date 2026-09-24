# PocketCommunity development agent

Node.js 22+, no runtime npm dependencies. See the [app setup](../README.md#connect-gemini--where-the-key-goes).

```sh
cp .env.example .env
# Edit .env with your own Gemini key, then:
npm start
npm test
```

`GET /health` reports only configuration presence. `POST /chat` accepts `{prompt, eventId?, surfaceId, history?, action?, surfaceData?, previousComponents?}` and returns `{text, messages}`. The key is sent only to Google's Gemini API in a request header. The DevEarth source is fixed to the public discovery endpoint. No credentials are needed for discovery.

This is a loopback-only development service, not a public deployment template. Add authenticated users, quotas, rate limits and operational monitoring before deploying. Your model must be available to your Gemini project. No model fallback or fabricated feed fallback exists. History is bounded to 12 turns and UI context to 3 component trees. On `refine`, checked values are carried into exact matching checklist labels; new or changed labels begin unchecked.

The output validator bounds component counts, graph depth, text length, actions, event IDs and checkbox paths; rejects cycles, missing references, unregistered components and arbitrary functions; then wraps the agent's component composition in A2UI protocol messages. Schema checks cannot guarantee factual accuracy: evaluate rendered results against the supplied listing data.
