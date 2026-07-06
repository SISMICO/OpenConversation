## Why

Conversation analysis currently returns hardcoded placeholder feedback items after transcription. To deliver real language-learning value, the system must analyze the transcribed text with a local LLM, identify mistakes, suggest corrections, and explain them. The design must also allow swapping to an external OpenAI-compatible provider (e.g., Moonshot Kimi) by changing configuration, so the platform can evolve from local-first to hybrid without rewriting the integration.

## What Changes

- Add a backend outbound port `LlmAnalysisPort` and an OpenAI-compatible adapter that talks to Ollama, llama.cpp, or any external provider exposing `/v1/chat/completions`.
- Integrate LLM analysis into `AnalyzeConversationService` so feedback items are generated from the transcript instead of hardcoded values.
- Define a JSON feedback schema returned by the LLM (`excerpt`, `correctedExcerpt`, `explanation`) and map it into the existing `FeedbackItem.recommendation` field.
- Add async, non-blocking warm-up logic that loads the local model into memory after application startup.
- Add an Ollama service to the Docker Compose infrastructure, preconfigured to pull `llama3.2:3b` on first start.
- Add backend configuration properties for LLM base URL, API key, model name, and timeout.
- Return a standardized `503 Service Unavailable` error with code `LLM_ANALYSIS_FAILED` when the LLM is unreachable, times out, or returns unparseable output; log the underlying cause loudly.

## Capabilities

### New Capabilities

- `llm-conversation-analysis`: Generate real vocabulary, grammar, tense, and fluency feedback from a transcribed conversation using an LLM, with support for local and external providers via a unified OpenAI-compatible adapter.

### Modified Capabilities

- `conversation-rest-api`: Extend the `POST /api/v1/conversations` success path to return LLM-generated feedback items, and add a standardized `503` error response with code `LLM_ANALYSIS_FAILED` when LLM analysis is unavailable.

## Impact

- Backend Kotlin/Spring Boot: new port, adapter, properties, exception, warm-up component, and service changes.
- Docker Compose: new `ollama` service, volume, healthcheck, and backend dependency.
- Environment configuration: new `LLM_BASE_URL`, `LLM_API_KEY`, `LLM_MODEL`, and `LLM_TIMEOUT_SECONDS` variables.
- Tests: new unit tests for the adapter and service, plus integration-test configuration for the Ollama container.
