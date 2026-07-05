## Context

The backend already exposes `POST /api/v1/conversations` and persists conversations with transcript and feedback items. Today `AnalyzeConversationService` produces a hardcoded transcript and simulated feedback. The Whisper service is already running in Docker Compose at `http://whisper:8080` with an `/inference` endpoint that accepts multipart audio and returns JSON like `{"text":"..."}`.

This design connects the backend to that running Whisper service, stores the uploaded audio locally, and replaces the simulated transcript with a real one. Feedback generation remains simulated; LLM integration is a future change.

## Goals / Non-Goals

**Goals:**

- Transcribe uploaded audio by calling the Whisper `/inference` endpoint.
- Persist uploaded audio to a local directory mounted from the host (`./audios`).
- Keep the audio storage mechanism behind a port so object storage (e.g., S3) can be plugged in later without changing domain or application code.
- Keep the request/response flow synchronous.
- Provide clear error handling when transcription fails.
- Add automated tests for the new adapters.
- Update documentation to reflect the real flow.

**Non-Goals:**

- Integrating an LLM for real feedback (still simulated).
- Asynchronous job processing or WebSocket progress updates.
- Language selection/translation for Whisper calls (English/auto-detect for now).
- Audio playback from server-side storage.
- Cloud object storage implementation (S3 adapter is not implemented, only the port is prepared).

## Decisions

1. **Introduce `TranscriptionPort` and `AudioStoragePort` in the application layer**
   - Rationale: Keeps domain/application code free of HTTP and filesystem details, consistent with the existing hexagonal architecture.
   - Rejected alternative: calling Whisper directly from the service.

2. **Implement `WhisperTranscriptionAdapter` with Spring `RestClient`**
   - Rationale: The existing stack is Spring WebMvc (blocking). `RestClient` is the idiomatic blocking HTTP client and requires no reactive starter.
   - Rejected alternative: `WebClient` with coroutines, which would pull in WebFlux/reactor and conflict with the current synchronous controller style.

3. **Implement `LocalAudioStorageAdapter` with a Docker-mounted directory**
   - Rationale: Minimal infrastructure for the local-first MVP. `infra/audios` is mounted to `/app/audios` in the backend container.
   - Storage reference format: `local:///app/audios/<uuid>.<ext>` so the source is explicit and a future S3 adapter can use `s3://...` without schema changes.
   - Rejected alternative: storing audio in PostgreSQL (`bytea`) or forwarding audio only without persistence.

4. **Pass raw audio bytes through the use case**
   - Rationale: Both storage and transcription need the bytes. Reading `MultipartFile.bytes` once in the controller and passing `ByteArray` keeps the service simple.
   - Rejected alternative: passing `InputStream`, which would require teeing or reading twice.

5. **Keep the API synchronous**
   - Rationale: Matches the current frontend “Analysing…” spinner and the local model latency expectations.
   - Rejected alternative: `202 Accepted` with polling, which is reserved for a future background-worker iteration.

6. **Use MockWebServer for Whisper adapter tests**
   - Rationale: Lightweight, no real Whisper container needed for unit tests, and easy to assert request shape and headers.
   - Rejected alternative: Testcontainers with Whisper (slower) or pure MockK (cannot validate real HTTP request serialization).

7. **Do not send `language` to Whisper yet**
   - Rationale: The frontend currently sends user-friendly labels (e.g., "Portuguese") while Whisper expects ISO codes. Auto-detect/default to English keeps this change focused.
   - Rejected alternative: building a language-code mapper now, which belongs to a future language-selection capability.

8. **Map `TranscriptionFailedException` to `503 Service Unavailable`**
   - Rationale: Whisper is an external dependency; a failure there is a downstream service issue, not a client error.

## Risks / Trade-offs

- [Risk] The local filesystem storage is lost if the container is removed without the host volume. → Mitigation: the `./audios` bind mount persists files on the host as long as the directory exists.
- [Risk] Whisper may reject the MIME type produced by the browser’s `MediaRecorder`. → Mitigation: the adapter forwards the file as-is; if Whisper fails, `TranscriptionFailedException` is surfaced. Future work can add transcoding if needed.
- [Risk] Large uploads could consume memory because the controller reads the full `ByteArray`. → Mitigation: `spring.servlet.multipart.max-file-size` is already capped at 25MB. For larger files, a future streaming/async worker should be introduced.
- [Risk] Synchronous transcription may timeout for long recordings. → Mitigation: keep recordings short in the MVP; document that async processing is planned.
- [Risk] `RestClient` error handling must distinguish HTTP errors from parse failures. → Mitigation: use `.onStatus(...)` and throw `TranscriptionFailedException` with descriptive messages.

## Migration Plan

1. Pull the branch and run `docker compose -f infra/docker-compose.yml up --build`.
2. The new `./audios` bind mount will be created automatically by Docker Compose.
3. Submit a recording through the webapp; verify the transcript comes from Whisper and a file appears in `infra/audios`.
4. To rollback, revert the backend image and remove or archive files in `infra/audios` if desired.

## Open Questions

- Should the local storage reference be exposed in the API, or kept internal? (Currently `audioStorageRef` is already returned; we will populate it with the local reference.)
- Should `infra/audios` be added to `.gitignore`? The directory itself can be tracked with `.gitkeep`, but audio files must not be committed.
