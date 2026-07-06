## Context

The backend already records audio, converts it, transcribes it via a local Whisper container, and persists a `Conversation` with hardcoded `FeedbackItem`s. The hexagonal architecture isolates I/O behind ports (`TranscriptionPort`, `AudioStoragePort`, etc.), so adding an LLM is a natural extension: a new outbound port, a new adapter, and a service change to call it.

The platform is local-first and single-user today, but the user wants the option to point at an external OpenAI-compatible provider later without code changes. The target hardware is CPU-based, so model size and first-load latency matter more than absolute output quality.

## Goals / Non-Goals

**Goals:**
- Generate real language feedback (excerpt, correction, explanation) from the Whisper transcript using a local LLM.
- Support both local Ollama and external OpenAI-compatible providers through a single adapter and environment configuration.
- Keep the first version simple: no new domain entities, no database migrations, reuse the existing `FeedbackItem` table.
- Warm up the local model asynchronously after startup so user requests are not blocked by model loading.
- Surface LLM failures as clear `503` errors with loud backend logs.

**Non-Goals:**
- Multi-language prompts or feedback (English only for now).
- Per-user provider/API-key configuration (single deployment-wide key).
- Streaming responses from the LLM.
- Structured database columns for correction/explanation (everything is joined into `recommendation`).
- Grammar-level output constraints such as JSON schema/grammar beyond `response_format: json_object`.

## Decisions

### 1. Single OpenAI-compatible adapter for local and external providers

**Choice:** Build one `OpenAiCompatibleLlmAdapter` behind a domain `LlmAnalysisPort`.

**Rationale:** Ollama (`/v1/chat/completions`), llama.cpp server (`/v1/chat/completions`), and providers like Moonshot Kimi all implement the same chat-completions contract. A single adapter keeps the codebase small and provider-switching becomes a configuration change.

**Alternatives considered:** Separate adapters per provider. Rejected because the interfaces are identical; separate adapters would only duplicate HTTP code.

### 2. Ollama as the local runtime

**Choice:** Run Ollama in a new Docker Compose service and pre-pull `llama3.2:3b` on startup.

**Rationale:** Ollama handles model download, versioning, and OpenAI-compatible serving out of the box. This matches the existing Whisper entrypoint pattern and makes model swapping trivial (`LLM_MODEL=...`).

**Alternatives considered:** llama.cpp server. Rejected because it requires manual GGUF download and more setup, which conflicts with the goal of easy model experimentation.

### 3. LLM returns JSON; Kotlin joins fields into `recommendation`

**Choice:** The prompt requests a JSON object with `feedbackItems[].excerpt`, `correctedExcerpt`, and `explanation`. The service maps these into the existing `FeedbackItem(excerpt, recommendation)` where `recommendation = "Correction: <correctedExcerpt>\n<explanation>"`.

**Rationale:** JSON is deterministic and easy to parse. Joining the fields in Kotlin avoids a database migration and keeps the domain model unchanged for this first version.

**Alternatives considered:** Ask the LLM to return plain text. Rejected because parsing free-text feedback is fragile and makes testing harder.

### 4. Deployment-wide configuration via environment variables

**Choice:** `LLM_BASE_URL`, `LLM_API_KEY`, `LLM_MODEL`, and `LLM_TIMEOUT_SECONDS` are injected through `.env` → Docker Compose → Spring Boot properties.

**Rationale:** The app is single-user today, so a per-deployment key is the simplest path. External provider keys live server-side and never reach the frontend.

**Alternatives considered:** User-provided key via the webapp. Rejected for the first version to avoid frontend storage and transit complexity.

### 5. Async warm-up after application ready

**Choice:** A Spring component listens for `ApplicationReadyEvent` and launches a non-blocking coroutine (or `CompletableFuture`) that sends a trivial prompt to the LLM to force model load.

**Rationale:** Loading the model on first inference can take tens of seconds on CPU. Doing this after startup prevents the first real user request from paying that cost, without blocking Spring initialization.

**Alternatives considered:** Warm-up inside the Ollama entrypoint. Rejected because it delays container readiness and still benefits from an explicit backend health ping.

### 6. Fail the whole request when LLM is unavailable

**Choice:** If the LLM call fails, times out, or returns unparseable JSON, throw `LlmAnalysisException`, which the global handler converts to `503 Service Unavailable` with code `LLM_ANALYSIS_FAILED`. Nothing is persisted.

**Rationale:** This is the simplest failure mode for a first version and matches the existing Whisper failure behavior.

**Alternatives considered:** Persist the transcript without feedback and let the user retry later. Rejected to keep the first version simple and the API contract predictable.

## Risks / Trade-offs

- **JSON parsing fragility on small models** → Mitigation: log raw responses loudly on failure so prompts can be tuned; keep the schema minimal (three string fields).
- **First-run CPU latency** → Mitigation: container entrypoint pulls the model during startup; backend warm-up loads it into memory; default model is only 3B parameters.
- **Hallucinated corrections** → Mitigation: accept as a trade-off for speed; plan to evaluate larger models if quality is too low.
- **Provider differences in JSON mode** → Mitigation: use `response_format: {type: "json_object"}` where supported and strong prompt instructions; document that external provider must support this mode.
- **Ollama container runs as root by default** → Mitigation: project container standard prefers non-root, but Ollama's official image requires root for GPU access. Document this deviation or use a custom image later if needed.

## Migration Plan

1. Merge the backend changes (port, adapter, service, config, exception, warm-up).
2. Merge the Docker Compose changes (new `ollama` service, volume, healthcheck).
3. Update `.env.example` with new LLM variables.
4. On the target machine, run `docker compose -f infra/docker-compose.yml up`. Ollama will pull `llama3.2:3b` on first start.
5. Verify via backend logs that the warm-up prompt succeeded.
6. Submit a test recording through the webapp and confirm real feedback is returned.

Rollback: revert the compose file and backend code; the existing hardcoded feedback path is replaced, so a full revert is needed to restore the old behavior.

## Open Questions

- Should the warm-up also verify external provider connectivity when `LLM_BASE_URL` points outside the local network?
- Should we add a retry with exponential backoff for transient LLM failures, or keep single-attempt failures for now?
- Do we need to cap transcript length sent to the LLM to avoid context-window errors?
